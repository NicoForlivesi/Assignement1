
public class Worker {

    public static void main(String[] args) {
        try {
            String workerName = "Worker-" + ProcessHandle.current().pid(); // Uso il pid per dare un nome univoco
            // ad ogni worker automaticamente, potendo runnare lo stesso file

            Middleware mutex = new Middleware(workerName); // Tutta la logica di rabbitmq dentro al middleware, qui infatti
            // non serve neanche fare l'import

            for (int i = 1; i <= 10; i++) {
                System.out.println("[" + workerName + "] Eseguo lavoro non critico...");
                Thread.sleep((long) (Math.random() * 5000 + 2000));

                mutex.enterCriticalSection();
                System.out.println("[" + workerName + "] Sto usando la risorsa condivisa (Iterazione " + i + ")");
                Thread.sleep(1500);
                mutex.exitCriticalSection();
            }

            System.out.println("[" + workerName + "] Termino.");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}