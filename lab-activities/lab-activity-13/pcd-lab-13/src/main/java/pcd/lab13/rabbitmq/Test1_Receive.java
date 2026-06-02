package pcd.lab13.rabbitmq;

import com.rabbitmq.client.*;

public class Test1_Receive {

	private final static String QUEUE_NAME = "hello";

	public static void main(String[] argv) throws Exception {
		// Qui NON usiamo try-with-resources perché vogliamo che il consumer resti vivo
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, true, false, false, null); // Stessa coda
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		/*
		 * basicQos(1) significa:
		 * → RabbitMQ invia al consumer SOLO 1 messaggio alla volta
		 *   finché non arriva un ack.
		 *
		 * Questo evita che un consumer lento venga sommerso da messaggi.
		 * È fondamentale quando si usano ack manuali.
		 * Insomma significa “Non inviarmi un nuovo messaggio finché non ho fatto l’ack del precedente”, garantisce
		 * che ogni consumer lavori solo su un messaggio alla volta
		 */
		channel.basicQos(1); // accept only one unack-ed message at a time (see below)
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			System.out.println(" [x] Received '" + message + "' by thread " + Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				System.out.println(" [x] Done");
				/*
				 * Ack manuale:
				 * → conferma a RabbitMQ che il messaggio è stato elaborato.
				 * Se il consumer crasha prima di questo punto,
				 * il messaggio verrà rimesso nella coda.
				 * Con l'ack automatico semplicemente quando il messaggio viene inviato il processo viene dato per concluso
				 */
			    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		};

		boolean autoAck = false; // Ack automatici = false
		channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {});
	}
}
