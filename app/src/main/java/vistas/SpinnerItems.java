package vistas;

public class SpinnerItems {

    private int idDocente;
    private String apellidos;
    private String nombres;

    public SpinnerItems(int idDocente, String apellidos, String nombres){
        this.idDocente = idDocente;
        this.apellidos = apellidos;
        this.nombres = nombres;
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
