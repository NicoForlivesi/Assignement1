package pcd.lab13.rabbitmq;
import com.rabbitmq.client.*;

public class Test3_SubscriberWithRoutingDirect {

  private static final String EXCHANGE_NAME = "direct_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    /*
     * Dichiara lo stesso exchange "direct".
     * Deve essere identico a quello del publisher.
     */
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    /*
     * Crea una coda anonima temporanea.
     * Perfetta per un subscriber che vive solo finché il programma è attivo.
     */
    String queueName = channel.queueDeclare().getQueue();

    /*
     * BINDING DELLA CODA ALL'EXCHANGE
     *
     * Qui il subscriber dice:
     *   "Voglio ricevere messaggi con routing key = 'tag-1'"
     *   "Voglio ricevere messaggi con routing key = 'tag-2'"
     *
     * Quindi questa coda riceverà SOLO quei messaggi.
     */
    channel.queueBind(queueName, EXCHANGE_NAME, "tag-1");
    channel.queueBind(queueName, EXCHANGE_NAME, "tag-2");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      /*
       * delivery.getEnvelope().getRoutingKey()
       * → mostra quale routing key ha causato la consegna del messaggio.
       */
    };

    /*
     * Consuma i messaggi dalla coda anonima.
     * autoAck = true → ack automatici.
     */
    channel.basicConsume(queueName, true, deliverCallback, t -> {});
  }
}

