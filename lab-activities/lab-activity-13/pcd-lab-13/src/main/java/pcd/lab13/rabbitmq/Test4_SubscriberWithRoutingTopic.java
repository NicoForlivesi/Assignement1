package pcd.lab13.rabbitmq;
import com.rabbitmq.client.*;

public class Test4_SubscriberWithRoutingTopic {

  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    /*
     * Dichiara lo stesso exchange "topic".
     */
    channel.exchangeDeclare(EXCHANGE_NAME, "topic");

    /*
     * Crea una coda anonima temporanea.
     * Perfetta per un subscriber che vive solo finché il programma è attivo.
     */
    String queueName = channel.queueDeclare().getQueue();

    if (argv.length < 1) {
      /*
       * Il subscriber deve passare almeno un binding key come argomento.
       * Esempi validi:
       *   "*.info"
       *   "kern.*"
       *   "auth.#"
       *   "#"
       */
      System.err.println("Usage: ReceiveLogsTopic [binding_key]...");
      System.exit(1);
    }

    for (String bindingKey : argv) {
      /*
       * Per ogni binding key passata, colleghiamo la coda all'exchange.
       * Ogni binding key è un pattern.
       */
      channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
    }

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
      /*
       * Mostra la routing key effettiva del messaggio ricevuto.
       */
        System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    // AutoAck attivo
    channel.basicConsume(queueName, true, deliverCallback, t -> {});
  }
}

