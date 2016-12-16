package vistas;

import java.util.Date;

public class lvMensajesItems implements Comparable<lvMensajesItems> {
    private int idMensaje;
    private int via;
    private int status;
    private long fechaHora;
    private String mensaje;
    private Date fecha;

    public lvMensajesItems(int idMensaje, int via, int status, long fechaHora, String mensaje){
        this.idMensaje = idMensaje;
        this.via = via;
        this.status = status;
        this.fechaHora = fechaHora;
        this.mensaje = mensaje;
        this.fecha = new Date(fechaHora);
    }

    public int getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public int getVia() {
        return via;
    }

    public void setVia(int via) {
        this.via = via;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(long fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    private Date getFecha() {
        return fecha;
    }

    private void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public int compareTo(lvMensajesItems o) {
        return getFecha().compareTo(getFecha());
    }
}
