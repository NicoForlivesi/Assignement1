import com.rabbitmq.client.*;

public class SystemInit {
    private final static String QUEUE_NAME = "mutex_token";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queuePurge(QUEUE_NAME); // Per pulizia da run precedenti

            String message = "TOKEN";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));

            System.out.println("Sistema inizializzato: Token inserito.");
        }
    }
}