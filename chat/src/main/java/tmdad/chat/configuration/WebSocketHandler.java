package tmdad.chat.configuration;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tmdad.chat.bbdd.DBAdministrator;
import tmdad.chat.controller.ChatRoomController;
import tmdad.chat.controller.MessageParser;
import tmdad.chat.controller.UserController;

@Component
public class WebSocketHandler extends TextWebSocketHandler{

	MessageParser msgParser = new MessageParser();
	UserController userController = new UserController();
	ChatRoomController chatController = new ChatRoomController();
	@Autowired DBAdministrator dbAdministrator;

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		// Obtener nombre de usuario
		String sender = UserController.getUsername(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	  	ArrayList<String> status = msgParser.checkMessage(session, message, sender, dbAdministrator);
    	TextMessage msg;
    	String id_room;
		String id_user;
				
		System.out.println(status.get(0));
		MessageParser.reply r = MessageParser.reply.valueOf(status.get(0).toUpperCase());
    	switch(r){
    		case WRONGCOMMAND:
    			System.err.println("CREATEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: usa HELP", session, "notification", dbAdministrator);
    			break;
    		case CHATOK:
    			id_room = status.get(1);
    			msg = new TextMessage( payload.getString("content").trim());
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    			break;
    		case VERIFYACTIVE:
    			id_user = status.get(1);
    			id_room = status.get(2);
    			userController.getNotificationUser(session, dbAdministrator);
    			chatController.getMsgRoom(session, id_room, dbAdministrator);
    			userController.sendNotificationToUser("Username: " + id_user + ", ChatRoom: " + id_room, session, "notification", dbAdministrator);
    			break;
    		case VERIFYNOTACTIVE:
    			id_user = status.get(1);
    			userController.getNotificationUser(session, dbAdministrator);
    			userController.sendNotificationToUser("Username: " + id_user + ", ChatRoom: null", session, "notification", dbAdministrator);
    			break;
    		case KICKOK:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Te han expulsado de la sala " + id_room, session, "notification", dbAdministrator);
    	    	break;
    		case CREATEOK:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Sala " + id_room + " creada con éxito", session, "notification", dbAdministrator);
    			userController.sendNotificationToUser("Te has unido a la sala " + id_room, session, "notification", dbAdministrator);
    	    	msg = new TextMessage( sender + " ha creado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    	    	break;
    		case CHATUSERMSG:
    			id_room = status.get(1);
    			chatController.getMsgRoom(session, id_room, dbAdministrator);
    			break;
    		case CHATUSERCREATE:
    			id_room = status.get(1);
    			id_user = status.get(2);
    	    	msg = new TextMessage("Conversación entre " + sender + " y " + id_user + " iniciada");
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    			break;
    		case JOINOK:
    			id_room = status.get(1);
    	    	msg = new TextMessage( "se ha unido a la sala " + id_room);
    			userController.sendNotificationToUser("", session, "clean", dbAdministrator);
    			userController.sendNotificationToUser("Te has unido a la sala " + id_room, session, "notification", dbAdministrator);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    	    	break;
    		case LEAVEOK:
    			id_room = status.get(1);
    	    	msg = new TextMessage( "ha abandonado la sala");
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);	
    			userController.sendNotificationToUser("Has abandonado la sala " + id_room, session, "notification", dbAdministrator);
    			userController.sendNotificationToUser("", session, "clean", dbAdministrator);
    	    	break;
    		case CLOSEOK:
    			userController.sendNotificationToUser("", session, "clean", dbAdministrator);
    			break;
    		case OPENOK:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("", session, "clean", dbAdministrator);
    			chatController.getMsgRoom(session, id_room, dbAdministrator);
    			break;
    		case NOTINVITED:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No te puedes unir a la sala " + id_room + ". Necesitas invitación.", session, "notification", dbAdministrator);
    			break;
    		case INVITEOK:
				id_user = status.get(1);
    			id_room = status.get(2);
    			userController.sendNotificationToUser("Has invitado al usuario " + id_user + " a unirse a la sala " + id_room, session, "notification", dbAdministrator);
    	    	msg = new TextMessage("Se ha invitado a unirse a la sala a " + id_user);
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    			userController.sendNotificationToUser("Has sido invitado a la sala " + id_room + " (JOINROOM " + id_room + " para aceptar)", userController.getSession(id_user), 
    					"notification", dbAdministrator);
    			break;
    		case DELETEOK:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Has eliminado la sala " + id_room, userController.getSession(sender), "notification", dbAdministrator);
    			userController.sendNotificationToUser("", session, "clean", dbAdministrator);
    	    	msg = new TextMessage( "(Administrador) ha eliminado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
				// Avisar al resto de usuarios de que su activeroom es null
    	    	chatController.sendMessageRoom(id_room, msg, sender, "kick", dbAdministrator);
    	    	break;
    		case KICKROK:
				id_user = status.get(1);
    			id_room = status.get(2);
    			WebSocketSession userSession = UserController.userUsernameMap.get(id_user);
    			userController.sendNotificationToUser("Has sido expulsado de la sala " + id_room, userSession, "notification", dbAdministrator);
				// Notificar al resto de usuarios de la sala
    	    	msg = new TextMessage( "(Administrador) ha expulsado de la sala " + id_room + " a " + id_user);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbAdministrator);
    	    	break;
    		case ROOMEXISTS:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Ya existe la sala " + id_room, session, "notification", dbAdministrator);
    			break;
    		case ROOMNOTEXISTS:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No existe la sala " + id_room, session, "notification", dbAdministrator);
    			break;
    		case NOTADMIN:
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No eres el administrador de la sala " + id_room, session, "notification", dbAdministrator);
    			break;
    		case USERNOTROOM:
				id_user = status.get(1);
    			id_room = status.get(2);
    			userController.sendNotificationToUser("El usuario " + id_user + " no pertenece a la sala " + id_room, session, "notification", dbAdministrator);	
    			break;
    		case USERNOTEXISTS:
    			id_user = status.get(1);
    			userController.sendNotificationToUser("El usuario " + id_user + " no existe", session, "notification", dbAdministrator);	
    			break;
    		case NOACTIVEROOM:
    			userController.sendNotificationToUser("No tienes ninguna sala activa", session, "notification", dbAdministrator);
    			break;
    		case USERINROOM:
				id_user = status.get(1);
    			userController.sendNotificationToUser("El usuario " + id_user + " ya esta en la sala", session, "notification", dbAdministrator);
    			break;
    		case NOTKNOWN:
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Comando desconocido", session, "notification", dbAdministrator);
    			break;
    		default:
    			break;

    	}
	    
	}
}
