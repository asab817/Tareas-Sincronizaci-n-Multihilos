package ejercicio4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Clase auxiliar para gestionar la entrada de datos por teclado.
 * Utiliza BufferedReader, el método clásico para leer entradas en Java.
 */
class Entrada {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static int leerEntero(String mensaje) {
        int numero = 0;
        boolean leido = false;

        while (!leido) {
            try {
                System.out.print(mensaje);
                String linea = reader.readLine();
                numero = Integer.parseInt(linea);
                leido = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número entero válido.");
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            }
        }
        return numero;
    }
}

/**
 * Clase Resultados
 */
class Resultados {
    private int gananciasTotales = 0;

    // Método sincronizado para sumar ganancias de forma atómica
    public synchronized void sumarGanancia(int cantidad) {
        this.gananciasTotales += cantidad;
    }

    public int getGananciasTotales() {
        return this.gananciasTotales;
    }
}

/**
 * Clase Caja
 */
class Caja {
    private int idCaja;
    private Resultados resultadosGlobales;

    public Caja(int id, Resultados resultados) {
        this.idCaja = id;
        this.resultadosGlobales = resultados;
    }

    // Método sincronizado
    public synchronized void procesarCompra(Cliente cliente, int pago) {
        System.out.println("Caja " + idCaja + " atendiendo al Cliente " + cliente.getIdCliente() +
                " -> Pago realizado: " + pago + " euros");

        try {
            // Simular tiempo de atención
            Thread.sleep(new Random().nextInt(400) + 100);
        } catch (InterruptedException e) {
            System.err.println("Error en la caja " + idCaja);
        }

        // Acumular ganancia de forma segura
        resultadosGlobales.sumarGanancia(pago);
    }
}

/**
 * Clase Cliente
 */
class Cliente extends Thread {
    private int idCliente;
    private Caja[] cajasDisponibles;
    private Random generadorAleatorio = new Random();

    public Cliente(int id, Caja[] cajas) {
        this.idCliente = id;
        this.cajasDisponibles = cajas;
    }

    public int getIdCliente() {
        return idCliente;
    }

    @Override
    public void run() {
        try {
            // 1. Simular tiempo comprando
            Thread.sleep(generadorAleatorio.nextInt(1000) + 500);

            // 2. Elegir caja aleatoria
            int indiceCaja = generadorAleatorio.nextInt(cajasDisponibles.length);
            Caja cajaSeleccionada = cajasDisponibles[indiceCaja];

            // 3. Determinar pago aleatorio
            int importePago = generadorAleatorio.nextInt(100) + 5;

            // 4. Intentar pagar (se bloqueará si la caja está ocupada)
            cajaSeleccionada.procesarCompra(this, importePago);

        } catch (InterruptedException e) {
            System.err.println("Cliente " + idCliente + " interrumpido.");
        }
    }
}

/**
 * Clase Principal.
 */
public class SuperMarket {
    public static void main(String[] args) {
        int numClientes = 0;
        int numCajas = 0;

        // Intentar leer argumentos de línea de comandos
        if (args.length == 2) {
            try {
                numClientes = Integer.parseInt(args[0]);
                numCajas = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // Si fallan los argumentos, se ignoran y se pide por teclado
            }
        }

        // Si no hay argumentos válidos, usar la clase Entrada
        if (numClientes <= 0 || numCajas <= 0) {
            numClientes = Entrada.leerEntero("Introduce el número de clientes (M): ");
            numCajas = Entrada.leerEntero("Introduce el número de cajas (N): ");
        }

        System.out.println("\n--- APERTURA DEL SUPERMERCADO ---");
        System.out.println("Clientes: " + numClientes + " | Cajas: " + numCajas);
        System.out.println("---------------------------------");

        // Inicializar recursos compartidos
        Resultados resultados = new Resultados();
        Caja[] cajas = new Caja[numCajas];
        for (int i = 0; i < numCajas; i++) {
            cajas[i] = new Caja(i + 1, resultados);
        }

        // Crear y arrancar hilos de clientes
        Cliente[] clientes = new Cliente[numClientes];
        for (int i = 0; i < numClientes; i++) {
            clientes[i] = new Cliente(i + 1, cajas);
            clientes[i].start();
        }

        // Esperar a que todos terminen
        for (int i = 0; i < numClientes; i++) {
            try {
                clientes[i].join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al cliente " + (i + 1));
            }
        }

        // Mostrar resultados finales
        System.out.println("\n=================================");
        System.out.println("Supermercado cerrado.");
        System.out.println("Ganancias: " + resultados.getGananciasTotales() + " (Valor de la variable resultados).");
        System.out.println("=================================");
    }
}