package tmdad.chat.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;

import tmdad.chat.bbdd.DBAdministrator;
import tmdad.chat.configuration.WebSocketHandler;
import tmdad.chat.controller.CommandChecker.typeMessage;


@Component
public class RabbitMQAdapter {

	public final static String ROOT_EXCHANGE = "rootExchange";
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
	
	public void bindQueue(String queueName, String exchangeName, DBAdministrator dbAdministrator){

		try {
			// Declaramos una centralita de tipo fanout llamada EXCHANGE_NAME
			channel.exchangeDeclare(exchangeName, "fanout", true);
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {			
			
			// Enviar mensaje a la sesion del usuario -> el username lo obtenermos por el nombre de la cola
			JSONObject payload = new JSONObject(new String(delivery.getBody()));
	        String message = payload.getString("msg");
	        String type = payload.getString("type");
	        String sender = payload.getString("sender");
	        String dst = payload.getString("dst");
	        // Comrpobar si el mensaje va dirigido a su chat activo
			String id_room = dbAdministrator.getActiveRoom(queueName);
			if(!dst.equals(id_room) && type.equals(typeMessage.CHAT.toString())){
				// Si no mandar como notificacion
				message = "Nuevo mensaje en " + dst;
				type = typeMessage.NOTIFICATION.toString();
				sender = "System";
			}

			WebSocketSession session = DBAdministrator.userUsernameMap.get(queueName);
			if(session == null || !session.isOpen()){
				//channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				try{
					channel.basicCancel(consumerTag);
				}catch (IOException e1) {
					System.err.println("Unknown consumer tag");
				}
				System.err.println(queueName);
				System.err.println(dst);
				if(dst != null){
					channel.basicPublish("", dst, MessageProperties.PERSISTENT_TEXT_PLAIN, delivery.getBody());
				}
			}
			else{
		        sendMsgSession(message, queueName, type, sender, delivery.getEnvelope().getDeliveryTag());
			}
			
	    };
	    try {
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteQueue(String queueName) throws IOException{
		channel.queueDelete(queueName);
	}
	
	public void deleteExchange(String exchangeName) throws IOException{
		channel.exchangeDelete(exchangeName);
	}

	public void unbindQueue(String queueName, String exchangeName) throws IOException{
		channel.queueUnbind(queueName, exchangeName, "");
	}
	
	public void sendMsg(String exchangeName, String msg, String sender, String type, DBAdministrator dbAdministrator) throws IOException{
		JSONObject json = new JSONObject();
		json.put("msg", msg);
		json.put("type", type);
		json.put("sender", sender);
		json.put("dst", exchangeName);
		
		
		// Publicamos el mensaje en la centralita EXCHANGE_NAME declarada
		// antes. La clave de enrutado la dejamos vac√≠a (la va a ignorar), 
		// y no indicamos propiedades para el mensaje (por ejemplo,
		// el mensaje no ser√° durable)
		System.err.println(exchangeName);
		if(exchangeName != null){
			channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, json.toString().getBytes());
		}
		
		// Guardar mensaje en la base de datos
		dbAdministrator.insertMsg(sender, exchangeName, (new Date()).getTime(), msg, type);		
	}
	
	// Se utiliza para enviar notificaciones al usuario
	// Las notificaciones no se almacenan en la BD
	public void sendMsgQueue(String queueName, String msg, String type) throws IOException{
		// Publicamos el mensaje en la centralita EXCHANGE_NAME declarada
		// antes. La clave de enrutado la dejamos vac√≠a (la va a ignorar), 
		// y no indicamos propiedades para el mensaje (por ejemplo,
		// el mensaje no ser√° durable)
		JSONObject json = new JSONObject();
		json.put("msg", msg);
		json.put("type", type);
		json.put("sender", "System");
		json.put("dst", queueName);
		System.err.println(queueName);
		if(queueName != null){
			channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, json.toString().getBytes());
		}

	}
	
	public void sendMsgSession(String msg, String username, String type, String sender, long tag){
		Date date= new Date();
		String timestamp = new SimpleDateFormat("HH:mm").format(date);	
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", "<b>" + sender + "</b>: " + msg + " (" + timestamp + ")");
		TextMessage textMmessage = new TextMessage(message.toString());
		//try { 
			WebSocketSession session = DBAdministrator.userUsernameMap.get(username);
			if(session != null && session.isOpen()){
				try{
					session.sendMessage(textMmessage);
				}
				catch (Exception e){
					System.err.println("no se ha posidio enviar el mensaje");
				}
				//channel.basicAck(tag, false);
			}
		/*} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
}

