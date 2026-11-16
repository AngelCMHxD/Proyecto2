package edu.seneca.modelo.torneos;

import edu.seneca.modelo.usuarios.Socio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TorneoRecreativo extends Torneo {

    private static final long serialVersionUID = 1L;

    private double valorBono;
    private List<String> sociosPremiados;

    public TorneoRecreativo(String id, String nombre, String deporte, String categoria, Modalidad modalidad, int cupos, LocalDate fecha, double valorBono) {
        super(id, nombre, deporte, categoria, modalidad, cupos, fecha);
        this.valorBono = valorBono;
        this.sociosPremiados = new ArrayList<>();
    }

    public TipoTorneo getTipo() {
        return TipoTorneo.RECREATIVO;
    }

    public double getTarifa() {
        return 0;
    }

    protected void entregarPremio() {
        for (Socio socio : getGanador().getSocios()) {
            socio.agregarBono(new BonoDescuento("B-" + id + "-" + socio.getId(), valorBono, nombre));
            sociosPremiados.add(socio.getNombre());
        }
    }

    public String describirPremio() {
        if (estado != EstadoTorneo.FINALIZADO) {
            return null;
        }
        if (sociosPremiados.isEmpty()) {
            return "Bono de " + valorBono + " sin entregar: el ganador no es socio";
        }

        String premiados = "";

        for (String socio : sociosPremiados) {
            if (!premiados.isEmpty()) {
                premiados += ", ";
            }

            premiados += socio;
        }
        return "Bono de " + valorBono + " para: " + premiados;
    }

    public double getValorBono() {
        return valorBono;
    }
}
