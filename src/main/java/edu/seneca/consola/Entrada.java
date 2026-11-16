package edu.seneca.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Entrada {

    private Scanner scanner;

    public Entrada(Scanner scanner) {
        this.scanner = scanner;
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine().trim();
    }

    public String leerTexto(String mensaje) {
        String texto = leerLinea(mensaje);

        while (texto.isEmpty()) {
            System.out.println("\tEste dato es obligatorio");
            texto = leerLinea(mensaje);
        }
        return texto;
    }

    public String leerOpcional(String mensaje) {
        String texto = leerLinea(mensaje + " (Enter para omitir)");

        if (texto.isEmpty()) {
            return null;
        }
        return texto;
    }

    public int leerEntero(String mensaje, int minimo, int maximo) {
        int valor = 0;
        boolean valido = false;

        while (!valido) {
            String texto = leerLinea(mensaje);

            try {
                valor = Integer.parseInt(texto);

                if (valor >= minimo && valor <= maximo) {
                    valido = true;
                } else {
                    System.out.println("\tDebe estar entre " + minimo + " y " + maximo);
                }
            } catch (NumberFormatException e) {
                System.out.println("\tEscriba un numero entero");
            }
        }
        return valor;
    }

    public double leerDecimal(String mensaje, double minimo, double maximo) {
        double valor = 0;
        boolean valido = false;

        while (!valido) {
            String texto = leerLinea(mensaje).replace(',', '.');

            try {
                valor = Double.parseDouble(texto);

                if (!Double.isNaN(valor) && valor >= minimo && valor <= maximo) {
                    valido = true;
                } else {
                    System.out.println("\tDebe estar entre " + minimo + " y " + maximo);
                }
            } catch (NumberFormatException e) {
                System.out.println("\tEscriba un numero");
            }
        }
        return valor;
    }

    public LocalDate leerFecha(String mensaje) {
        LocalDate fecha = null;

        while (fecha == null) {
            String texto = leerLinea(mensaje + " (AAAA-MM-DD)");

            try {
                fecha = LocalDate.parse(texto);
            } catch (Exception e) {
                System.out.println("\tFecha no valida. ejemplo: 2026-12-05");
            }
        }
        return fecha;
    }

    public LocalTime leerHora(String mensaje) {
        LocalTime hora = null;

        while (hora == null) {
            String texto = leerLinea(mensaje + " (HH:MM)");

            try {
                hora = LocalTime.parse(texto);
            } catch (Exception e) {
                System.out.println("\tHora no valida. ejemplo: 08:30");
            }
        }
        return hora;
    }

    public boolean leerSiNo(String mensaje) {
        String texto = leerLinea(mensaje + " (s/n)").toLowerCase();

        while (!texto.equals("s") && !texto.equals("si") && !texto.equals("n") && !texto.equals("no")) {
            System.out.println("\tResponda s o n");
            texto = leerLinea(mensaje + " (s/n)").toLowerCase();
        }
        return texto.equals("s") || texto.equals("si");
    }

    public String elegir(String mensaje, String[] opciones) {
        System.out.println(mensaje + ":");

        for (int i = 0; i < opciones.length; i++) {
            System.out.println("\t" + (i + 1) + ". " + opciones[i]);
        }

        int opcion = leerEntero("Opcion", 1, opciones.length);
        return opciones[opcion - 1];
    }

    public String elegirOpcional(String mensaje, String[] opciones) {
        System.out.println(mensaje + ":");
        System.out.println("\t0. Cualquiera");

        for (int i = 0; i < opciones.length; i++) {
            System.out.println("\t" + (i + 1) + ". " + opciones[i]);
        }

        int opcion = leerEntero("Opcion", 0, opciones.length);

        if (opcion == 0) {
            return null;
        }
        return opciones[opcion - 1];
    }
}
