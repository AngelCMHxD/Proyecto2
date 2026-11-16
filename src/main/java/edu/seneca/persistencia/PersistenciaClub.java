package edu.seneca.persistencia;

import edu.seneca.ClubDeportivoSeneca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistenciaClub {

    public static final String ARCHIVO = "datos/club.dat";

    public static void guardar(ClubDeportivoSeneca club) throws Exception {
        guardar(club, ARCHIVO);
    }

    public static ClubDeportivoSeneca cargar() throws Exception {
        return cargar(ARCHIVO);
    }

    public static void guardar(ClubDeportivoSeneca club, String ruta) throws Exception {
        new File(ruta).getAbsoluteFile().getParentFile().mkdirs();

        ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta));
        salida.writeObject(club);
        salida.close();
    }

    public static ClubDeportivoSeneca cargar(String ruta) throws Exception {
        ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta));
        ClubDeportivoSeneca club = (ClubDeportivoSeneca) entrada.readObject();
        entrada.close();
        return club;
    }

    public static ClubDeportivoSeneca cargarOCrear() throws Exception {
        if (!new File(ARCHIVO).exists()) {
            GeneradorDatosPrueba.generar();
        }
        return cargar();
    }
}
