package vistas;

import java.util.Date;

public class lvMensajesItems implements Comparable<lvMensajesItems> {
    private int tempId = 0;
    private int idMensaje;
    private int idRepresentante;
    private int idDocente;
    private int via;
    private int status;
    private long fechaHora;
    private String mensaje;
    private Date fecha;

    public lvMensajesItems(int tempId, int idMensaje, int via, int idDocente, int idRepresentante, int status, long fechaHora, String mensaje){
        this.tempId = tempId;
        this.idMensaje = idMensaje;
        this.via = via;
        this.idDocente = idDocente;
        this.idRepresentante = idRepresentante;
        this.status = status;
        this.fechaHora = fechaHora;
        this.mensaje = mensaje;
        this.fecha = new Date(fechaHora);
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public int getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(int idMensaje) {
        this.idMensaje = idMensaje;
    }

    public int getIdRepresentante() {
        return idRepresentante;
    }

    public void setIdRepresentante(int idRepresentante) {
        this.idRepresentante = idRepresentante;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
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
