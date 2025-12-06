package ejercicio2;

import java.util.Random;

/**
 * Clase Buffer
 * Gestiona el acceso concurrente mediante métodos sincronizados.
 */
class Buffer {
    private char[] almacenamiento;
    private int indiceSiguiente; // Apunta a la siguiente posición libre
    private boolean estaVacio;
    private boolean estaLleno;
    private final int CAPACIDAD = 6;

    public Buffer() {
        this.almacenamiento = new char[CAPACIDAD];
        this.indiceSiguiente = 0;
        this.estaVacio = true;
        this.estaLleno = false;
    }

    /**
     * Método sincronizado para depositar caracteres (Productor).
     * @param c Caracter a depositar.
     */
    public synchronized void producir(char c) {
        while (estaLleno) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Productor interrumpido: " + e.getMessage());
            }
        }

        // Inserción LIFO (llenado de izquierda a derecha)
        almacenamiento[indiceSiguiente] = c;
        indiceSiguiente++;

        System.out.println("Depositado el caracter " + c + " en el buffer");

        // Actualización de estado
        estaVacio = false;
        if (indiceSiguiente == CAPACIDAD) {
            estaLleno = true;
        }

        notifyAll();
    }

    /**
     * Método sincronizado para recoger caracteres (Consumidor).
     * @return Caracter recogido.
     */
    public synchronized char consumir() {
        while (estaVacio) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Consumidor interrumpido: " + e.getMessage());
            }
        }

        // Extracción LIFO (recogida de derecha a izquierda)
        indiceSiguiente--;
        char c = almacenamiento[indiceSiguiente];

        System.out.println("Recogido el caracter " + c + " del buffer");

        // Actualización de estado
        estaLleno = false;
        if (indiceSiguiente == 0) {
            estaVacio = true;
        }

        notifyAll();
        return c;
    }
}

/**
 * Hilo Productor.
 */
class Productor extends Thread {
    private Buffer buffer;
    private final String alfabeto = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
    private Random aleatorio;

    public Productor(Buffer b) {
        this.buffer = b;
        this.aleatorio = new Random();
    }

    @Override
    public void run() {
        for (int i = 0; i < 15; i++) {
            char c = alfabeto.charAt(aleatorio.nextInt(alfabeto.length()));
            buffer.producir(c);

            try {
                Thread.sleep(aleatorio.nextInt(101)); // 0 a 100 ms
            } catch (InterruptedException e) {
                System.err.println("Error en espera del productor.");
            }
        }
    }
}

/**
 * Hilo Consumidor.
 */
class Consumidor extends Thread {
    private Buffer buffer;
    private Random aleatorio;

    public Consumidor(Buffer b) {
        this.buffer = b;
        this.aleatorio = new Random();
    }

    @Override
    public void run() {
        for (int i = 0; i < 15; i++) {
            buffer.consumir();

            try {
                Thread.sleep(aleatorio.nextInt(1001)); // 0 a 1000 ms
            } catch (InterruptedException e) {
                System.err.println("Error en espera del consumidor.");
            }
        }
    }
}

/**
 * Clase Principal Ejercicio 2.
 */
public class Ejercicio2 {
    public static void main(String[] args) {
        Buffer bufferCompartido = new Buffer();

        Productor p = new Productor(bufferCompartido);
        Consumidor c = new Consumidor(bufferCompartido);

        p.start();
        c.start();

        try {
            p.join();
            c.join();
            System.out.println("--- Fin de la simulación ---");
        } catch (InterruptedException e) {
            System.err.println("Hilo principal interrumpido.");
        }
    }
}