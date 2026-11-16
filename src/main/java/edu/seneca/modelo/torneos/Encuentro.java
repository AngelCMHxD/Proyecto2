package edu.seneca.modelo.torneos;

import java.io.Serializable;

public class Encuentro implements Serializable {

    private static final long serialVersionUID = 1L;

    private InscripcionTorneo participanteA;
    private InscripcionTorneo participanteB;
    private String marcador;
    private InscripcionTorneo ganador;

    public Encuentro(InscripcionTorneo participanteA, InscripcionTorneo participanteB) {
        this.participanteA = participanteA;
        this.participanteB = participanteB;
    }

    public void registrarResultado(String marcador, InscripcionTorneo ganador) throws Exception {
        if (marcador == null || marcador.trim().isEmpty()) {
            throw new Exception("Falta el marcador del encuentro");
        }
        if (ganador != participanteA && ganador != participanteB) {
            throw new Exception("El ganador debe ser uno de los dos participantes del encuentro");
        }

        this.marcador = marcador.trim();
        this.ganador = ganador;
    }

    public boolean tieneResultado() {
        return ganador != null;
    }

    public InscripcionTorneo getParticipanteA() {
        return participanteA;
    }

    public InscripcionTorneo getParticipanteB() {
        return participanteB;
    }

    public String getMarcador() {
        return marcador;
    }

    public InscripcionTorneo getGanador() {
        return ganador;
    }
}
