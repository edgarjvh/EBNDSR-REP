package vistas;

public class lvPerfilItems {
    private String header;
    private String title;
    private String body;
    private String footer;

    public lvPerfilItems(String header, String title, String body, String footer){
        this.header = header;
        this.title = title;
        this.body = body;
        this.footer = footer;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
