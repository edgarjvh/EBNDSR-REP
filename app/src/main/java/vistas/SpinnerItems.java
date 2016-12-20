package vistas;

public class SpinnerItems {
    private int registrado;
    private int idDocente;
    private String apellidos;
    private String nombres;

    public SpinnerItems(int registrado, int idDocente, String apellidos, String nombres){
        this.registrado = registrado;
        this.idDocente = idDocente;
        this.apellidos = apellidos;
        this.nombres = nombres;
    }

    public int getRegistrado() {
        return registrado;
    }

    public void setRegistrado(int registrado) {
        this.registrado = registrado;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
}
