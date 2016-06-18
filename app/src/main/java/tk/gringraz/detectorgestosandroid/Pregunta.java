package tk.gringraz.detectorgestosandroid;

/**
 * Creado por GRINGRAZ el 17-06-2016.
 */
public class Pregunta {
    private String pregunta;
    private String resultado;

    public Pregunta(String pregunta, String resultado) {
        this.pregunta = pregunta;
        this.resultado = resultado;
    }

    public Pregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }
}
