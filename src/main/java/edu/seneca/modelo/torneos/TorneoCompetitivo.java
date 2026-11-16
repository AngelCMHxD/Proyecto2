package edu.seneca.modelo.torneos;

import java.time.LocalDate;

public class TorneoCompetitivo extends Torneo {

    private static final long serialVersionUID = 1L;

    private double tarifa;
    private double porcentajePremio;
    private double premioEfectivo;
    private InscripcionTorneo beneficiarioPremio;

    public TorneoCompetitivo(String id, String nombre, String deporte, String categoria, Modalidad modalidad, int cupos, LocalDate fecha, double tarifa, double porcentajePremio) {
        super(id, nombre, deporte, categoria, modalidad, cupos, fecha);
        this.tarifa = tarifa;
        this.porcentajePremio = porcentajePremio;
    }

    public TipoTorneo getTipo() {
        return TipoTorneo.COMPETITIVO;
    }

    public double getTarifa() {
        return tarifa;
    }

    protected void entregarPremio() {
        premioEfectivo = getTotalRecaudado() * porcentajePremio;

        for (InscripcionTorneo inscripcion : posicionesFinales) {
            if (inscripcion.tieneDerechoAPremioEfectivo()) {
                beneficiarioPremio = inscripcion;
                return;
            }
        }
    }

    public String describirPremio() {
        if (estado != EstadoTorneo.FINALIZADO) {
            return null;
        }
        if (beneficiarioPremio == null) {
            return "Premio de " + premioEfectivo + " sin entregar: ningun participante tiene derecho";
        }
        return "Premio en efectivo de " + premioEfectivo + " para " + beneficiarioPremio.getNombre();
    }

    public double getPorcentajePremio() {
        return porcentajePremio;
    }

    public double getPremioEfectivo() {
        return premioEfectivo;
    }

    public InscripcionTorneo getBeneficiarioPremio() {
        return beneficiarioPremio;
    }
}
