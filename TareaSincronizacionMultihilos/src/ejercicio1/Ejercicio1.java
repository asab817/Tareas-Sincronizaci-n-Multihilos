package ejercicio1;

import java.util.Random;

/**
 * Clase Contador
 * Mantiene el valor y controla la sincronización de turnos para una alternancia estricta.
 */
class Contador {
    private int valor;
    // Variable para controlar el turno: true = le toca a Sumador, false = le toca a Restador.
    private boolean turnoSumador = true;

    public Contador(int valorInicial) {
        this.valor = valorInicial;
    }

    /**
     * Incrementa el contador. Método sincronizado.
     * Si no es el turno del sumador, el hilo se bloquea.
     */
    public synchronized void incrementa() {
        while (!turnoSumador) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Hilo interrumpido en incrementa: " + e.getMessage());
            }
        }

        // Sección crítica
        valor++;
        System.out.println(Thread.currentThread().getName() + " incrementa. Valor actual: " + valor);

        // Cambio de turno y notificación
        turnoSumador = false;
        notifyAll();
    }

    /**
     * Decrementa el contador. Método sincronizado.
     * Si es el turno del sumador, el hilo restador se bloquea.
     */
    public synchronized void decrementa() {
        while (turnoSumador) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Hilo interrumpido en decrementa: " + e.getMessage());
            }
        }

        // Sección crítica
        valor--;
        System.out.println(Thread.currentThread().getName() + " decrementa. Valor actual: " + valor);

        // Cambio de turno y notificación
        turnoSumador = true;
        notifyAll();
    }

    public int getValor() {
        return valor;
    }
}

/**
 * Hilo Sumador. Hereda de Thread.
 */
class Sumador extends Thread {
    private Contador contador;
    private Random aleatorio;

    public Sumador(String nombre, Contador c) {
        super(nombre);
        this.contador = c;
        this.aleatorio = new Random();
    }

    @Override
    public void run() {
        for (int i = 0; i < 300; i++) {
            contador.incrementa();
            try {
                // Espera aleatoria entre 50ms y 150ms
                Thread.sleep(aleatorio.nextInt(101) + 50);
            } catch (InterruptedException e) {
                System.err.println("Sumador interrumpido.");
            }
        }
    }
}

/**
 * Hilo Restador. Implementa Runnable.
 */
class Restador implements Runnable {
    private Contador contador;
    private String nombre;
    private Random aleatorio;

    public Restador(String nombre, Contador c) {
        this.nombre = nombre;
        this.contador = c;
        this.aleatorio = new Random();
    }

    @Override
    public void run() {
        // Asignar nombre al hilo actual
        Thread.currentThread().setName(this.nombre);

        for (int i = 0; i < 300; i++) {
            contador.decrementa();
            try {
                // Espera aleatoria entre 50ms y 150ms
                Thread.sleep(aleatorio.nextInt(101) + 50);
            } catch (InterruptedException e) {
                System.err.println("Restador interrumpido.");
            }
        }
    }
}

/**
 * Clase Principal Ejercicio 1.
 */
public class Ejercicio1 {
    public static void main(String[] args) {
        // Inicialización del recurso compartido
        Contador cont = new Contador(100);

        System.out.println("--- Inicio del programa ---");
        System.out.println("Valor inicial: " + cont.getValor());

        // Creación de hilos
        Sumador hiloSumador = new Sumador("HILO-SUMADOR", cont);
        Thread hiloRestador = new Thread(new Restador("HILO-RESTADOR", cont));

        // Inicio de ejecución
        hiloSumador.start();
        hiloRestador.start();

        // Espera a la finalización de los hilos
        try {
            hiloSumador.join();
            hiloRestador.join();
        } catch (InterruptedException e) {
            System.err.println("Hilo principal interrumpido.");
        }

        System.out.println("--- Fin del programa ---");
        System.out.println("Valor final: " + cont.getValor());
    }
}