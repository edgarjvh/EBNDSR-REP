package vistas;

public class lvCalendarioItems {
    int icono;
    String header;
    String titulo;
    String fecha;
    String descripcion;
    int antiguedad;

    public lvCalendarioItems(String header, int icono, int antiguedad, String titulo, String fecha, String descripcion) {
        super();
        this.icono = icono;
        this.header = header;
        this.titulo = titulo;
        this.fecha = fecha;
        this.antiguedad = antiguedad;
        this.descripcion=descripcion;
    }
}
