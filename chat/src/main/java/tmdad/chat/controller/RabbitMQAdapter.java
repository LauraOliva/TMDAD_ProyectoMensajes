package tmdad.chat.controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;


public class RabbitMQAdapter {

	private final static String ENV_AMQPURL_NAME = "CLOUDAMQP_URL";
	private Channel channel;
	private Connection connection = null;

	RabbitMQAdapter(){
		ConnectionFactory factory = new ConnectionFactory();
		String amqpURL = System.getenv().get(ENV_AMQPURL_NAME) != null ? System.getenv().get(ENV_AMQPURL_NAME) : "amqp://localhost";
		try {
			factory.setUri(amqpURL);
		} catch (Exception e) {
			System.out.println(" [*] AQMP broker not found in " + amqpURL);
			System.exit(-1);
		}
		System.out.println(" [*] AQMP broker found in " + amqpURL);

		try {
			connection = factory.newConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Con un solo canal
		try {
			channel = connection.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createExchange(String name) throws IOException{
		channel.exchangeDeclare(name, "fanout");
	}
	
	public void publishExchange(String message, String exchange) throws IOException{		
		// Publicamos el mensaje en la centralita EXCHANGE_NAME declarada
		// antes. La clave de enrutado la dejamos vacÌa (la va a ignorar), 
		// e indicamos que el mensaje sea durable
		channel.basicPublish(exchange, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());				
		System.out.println(" [x] Enviado '" + message + "'");
	}
	
	public void createQueue(String queueName, String exchangeName) throws IOException{
		// Declaramos una centralita de tipo fanout llamada EXCHANGE_NAME
		channel.exchangeDeclare(exchangeName, "fanout");
		// Declaramos una cola en el broker a travÈs del canal
		// reciÈn creado llamada queueName 
		// Indicamos que sea durable pero no exclusiva y que 
		// no se borre autom·ticamente cuando nos desconectemos
		boolean durable = true;
		channel.queueDeclare(queueName, durable, false, false, null);
		// E indicamos que queremos que la centralita exchangeName
		// envÌe los mensajes a la cola reciÈn creada. Para ello creamos
		// una uniÛn (binding) entre ellas (la clave de enrutado
		// la ponemos vacÌa, porque se va a ignorar)	
		channel.queueBind(queueName, exchangeName, "");
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			// Insertar mensaje en la BBDD -> Aqui o en checker
			
			// Comrpobar si el mensaje va dirigido a su chat activo
			
			// Si no mandar como notificacion
			
			// Enviar mensaje a la sesion del usuario -> el username lo obtenermos por el nombre de la cola
			
	        String message = new String(delivery.getBody(), "UTF-8");
	        System.out.println(" [x] Recibido '" + message + "'");
	    };
	    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
	}
	
	public void deleteQueue(String queueName){
		
	}
	
	public void deleteExchange(String exchangeName){
		
	}
	
	public void unBindQueue(String queueName, String exchangeName){
		
	}
	
	public void sendMsg(String exchangeName, String msg) throws IOException{
		// Publicamos el mensaje en la centralita EXCHANGE_NAME declarada
		// antes. La clave de enrutado la dejamos vac√≠a (la va a ignorar), 
		// y no indicamos propiedades para el mensaje (por ejemplo,
		// el mensaje no ser√° durable)
		channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());				
		System.out.println(" [x] Enviado '" + msg + "'");
	}
	
	
}

