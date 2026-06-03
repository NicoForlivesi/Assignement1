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
    // non ci siano istanti di tempo in cui più di un worker è in sezione critica

    public Middleware(String workerName) throws Exception {
        this.workerName = workerName;

        // Configurazione interna nascosta dal worker
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null); // Solo il primo worker dichiara la coda, essendo già
        // dihiarata gli altri non fanno niente

         /**
          Assicura che il token non venga "prenotato" da un worker che non è ancora pronto a processarlo, facendo
          qualche test ho verificato che sfruttando basicQos si mantiene una maggiore fairness. E' importante sottolineare
          che non è questa istruzione che garantisce un unico token nel sistema distribuito, è la SystemInit che ne pubblica
          solo uno e ogni worker lo prende e lo restituisce, garantendo che il token disponibile in un certo istante di tempo
          sia sempre 1 (se nessun worker è in sezione critica) o 0 (se un worker è in sezione critica).
          */
        channel.basicQos(1);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            try {
                tokenSlot.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    public void enterCriticalSection() throws Exception {
        System.out.println("[" + workerName + "] Attendo il token...");
        tokenSlot.take(); // I worker si mettono in coda qui finchè chi ha il token non fa la put
        System.out.println("[" + workerName + "] => Entro nella sezione critica (t = " + LocalTime.now().format(FMT) + ")");
    }

    public void exitCriticalSection() throws Exception {
        String message = "TOKEN";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println("[" + workerName + "] => Esco dalla sezione critica (t = " + LocalTime.now().format(FMT) + ")");
    }
}