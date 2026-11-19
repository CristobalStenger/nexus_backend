package cl.nexustech.usuario.model;

// Se escriben igual que en el frontend para que el JSON no necesite traducción
public enum Rol {
    Administrador, // acceso total
    Vendedor,      // ve productos y órdenes
    Cliente        // solo la tienda
}
