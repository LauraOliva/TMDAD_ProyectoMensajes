package tmdad.chat.configuration;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tmdad.chat.bbdd.DBController;
import tmdad.chat.controller.ChatRoomController;
import tmdad.chat.controller.MessageController;
import tmdad.chat.controller.UserController;

public class WebSocketHandler extends TextWebSocketHandler{
	private int connections = 0;
	MessageController msgController = new MessageController();
	UserController userController = new UserController();
	ChatRoomController chatController = new ChatRoomController();
	DBController dbController = new DBController();
	
	@Override 
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Sesion creada: " + connections);
				
		connections++;
		
	}
	
	/* TODO convesacion 2 personas */
	
	/* TODO comando closeroom */
	
	/* TODO comando openroom */
	
	/* TODO invite room */
	
	/* TODO si el administrados de una sala sala abandona la sala pasar los privilegios 
	 * a otro usuario
	 */

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	        
		// Obtener nombre de usuario
		String sender = UserController.getUsername(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	  	ArrayList<String> status = msgController.checkMessage(session, message, sender, dbController);
    	TextMessage msg;
    	String id_room;
		String id_user;
				
		System.out.println(status.get(0));
    	switch(status.get(0)){
    		case "ChatOK":
    			id_room = status.get(1);
    			msg = new TextMessage( payload.getString("content").trim());
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    			break;
    		case "VerifyActive":
    			id_user = status.get(1);
    			id_room = status.get(2);
    			userController.getNotificationUser(session, dbController);
    			chatController.getMsgRoom(session, id_room, dbController);
    			userController.sendNotificationToUser("Username: " + id_user + ", ChatRoom: " + id_room, session, "notification", dbController);
    			break;
    		case "VerifyNotActive":
    			id_user = status.get(1);
    			userController.getNotificationUser(session, dbController);
    			userController.sendNotificationToUser("Username: " + id_user + ", ChatRoom: null", session, "notification", dbController);
    			break;
    		case "KickOK":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Te han expulsado de la sala " + id_room, session, "notification", dbController);
    	    	break;
    		case "CreateNotOK":
    			System.err.println("CREATEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: CREATEROOM id_room", session, "notification", dbController);
    			break;
    		case "CreateOK":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Sala " + id_room + " creada con éxito", session, "notification", dbController);
    			userController.sendNotificationToUser("Te has unido a la sala " + id_room, session, "notification", dbController);
    	    	msg = new TextMessage( sender + " ha creado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    	    	break;
    		case "ChatUserMsg":
    			id_room = status.get(1);
    			chatController.getMsgRoom(session, id_room, dbController);
    			break;
    		case "ChatUserCreate":
    			id_room = status.get(1);
    			id_user = status.get(2);
    	    	msg = new TextMessage("Conversación entre " + sender + " y " + id_user + " iniciada");
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    			break;
    		case "ChatUserNotOK":
    			userController.sendNotificationToUser("Error en el número de parámetros: CHATUSER id_user", session, "notification", dbController);
    			break;
    		case "JoinNotOK":
    			System.err.println("JOINROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: JOINROOM id_room", session, "notification", dbController);
    			break;
    		case "JoinOK":
    			id_room = status.get(1);
    	    	msg = new TextMessage( "se ha unido a la sala " + id_room);
    			userController.sendNotificationToUser("Te has unido a la sala " + id_room, session, "notification", dbController);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    	    	break;
    		case "LeaveNotOK":
    			System.err.println("LEAVEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: LEAVEROOM id_room", session, "notification", dbController);
    			break;
    		case "LeaveOK":
    			id_room = status.get(1);
    	    	msg = new TextMessage( "ha abandonado la sala");
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);	
    			userController.sendNotificationToUser("Has abandonado la sala " + id_room, session, "notification", dbController);
    	    	break;
    		case "CloseOK":
    			userController.sendNotificationToUser("", session, "clean", dbController);
    			break;
    		case "CloseNotOK":
    			userController.sendNotificationToUser("Error en el número de parámetros: CLOSEROOM", session, "notification", dbController);
    			break;
    		case "OpenOK":
    			id_room = status.get(1);
    			chatController.getMsgRoom(session, id_room, dbController);
    			break;
    		case "NotInvited":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No te puedes unir a la sala " + id_room + ". Necesitas invitación.", session, "notification", dbController);
    			break;
    		case "OpenNotOK":
    			userController.sendNotificationToUser("Error en el número de parámetros: OPENROOM id_room", session, "notification", dbController);
    			break;
    		case "InviteOK":
				id_user = status.get(1);
    			id_room = status.get(2);
    			userController.sendNotificationToUser("Has invitado al usuario " + id_user + " a unirse a la sala " + id_room, session, "notification", dbController);
    	    	msg = new TextMessage("Se ha invitado a unirse a la sala a " + id_user);
    			chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    			userController.sendNotificationToUser("Has sido invitado a la sala " + id_room + " (JOINROOM " + id_room + " para aceptar)", userController.getSession(id_user), 
    					"notification", dbController);
    			break;
    		case "InviteNotOK":
    			userController.sendNotificationToUser("Error en el número de parámetros: INVITEROOM id_room id_user", session, "notification", dbController);
    			break;
    		case "DeleteNotOK":
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: DELETEROOM id_room", session, "notification", dbController);
    			break;
    		case "DeleteOK":
    			id_room = status.get(1);
    	    	msg = new TextMessage( "(Administrador) ha eliminado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
				// Avisar al resto de usuarios de que su activeroom es null
    	    	chatController.sendMessageRoom(id_room, msg, sender, "kick", dbController);
    	    	break;
    		case "KickRNotOK":
    			System.err.println("KICKROOM id_room id_user");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: KICKROOM id_room id_user", session, "notification", dbController);
    			break;
    		case "KickROK":
				id_user = status.get(1);
    			id_room = status.get(2);
    			WebSocketSession userSession = UserController.userUsernameMap.get(id_user);
    			userController.sendNotificationToUser("Has sido expulsado de la sala " + id_room, userSession, "notification", dbController);
				// Notificar al resto de usuarios de la sala
    	    	msg = new TextMessage( "(Administrador) ha expulsado de la sala " + id_room + " a " + id_user);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    	    	break;
    		case "RoomExists":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Ya existe la sala " + id_room, session, "notification", dbController);
    			break;
    		case "RoomNotExists":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No existe la sala " + id_room, session, "notification", dbController);
    			break;
    		case "NotAdmin":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No eres el administrador de la sala " + id_room, session, "notification", dbController);
    			break;
    		case "UserNotRoom":
				id_user = status.get(1);
    			id_room = status.get(2);
    			userController.sendNotificationToUser("El usuario " + id_user + " no pertenece a la sala " + id_room, session, "notification", dbController);	
    			break;
    		case "UserNotExists":
    			id_user = status.get(1);
    			userController.sendNotificationToUser("El usuario " + id_user + " no existe", session, "notification", dbController);	
    			break;
    		case "NoActiveRoom":
    			userController.sendNotificationToUser("No tienes ninguna sala activa", session, "notification", dbController);
    			break;
    		case "UserInRoom":
				id_user = status.get(1);
    			userController.sendNotificationToUser("El usuario " + id_user + " ya esta en la sala", session, "notification", dbController);
    			break;
    		default:
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Comando desconocido", session, "notification", dbController);

    	}
	    
	}
}
