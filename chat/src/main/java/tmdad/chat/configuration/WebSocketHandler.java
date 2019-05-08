package tmdad.chat.configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tmdad.chat.bbdd.DBAdministrator;
import tmdad.chat.controller.CensureAdapter;
import tmdad.chat.controller.CommandChecker;
import tmdad.chat.controller.CommandChecker.typeMessage;
import tmdad.chat.controller.RabbitMQAdapter;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

	CommandChecker msgParser = new CommandChecker();
	@Autowired DBAdministrator dbAdministrator;
	@Autowired RabbitMQAdapter rabbitAdapter;
	@Autowired CensureAdapter censureAdapter;

	public static Map<String, Long> timeMap = new ConcurrentHashMap<>();
	public static Map<String, Integer> numUserMap = new ConcurrentHashMap<>();
	public static Map<String, Long> timeRabbit = new ConcurrentHashMap<>();
	
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String sender = DBAdministrator.getUsername(session);
		if(sender != null){
			System.out.println(sender);
			DBAdministrator.userUsernameMap.remove(sender);
		}
		System.err.println("Connection closed");
		super.afterConnectionClosed(session, status);
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.err.println("Error");
		super.handleTransportError(session, exception);
	}
	
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		// Obtener nombre de usuario
		String sender = DBAdministrator.getUsername(session);
		if(sender != null){
			timeMap.put(sender, System.nanoTime());
			int numUsersRoom = msgParser.getNumActiveUsers();
			numUserMap.put(sender, numUsersRoom);
		}

	    // Leer el json para saber que tipo de mensaje es
		JSONObject payload = null;
		try{
			payload = new JSONObject(message.getPayload());
		}
		catch(JSONException e){
			System.out.println(message.getPayload());
			System.out.println(e.getStackTrace());
		}
	  	ArrayList<String> status = msgParser.checkMessage(session, message, sender, dbAdministrator);
    	String id_room;
		String id_user;	
		String msg;
		ArrayList<String> censure = new ArrayList<>();
		JSONObject json = new JSONObject();
		TextMessage textMessage;

		CommandChecker.reply r = CommandChecker.reply.valueOf(status.get(0).toUpperCase());
    	switch(r){
    		case HELPOK:
    			rabbitAdapter.sendMsgQueue(sender, status.get(1), typeMessage.NOTIFICATION.toString());
    			break;
    		case NOTROOT:
    			rabbitAdapter.sendMsgQueue(sender, "No eres administrador", typeMessage.NOTIFICATION.toString());
    			break;
    		case BROADCASTOK:
    			rabbitAdapter.sendMsgQueue(sender, "Notification enviada", typeMessage.NOTIFICATION.toString());
    			// Censura
    			msg = status.get(1);
    			censure = censureAdapter.filterMsg(msg, sender);
    			if(Boolean.parseBoolean(censure.get(0))) msg = censure.get(1);
    			rabbitAdapter.sendMsg(RabbitMQAdapter.ROOT_EXCHANGE, msg, "root", typeMessage.BROADCAST.toString(), dbAdministrator);
    			break;
    		case WRONGCOMMAND:
    			// Notificar al usuario
    			rabbitAdapter.sendMsgQueue(sender, "Error en el número de parámetros: usa HELP", typeMessage.NOTIFICATION.toString());
    			break;
    		case ROOMSOK:
    			rabbitAdapter.sendMsgQueue(sender, status.get(1), typeMessage.NOTIFICATION.toString());    			
    			break;
    		case USERSROOMOK:
    			rabbitAdapter.sendMsgQueue(sender, status.get(1), typeMessage.NOTIFICATION.toString());  
    			break;
    		case CHATOK:
    			id_room = status.get(1);
    			// Censura
    			msg = payload.getString("content").trim();
    			censure = censureAdapter.filterMsg(msg, sender);
    			if(Boolean.parseBoolean(censure.get(0))) msg = censure.get(1);
    			rabbitAdapter.sendMsg(id_room, msg, sender, typeMessage.CHAT.toString(), dbAdministrator);
    			long startTime = WebSocketHandler.timeMap.get(sender);
    			long endTime = System.nanoTime();
    			// get difference of two nanoTime values
    			long timeElapsed = endTime - startTime;
    			int numUsersRoom = numUserMap.get(sender);//dbAdministrator.getNumUsersRoom(id_room);
    			System.out.println(numUsersRoom + ", " + timeElapsed/1000000);
    			try(FileWriter fw = new FileWriter("times.txt", true);
    				    BufferedWriter bw = new BufferedWriter(fw);
    				    PrintWriter out = new PrintWriter(bw))
				{
				    out.println(numUsersRoom + ", " + timeElapsed/1000000);
				} catch (IOException e) {
				    //exception handling left as an exercise for the reader
				}

    			break;
    		case VERIFYOK:
    			id_user = status.get(1);
    			json.put("type", typeMessage.VERIFY.toString());
    			json.put("content", "ok");
    			textMessage = new TextMessage(json.toString());
    			session.sendMessage(textMessage);
    			rabbitAdapter.bindQueue(id_user, RabbitMQAdapter.ROOT_EXCHANGE, dbAdministrator);
    			rabbitAdapter.sendMsgQueue(id_user, "Username: " + id_user + ", ChatRoom: null", typeMessage.NOTIFICATION.toString());
    			break;
    		case VERIFYNOK:
    			json.put("type", typeMessage.VERIFY.toString());
    			json.put("content", "no");
    			textMessage = new TextMessage(json.toString());
    			session.sendMessage(textMessage);
    			break;
    		case KICKOK:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "Te han expulsado de la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
    			rabbitAdapter.unbindQueue(sender, id_room);	
    	    	break;
    		case CREATEOK:
    			id_room = status.get(1);
    			rabbitAdapter.bindQueue(sender, id_room, dbAdministrator);
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
    			rabbitAdapter.sendMsgQueue(sender, "Sala " + id_room + " creada con éxito", typeMessage.NOTIFICATION.toString());
    			rabbitAdapter.sendMsg(id_room, sender + " ha creado la sala " + id_room, sender, typeMessage.CHAT.toString(), 
    					dbAdministrator);
    			rabbitAdapter.sendMsg(id_room, "se ha unido a la sala", sender, typeMessage.CHAT.toString(), dbAdministrator);
    	    	break;
    		case CHATUSERMSG:
    			id_room = status.get(1);
    			break;
    		case CHATUSERCREATE:
    			id_room = status.get(1);
    			id_user = status.get(2);
    			rabbitAdapter.bindQueue(sender, id_room, dbAdministrator);
    			rabbitAdapter.bindQueue(id_user, id_room, dbAdministrator);
    			rabbitAdapter.sendMsg(id_room, "Conversación entre " + sender + " y " + id_user + " iniciada", sender, 
    					typeMessage.CHAT.toString(), dbAdministrator);
    			
    			break;
    		case JOINOK:
    			id_room = status.get(1);
    			rabbitAdapter.bindQueue(sender, id_room, dbAdministrator);
    			rabbitAdapter.sendMsgQueue(sender, "Te has unido a la sala", typeMessage.NOTIFICATION.toString());
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
    			rabbitAdapter.sendMsg(id_room, "se ha unido a la sala", sender, typeMessage.CHAT.toString(), dbAdministrator);
    	    	break;
    		case LEAVEOK:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "Has abandonado la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
    			rabbitAdapter.unbindQueue(sender, id_room);	
    			rabbitAdapter.sendMsg(id_room, "ha abandonado la sala", sender, typeMessage.CHAT.toString(), dbAdministrator);
    	    	break;
    		case CLOSEOK:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
    			break;
    		case OPENOK:
    			id_room = status.get(1);
    			rabbitAdapter.bindQueue(sender, id_room, dbAdministrator);
    			break;
    		case NOTINVITED:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "No te puedes unir a la sala " + id_room + ". Necesitas invitación." + id_room, typeMessage.NOTIFICATION.toString());
    			break;
    		case INVITEOK:
				id_user = status.get(1);
    			id_room = status.get(2);
    			rabbitAdapter.sendMsgQueue(sender, "Has invitado al usuario " + id_user + " a unirse a la sala " + id_room, typeMessage.NOTIFICATION.toString());
    	    	rabbitAdapter.sendMsg(id_room, "Se ha invitado a unirse a la sala a " + id_user, sender, typeMessage.CHAT.toString(), 
    	    			dbAdministrator);
    			rabbitAdapter.sendMsgQueue(id_user, "Has sido invitado a la sala " + id_room + " (JOINROOM " + id_room + " para aceptar)", typeMessage.NOTIFICATION.toString());
    			break;
    		case DELETEOK:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "Has eliminado la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			rabbitAdapter.sendMsgQueue(sender, "", typeMessage.CLEAN.toString());
				rabbitAdapter.sendMsg(id_room, "", sender, typeMessage.KICK.toString(), dbAdministrator);
    			rabbitAdapter.deleteExchange(id_room);
    	    	break;
    		case KICKROK:
				id_user = status.get(1);
    			id_room = status.get(2);
    			rabbitAdapter.sendMsgQueue(id_user, "Has sido expulsado de la sala " + id_room, typeMessage.NOTIFICATION.toString());
				rabbitAdapter.sendMsgQueue(id_user, "", typeMessage.CLEAN.toString());
    			rabbitAdapter.unbindQueue(id_user, id_room);
    			rabbitAdapter.sendMsg(id_room, "(Administrador) ha expulsado de la sala " + id_room + " a " + id_user, sender, 
    					typeMessage.CHAT.toString(), dbAdministrator);
    	    	break;
    		case ADDCENSUREOK:
    			msg = censureAdapter.addWord(status.get(1));
    			rabbitAdapter.sendMsgQueue(sender, msg, typeMessage.NOTIFICATION.toString());
    			break;
    		case REMCENSUREOK:
    			msg = censureAdapter.removeWord(status.get(1));
    			rabbitAdapter.sendMsgQueue(sender, msg, typeMessage.NOTIFICATION.toString());
    			break;
    		case GETCENSUREOK:
    			msg = censureAdapter.censuredWords();
    			rabbitAdapter.sendMsgQueue(sender, msg, typeMessage.NOTIFICATION.toString());
    			break;
    		case ROOMEXISTS:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "Ya existe la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			break;
    		case ROOMNOTEXISTS:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "No existe la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			break;
    		case NOTADMIN:
    			id_room = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "No eres el administrador de la sala " + id_room, typeMessage.NOTIFICATION.toString());
    			break;
    		case USERNOTROOM:
				id_user = status.get(1);
    			id_room = status.get(2);
    			rabbitAdapter.sendMsgQueue(sender, "El usuario " + id_user + " no pertenece a la sala " + id_room, typeMessage.NOTIFICATION.toString());	
    			break;
    		case USERNOTEXISTS:
    			id_user = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "El usuario " + id_user + " no existe", typeMessage.NOTIFICATION.toString());
    			break;
    		case NOACTIVEROOM:
    			rabbitAdapter.sendMsgQueue(sender, "No tienes ninguna sala activa", typeMessage.NOTIFICATION.toString());
    			break;
    		case USERINROOM:
				id_user = status.get(1);
    			rabbitAdapter.sendMsgQueue(sender, "El usuario " + id_user + " ya esta en la sala", typeMessage.NOTIFICATION.toString());
    			break;
    		case NOTKNOWN:
    			rabbitAdapter.sendMsgQueue(sender, "Comando desconocido", typeMessage.NOTIFICATION.toString());
    			break;
    		case NOTJSON:
    			System.out.println("No es json");
    		default:
    			break;

    	}
	    
	}
}
