package edu.seneca.modelo.torneos;

public enum EstadoTorneo {
    INSCRIPCIONES_ABIERTAS("Inscripciones abiertas"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado");

    private String texto;

    EstadoTorneo(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    public static EstadoTorneo desdeFiltro(String texto) throws Exception {
        if (texto != null) {
            String limpio = texto.trim();

            if (limpio.equalsIgnoreCase(EN_CURSO.texto)) {
                return EN_CURSO;
            }
            if (limpio.equalsIgnoreCase(FINALIZADO.texto)) {
                return FINALIZADO;
            }
        }
        throw new Exception("Estado no valido: " + texto + ". use 'En curso' o 'Finalizado'");
    }
}
