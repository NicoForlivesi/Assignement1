import com.rabbitmq.client.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {

    private static final String QUEUE_NAME = "mutex_token";
    private final Channel channel;
    private final String workerName;
    private final BlockingQueue<String> tokenSlot = new LinkedBlockingQueue<>(1);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS"); // giusto per verificare che
    // non ci siano istanti di tempo in cui più di un worker è in sezione critica nei logs
    private String consumerTag;

    public Middleware(String workerName) throws Exception {
        this.workerName = workerName;

        // Configurazione interna nascosta dal worker che sfrutta l'API senza sapere "cosa c'è sotto"
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null); // Solo il primo worker dichiara la coda, essendo già
        // dihiarata gli altri non fanno niente

        /**
         * E' importante sottolineare che non è questa istruzione che garantisce un unico token nel sistema distribuito,
         * è la SystemInit che ne pubblica solo uno e ogni worker lo prende e lo restituisce tramite RabbitMQ e la sua callback,
         * garantendo che il token disponibile in un certo istante di tempo sia sempre 1 (se nessun worker è in sezione
         * critica) o 0(se un workerè in sezione critica).
         */
        channel.basicQos(1);

//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            try {
//                tokenSlot.put(message);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        };
//        COSI C'E' POSSIBILE STARVATION!!!! (Token sempre "prenotato")
//        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    // tokenSlot funge da "ponte" thread-safe tra il thread interno di RabbitMQ (che esegue la deliverCallback
    // e chiama .put quando il token arriva se c'è qualcuno registrato) e il thread del Worker (che si registra
    // come consumer e chiama .take in enterCriticalSection).
    // .take è bloccante: il worker si sospende finché RabbitMQ non chiama .put.
    /**
     * La BlockingQueue si comporta localmente come un semaforo mutex, ma il meccanismo di mutua esclusione reale
     * è distribuito, è la coda RabbitMQ con il token circolante che garantisce che un solo worker alla volta possa
     * essere in sezione critica, anche su macchine fisicamente separate.
     * */
    public void enterCriticalSection() throws Exception {
        System.out.println("[" + workerName + "] Attendo il token...");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            try {
                tokenSlot.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        consumerTag = channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        tokenSlot.take(); // I worker si mettono in coda qui finchè chi ha il token non lo rilascia mandando il messaggio
        // "TOKEN" sulla coda.
        System.out.println("[" + workerName + "] => Entro nella sezione critica (t = " + LocalTime.now().format(FMT) + ")");
    }

    public void exitCriticalSection() throws Exception {
        channel.basicCancel(consumerTag);
        String message = "TOKEN";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8")); // Se c'è
        // qualcun'altro già registrato come basicConsume allora questo messaggio viene subito consumato ed eseguita
        // la callback associata da RabbitMQ che mette il token sull'altro processo, se non c'è nessuno registrato
        // perchè nessuno ha fatto richiesta per entrare in sezione critica comunque il messaggio non viene perso,
        // viene mantenuto nella coda in uno stato di attesa aspettando che qualcuno si registri come basicConsume.
        System.out.println("[" + workerName + "] => Esco dalla sezione critica (t = " + LocalTime.now().format(FMT) + ")");
    }
}