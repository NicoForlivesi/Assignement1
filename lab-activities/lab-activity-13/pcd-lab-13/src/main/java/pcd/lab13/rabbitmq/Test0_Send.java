package pcd.lab13.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Test0_Send {

	private final static String QUEUE_NAME = "hello";
	private final static String NO_EXCHANGE_USED = ""; // Indica che viene usato l'exchange predefinito di rabbitmq

	public static void main(String[] argv) throws Exception {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection();
			Channel channel = connection.createChannel()) {

			channel.queueDeclare(QUEUE_NAME, true, false, false, null); // Il producer dichiara la coda per sicurezza.
			// RabbitMQ permette di dichiarare la stessa coda più volte senza problemi, come detto nella classe RECV,
			// se una coda esiste già non succede niente.

			String message = "Hello World!"; // Messaggio da inviare

			channel.basicPublish(NO_EXCHANGE_USED, QUEUE_NAME, null, message.getBytes("UTF-8"));
			// Viene pubblicato il messaggio sulla coda, senza proprietà aggiuntive
			System.out.println(" [x] Sent '" + message + "'");

			/* 
			 * closing not needed if we use try-with-resources statement 
			 * because both Connection and Channel implement java.lang.AutoCloseable. 
			 * 
			 */
			// channel.close();
			// connection.close();


			/**
			 * Producer
			 * apre connessione e channel
			 * dichiara la stessa coda
			 * pubblica un messaggio sull’exchange predefinito
			 * routing key = nome della coda
			 * */
		}
	}
}
