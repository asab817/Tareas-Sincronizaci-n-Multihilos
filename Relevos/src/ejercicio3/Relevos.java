package ejercicio3;

// Clase que gestiona la sincronización de la carrera
class Testigo {
    private int turno = 1; // Indica qué corredor tiene el turno (1, 2, 3 o 4)
    private boolean carreraComenzada = false; // Bandera para la salida inicial

    // Método para esperar a que sea mi turno y la carrera haya comenzado
    public synchronized void esperarTurno(int idCorredor) {
        // Mientras NO haya empezado la carrera O NO sea mi turno, espero
        while (!carreraComenzada || turno != idCorredor) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para dar la salida inicial
    public synchronized void darSalida() {
        carreraComenzada = true;
        System.out.println("Doy la salida!!");
        notifyAll(); // Despierta a todos para que verifiquen su turno
    }

    // Método para pasar el testigo al siguiente corredor
    public synchronized void pasarTestigo(int siguienteTurno) {
        this.turno = siguienteTurno;
        notifyAll(); // Avisa a todos los hilos que el turno ha cambiado
    }
}

// Clase Corredor (Hilo)
class Corredor extends Thread {
    private int id; // Identificador del corredor (1, 2, 3, 4)
    private Testigo testigo; // Objeto compartido para sincronización

    public Corredor(int id, Testigo testigo) {
        this.id = id;
        this.testigo = testigo;
    }

    @Override
    public void run() {
        // 1. Esperar a que den la salida y sea mi turno
        testigo.esperarTurno(id);

        // 2. Correr
        System.out.println("Soy el hilo " + id + ", corriendo...");
        try {
            // Simula que corre durante 2 segundos
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Pasar el testigo al siguiente o terminar
        if (id < 4) {
            System.out.println("Terminé. Paso el testigo al hilo " + (id + 1));
            testigo.pasarTestigo(id + 1); // Pasa el turno al siguiente ID
        } else {
            System.out.println("Terminé!"); // El último solo avisa que terminó
        }
    }
}

// Clase Principal
public class Relevos {
    public static void main(String[] args) {
        // Crear el objeto compartido para sincronización
        Testigo testigo = new Testigo();

        // Array para guardar referencias a los hilos
        Corredor[] corredores = new Corredor[4];

        // Crear y lanzar los 4 hilos. Se quedarán esperando.
        for (int i = 0; i < 4; i++) {
            // IDs del 1 al 4
            corredores[i] = new Corredor(i + 1, testigo);
            corredores[i].start();
        }

        System.out.println("Todos los hilos creados.");

        // Pequeña pausa opcional para asegurar que todos los hilos han llegado al wait()
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Dar la salida
        testigo.darSalida();

        // Esperar a que todos terminen para mostrar el mensaje final
        for (int i = 0; i < 4; i++) {
            try {
                corredores[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Todos los hilos terminaron.");
    }
}
