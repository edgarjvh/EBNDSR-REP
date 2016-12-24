package vistas;

@SuppressWarnings("ALL")
public class SpinnerItems {
    private int registrado;
    private int idDocente;
    private String apellidosDoc;
    private String nombresDoc;
    private int idAlumno;
    private String apellidosAl;
    private String nombresAl;
    private String imagen;

    public SpinnerItems(int registrado, int idDocente, String apellidosDoc, String nombresDoc, int idAlumno, String apellidosAl, String nombresAl, String imagen){
        this.registrado = registrado;
        this.idDocente = idDocente;
        this.apellidosDoc = apellidosDoc;
        this.nombresDoc = nombresDoc;
        this.idAlumno = idAlumno;
        this.apellidosAl = apellidosAl;
        this.nombresAl = nombresAl;
        this.imagen = imagen;
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

    public String getApellidosDoc() {
        return apellidosDoc;
    }

    public void setApellidosDoc(String apellidosDoc) {
        this.apellidosDoc = apellidosDoc;
    }

    public String getNombresDoc() {
        return nombresDoc;
    }

    public void setNombresDoc(String nombresDoc) {
        this.nombresDoc = nombresDoc;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getApellidosAl() {
        return apellidosAl;
    }

    public void setApellidosAl(String apellidosAl) {
        this.apellidosAl = apellidosAl;
    }

    public String getNombresAl() {
        return nombresAl;
    }

    public void setNombresAl(String nombresAl) {
        this.nombresAl = nombresAl;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
