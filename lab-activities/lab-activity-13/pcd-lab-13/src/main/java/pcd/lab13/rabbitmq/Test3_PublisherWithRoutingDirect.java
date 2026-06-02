package pcd.lab13.rabbitmq;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Test3_PublisherWithRoutingDirect {

  private static final String EXCHANGE_NAME = "direct_logs";

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
    	Channel channel = connection.createChannel()){

        /*
         * Dichiara un exchange di tipo "direct".
         *
         * Un direct exchange recapita il messaggio SOLO alle code
         * che hanno un binding con una routing key esattamente uguale
         * a quella usata dal publisher.
         */
    	channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // Routing key scelta manualmente (potrebbe venire dagli argomenti)
    	String routingKey = "tag-1"; // getSeverity(argv);
    	String message = "hello2"; // getMessage(argv);

        /*
         * Pubblica il messaggio sull'exchange "direct_logs".
         * La routing key è fondamentale: determina quali code lo riceveranno.
         */
    	channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
    	System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

    }
  }

  private static String getSeverity(String[] strings){
    if (strings.length < 1)
    	    return "tag-1";
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

