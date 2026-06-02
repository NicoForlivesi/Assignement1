package pcd.lab13.rabbitmq;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Test4_PublisherWithRoutingTopic {

  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
	try (Connection connection = factory.newConnection();
		Channel channel = connection.createChannel()) {

        /*
         * Dichiara un exchange di tipo "topic".
         *
         * Un topic exchange permette routing basato su pattern.
         * Le routing key sono stringhe con parole separate da punti, es:
         *   "kern.critical"
         *   "auth.info"
         *   "sensor.temperature.kitchen"
         *
         * I subscriber possono usare wildcard:
         *   *  → una sola parola
         *   #  → zero o più parole
         */
		channel.exchangeDeclare(EXCHANGE_NAME, "topic");

		String routingKey = getRouting(argv);
		String message = getMessage(argv);

        /*
         * Pubblica il messaggio sull'exchange "topic_logs".
         * La routing key determina quali code lo riceveranno,
         * in base ai pattern dei binding.
         */
		channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
	}
  }

  private static String getRouting(String[] strings){
    if (strings.length < 1)
    	    return "anonymous.info";
    return strings[0];
  }

  private static String getMessage(String[] strings){
    if (strings.length < 2)
    	    return "Hello World!";
    return joinStrings(strings, " ", 1);
  }

  private static String joinStrings(String[] strings, String delimiter, int startIndex) {
    int length = strings.length;
    if (length == 0 ) return "";
    if (length < startIndex ) return "";
    StringBuilder words = new StringBuilder(strings[startIndex]);
    for (int i = startIndex + 1; i < length; i++) {
        words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}

