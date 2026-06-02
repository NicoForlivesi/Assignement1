package pcd.lab13.rabbitmq;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Test2_Publisher {

	  private static final String EXCHANGE_NAME = "logs";
	  private static final String NO_QUEUE_NAME = "";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
	try (Connection connection = factory.newConnection();
		Channel channel = connection.createChannel()) {

        /*
         * Dichiara un exchange di tipo "fanout".
         * Un fanout exchange:
         *   → ignora la routing key
         *   → invia ogni messaggio a TUTTE le code collegate (bindate)
         */
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

		String message = getMessage(argv);

        /*
         * Pubblica un messaggio sull'exchange "logs".
         * La routing key è vuota perché nei fanout non serve.
         */
		channel.basicPublish(EXCHANGE_NAME, NO_QUEUE_NAME, null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
	}
  }

    // Se non ci sono argomenti, manda un messaggio di default
  private static String getMessage(String[] strings){
    if (strings.length < 1)
    	    return "info: Hello World!";
    return joinStrings(strings, " ");
  }

  private static String joinStrings(String[] strings, String delimiter) {
    int length = strings.length;
    if (length == 0) return "";
    StringBuilder words = new StringBuilder(strings[0]);
    for (int i = 1; i < length; i++) {
        words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}

