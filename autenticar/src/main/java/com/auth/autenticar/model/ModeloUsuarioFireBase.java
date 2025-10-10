package com.auth.autenticar.model;

/**
 * Modelo de Usuario adaptado para Firebase Firestore
 * Eliminadas las anotaciones JPA (@Entity, @Table, @Column, etc.)
 * El ID ahora es String en lugar de Long (estándar de Firestore)
 */
public class ModeloUsuarioFireBase {

    private String idPersona; // Cambio de Long a String para Firestore
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private String contrasena;

    /**
     * Constructor vacío requerido por Firebase para la deserialización
     */
    public ModeloUsuarioFireBase() {
        this.rol = "USER"; // Valor por defecto
    }

    /**
     * Constructor completo
     */
    public ModeloUsuarioFireBase(String idPersona, String nombre, String apellido,
            String correo, String rol, String contrasena) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol != null ? rol : "USER";
        this.contrasena = contrasena;
    }

    /**
     * Constructor sin ID (para creación de nuevos usuarios)
     */
    public ModeloUsuarioFireBase(String nombre, String apellido, String correo,
            String rol, String contrasena) {
        this(null, nombre, apellido, correo, rol, contrasena);
    }

    // Getters y Setters

    public String getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public String toString() {
        return "ModeloUsuario [idPersona=" + idPersona +
                ", nombre=" + nombre +
                ", apellido=" + apellido +
                ", correo=" + correo +
                ", rol=" + rol +
                ", contrasena=" + contrasena + "]";
    }
}