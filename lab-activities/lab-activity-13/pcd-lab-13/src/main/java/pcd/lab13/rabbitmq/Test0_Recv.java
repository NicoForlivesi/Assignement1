package pcd.lab13.rabbitmq;

import com.rabbitmq.client.*;

public class Test0_Recv {

	private final static String QUEUE_NAME = "hello"; // Il nome della coda da cui leggere deve essere lo stesso
	// fra sender e reciver

	public static void main(String[] argv) throws Exception {

		ConnectionFactory factory = new ConnectionFactory(); // Viene creata la connessione verso rabbitmq
		factory.setHost("localhost"); // Indica che rabbitmq gira in localhost
		try (Connection connection = factory.newConnection(); //Apre una connessione TCP verso RabbitMQ e un channel.
		     Channel channel = connection.createChannel()) { // Il channel è l’unità logica su cui si fanno tutte le operazioni (publish, consume, declare…).

			channel.queueDeclare(QUEUE_NAME, true, false, false, null); // Viene dichiarata la coda,
			// se non esiste la crea, se esiste già non fa nulla
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	
			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				// DeliverCallback è una lambda che RabbitMQ chiamerà ogni volta che arriva un messaggio.
				String message = new String(delivery.getBody(), "UTF-8"); // getBody contiene il payload
				// del messaggio
				System.out.println(" [x] Received '" + message + "' by thread: " + Thread.currentThread().getName());
			};
	
			boolean autoAck = true; // Significa che appena il consumer riceve il messaggio rabbitMQ lo da per assodato,
			// se il consumer crasha prima di aver elaborato il messaggio esso viene perso.

			String consumerTag = channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, // Viene registrato il
					// consumer sulla coda creata
					/* cancellation callback */ consTag -> {
					});
	
			System.out.println("Consumer configured - tag: " + consumerTag); // consumerTag è il ritorno di basicConsume
			// ed è un identificatore univoco.


			/**
			 * Consumer
			 * apre connessione e channel
			 * dichiara la stessa coda
			 * registra un callback che verrà chiamato quando arriva un messaggio
			 * usa autoAck = true
			 * */
		}
	}
}
