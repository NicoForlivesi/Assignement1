package pcd.lab13.rabbitmq;
import com.rabbitmq.client.*;

public class Test2_Subscriber {
  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {
	  
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    /*
     * Dichiara lo stesso exchange "fanout".
     * Deve esistere sia lato publisher che lato subscriber.
     */
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

    /*
     * Crea una coda anonima, con nome generato automaticamente.
     * È una coda:
     *   - non durevole
     *   - esclusiva
     *   - auto-delete
     *
     * Perfetta per i subscriber "temporanei".
     */
    String queueName = channel.queueDeclare().getQueue();

    /*
     * Collega (bind) la coda anonima all'exchange "logs".
     * Nei fanout la routing key è ignorata → si usa stringa vuota.
     */
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
    };

    /*
     * Consuma i messaggi dalla coda anonima.
     * autoAck = true → ack automatici.
     */
    channel.basicConsume(queueName, deliverCallback, t -> {});
  }
}

