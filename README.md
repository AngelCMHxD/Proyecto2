En `docs/` esta el .pdf con toda la info y las imagenes de los diagramas.

Usa Java 17

| clase | acceso |
|---|---|
| `edu.seneca.consola.ConsolaAdministrador` | administrador |
| `edu.seneca.consola.ConsolaSocio` | socios, estudiantes y participantes externos |
| `edu.seneca.consola.ConsolaEmpleado` | entrenadores y fisioterapeutas |

## Datos de prueba

Los datos estan en `datos/club.dat`, las consolas lo cargan al iniciar y lo actualizan al salir

si no existen, el programa lo crea con `GeneradorDatosPrueba`

## acceso

### Administrador

| Login | Password | Usuario |
|---|---|---|
| admin | admin123 | Marta Leon |

### Socio

| Login | Password | Usuario |
|---|---|---|
| laura | laura123 | Laura Gomez, nivel intermedio |
| mateo | mateo123 | Mateo Rojas, nivel avanzado, tiene un bono ganado en un torneo |
| sofia | sofia123 | Sofia Herrera, nivel principiante |
| daniel | daniel123 | Daniel Castro, nivel intermedio |
| valentina | valentina123 | Valentina Ruiz, nivel avanzado |
| felipe | felipe123 | Felipe Mora, nivel intermedio, del equipo FUT-MAY |

### Empleado

| Login | Password | Usuario |
|---|---|---|
| carlos | carlos123 | Carlos Mejia, entrenador |
| paula | paula123 | Paula Torres, entrenadora |
| andres | andres123 | Andres Pardo, fisioterapeuta |

# declaracion de IA
se utilizó para la generacion de datos de prueba en `GeneradorDatosPrueba.java`, lo cual esta permitido bajo la guía de la actividad (datos mock-up). la logica sí fue creada a mano