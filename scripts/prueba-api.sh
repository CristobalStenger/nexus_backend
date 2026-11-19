#!/usr/bin/env bash
# ============================================================
# NexusTech - Prueba de punta a punta a través del API Gateway
# Uso:  ./scripts/prueba-api.sh [URL_DEL_GATEWAY]
#       (por defecto http://localhost:8080)
# Ojo: registra dos boletas de prueba; la pagada descuenta
# 1 unidad de stock del producto 1.
# ============================================================
set -u
API=${1:-http://localhost:8080}
RESPUESTA=$(mktemp)
fallos=0

# revisar "qué se prueba" código_esperado código_obtenido
revisar() {
  if [ "$2" = "$3" ]; then echo "  OK    $1 ($3)"; else echo "  FALLA $1 (esperado $2, llegó $3)"; fallos=$((fallos + 1)); fi
}
estado() { curl -s -o "$RESPUESTA" -w '%{http_code}' "$@"; }
login() {
  curl -s -X POST "$API/api/v1/auth/login" -H 'Content-Type: application/json' \
    -d "{\"correo\":\"$1\",\"clave\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'
}
compra() { # compra número_de_tarjeta
  echo "{\"cliente\":{\"nombre\":\"Pedro\",\"apellidos\":\"Soto Fuentes\",\"correo\":\"cliente@gmail.com\"},
    \"entrega\":{\"tipo\":\"despacho\",\"calle\":\"Los Crisantemos 789\",\"region\":\"Región Metropolitana\",\"comuna\":\"Providencia\"},
    \"items\":[{\"productoId\":1,\"cantidad\":1}],\"cupon\":\"FELIZ18\",
    \"pago\":{\"numeroTarjeta\":\"$1\",\"titular\":\"Pedro Soto\",\"vencimiento\":\"12/30\",\"cvv\":\"123\"}}"
}

echo "API Gateway: $API"
echo "1) Tienda pública"
revisar "Catálogo de productos" 200 "$(estado "$API/api/v1/productos")"
revisar "Detalle de producto" 200 "$(estado "$API/api/v1/productos/1")"
revisar "Categorías" 200 "$(estado "$API/api/v1/categorias")"
revisar "Cupón FELIZ18" 200 "$(estado "$API/api/v1/cupones/FELIZ18")"
revisar "Cupón inexistente" 404 "$(estado "$API/api/v1/cupones/NOEXISTE")"

echo "2) Autenticación (JWT)"
ADMIN=$(login admin@duoc.cl admin123)
VENDEDOR=$(login vendedor@duoc.cl vende123)
CLIENTE=$(login cliente@gmail.com cliente123)
revisar "Login con clave incorrecta" 401 "$(estado -X POST "$API/api/v1/auth/login" -H 'Content-Type: application/json' -d '{"correo":"admin@duoc.cl","clave":"mala"}')"
revisar "Perfil propio con token" 200 "$(estado "$API/api/v1/usuarios/me" -H "Authorization: Bearer $CLIENTE")"
revisar "Token falso" 401 "$(estado "$API/api/v1/usuarios/me" -H 'Authorization: Bearer token.falso.123')"

echo "3) Permisos por rol"
revisar "Administrador lista usuarios" 200 "$(estado "$API/api/v1/usuarios" -H "Authorization: Bearer $ADMIN")"
revisar "Vendedor NO lista usuarios" 403 "$(estado "$API/api/v1/usuarios" -H "Authorization: Bearer $VENDEDOR")"
revisar "Vendedor lista órdenes" 200 "$(estado "$API/api/v1/boletas" -H "Authorization: Bearer $VENDEDOR")"
revisar "Vendedor ve detalle de orden" 200 "$(estado "$API/api/v1/boletas/1001" -H "Authorization: Bearer $VENDEDOR")"
revisar "Cliente NO lista órdenes" 403 "$(estado "$API/api/v1/boletas" -H "Authorization: Bearer $CLIENTE")"
revisar "Sin sesión NO lista órdenes" 401 "$(estado "$API/api/v1/boletas")"
revisar "Vendedor NO crea productos" 403 "$(estado -X POST "$API/api/v1/productos" -H "Authorization: Bearer $VENDEDOR" -H 'Content-Type: application/json' -d '{}')"
revisar "Stock bloqueado desde afuera" 403 "$(estado -X POST "$API/api/v1/productos/stock/descontar" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d '[]')"

echo "4) CRUD del administrador"
revisar "Crea categoría" 201 "$(estado -X POST "$API/api/v1/categorias" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d "{\"nombre\":\"Prueba $(date +%s)\"}")"
ID=$(sed -E 's/.*"id":([0-9]+).*/\1/' "$RESPUESTA")
revisar "Valida datos (400)" 400 "$(estado -X POST "$API/api/v1/categorias" -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d '{"nombre":"x"}')"
revisar "Elimina categoría" 204 "$(estado -X DELETE "$API/api/v1/categorias/$ID" -H "Authorization: Bearer $ADMIN")"
revisar "Registro público de cliente" 201 "$(estado -X POST "$API/api/v1/auth/registro" -H 'Content-Type: application/json' -d '{"run":"111111111","nombre":"Prueba","apellidos":"API","correo":"prueba.api@gmail.com","clave":"prueba1","region":"Región Metropolitana","comuna":"Santiago","direccion":"Calle 123"}')"
grep -q '"tipo":"Cliente"' "$RESPUESTA" && echo "  OK    el registro crea un Cliente" || { echo "  FALLA el registro debía crear un Cliente"; fallos=$((fallos + 1)); }
ID=$(sed -E 's/.*"id":([0-9]+).*/\1/' "$RESPUESTA")
revisar "RUN repetido" 409 "$(estado -X POST "$API/api/v1/auth/registro" -H 'Content-Type: application/json' -d '{"run":"111111111","nombre":"Prueba","apellidos":"API","correo":"otra.api@gmail.com","clave":"prueba1","region":"Región Metropolitana","comuna":"Santiago","direccion":"Calle 123"}')"
revisar "Administrador elimina usuario" 204 "$(estado -X DELETE "$API/api/v1/usuarios/$ID" -H "Authorization: Bearer $ADMIN")"
revisar "No elimina categoría con productos" 409 "$(estado -X DELETE "$API/api/v1/categorias/1" -H "Authorization: Bearer $ADMIN")"

echo "5) Compra (pedido-service <-> producto-service)"
revisar "Compra pagada" 201 "$(estado -X POST "$API/api/v1/boletas" -H "Authorization: Bearer $CLIENTE" -H 'Content-Type: application/json' -d "$(compra '4242 4242 4242 4242')")"
grep -q '"estado":"pagada"' "$RESPUESTA" && echo "  OK    estado: pagada" || { echo "  FALLA estado esperado: pagada"; fallos=$((fallos + 1)); }
revisar "Compra con tarjeta 0000" 201 "$(estado -X POST "$API/api/v1/boletas" -H 'Content-Type: application/json' -d "$(compra '4242 4242 4242 0000')")"
grep -q '"estado":"rechazada"' "$RESPUESTA" && echo "  OK    estado: rechazada" || { echo "  FALLA estado esperado: rechazada"; fallos=$((fallos + 1)); }

rm -f "$RESPUESTA"
echo
[ "$fallos" -eq 0 ] && echo "Todo OK" || echo "$fallos prueba(s) fallaron"
exit "$fallos"
