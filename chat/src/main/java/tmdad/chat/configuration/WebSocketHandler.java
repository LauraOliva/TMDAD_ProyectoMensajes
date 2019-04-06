package tmdad.chat.configuration;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tmdad.chat.controller.ChatRoomController;
import tmdad.chat.controller.MessageController;
import tmdad.chat.controller.UserController;
import tmdad.chat.model.ChatRoom;

public class WebSocketHandler extends TextWebSocketHandler{
	private int connections = 0;
	MessageController msgController = new MessageController();
	UserController userController = new UserController();
	ChatRoomController chatController = new ChatRoomController();
	
	@Override 
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Sesion creada: " + connections);
		String username = "user" + connections;
		userController.newUser(username, session);
		connections++;
		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	        
		// Obtener nombre de usuario
		String sender = userController.getUsername(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
	    // Obtener sala activa del usuario
	    ChatRoom activeRoom = userController.getActiveChat(session);
	    
	    if(activeRoom == null){
	    	System.err.println("No hay chat activo");
	    }
	    else{
	    	System.out.println("Chat activo: " + activeRoom.getId());
	    }
	    
	    // Pasar el mensaje al MessageController
	    // MessageController esta conectado al ChatRoomController
	    // Dependiendo de la respuesta devolver una cosa u otra
	    

    	ArrayList<String> status = msgController.checkMessage(session, message, userController, chatController);
    	TextMessage msg;
    	String id_room;
		ChatRoom room;
		String id_user;
    	
    	switch(status.get(0)){
    		case "ChatOK":
    			msg = new TextMessage( payload.getString("content").trim());
    	    	activeRoom.sendMessageRoom(msg, sender, "chat");
    			break;
    		case "ChatNotOK":
    			/* TODO */
    			break;
    		case "KickOK":
    			System.out.println(sender + " kick");
    	    	userController.removeActiveChat(session);
    	    	break;
    		case "KickNotOK":
    			/* TODO */
    			break;
    		case "CreateNotOK":
    			System.err.println("CREATEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: CREATEROOM id_room", session, "notification");
    			break;
    		case "CreateOK":
    			id_room = status.get(1);
    			room = new ChatRoom(id_room, sender, session);
    			chatController.addChatRoom(room);
    			userController.sendNotificationToUser("Sala creada con éxito", session, "notification");
    	    	msg = new TextMessage( sender + " ha creado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat");
    	    	userController.addActiveChat(session, room);
    	    	break;
    		case "JoinNotOK":
    			System.err.println("JOINROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: JOINROOM id_room", session, "notification");
    			break;
    		case "JoinOK":
    			id_room = status.get(1);
    			room = chatController.getChatRoom(id_room);
				room.addUser(session, sender);
    	    	msg = new TextMessage( "se ha unido a la sala " + id_room);
    	    	room.sendMessageRoom(msg, sender, "chat");
    	    	userController.addActiveChat(session, room);
    	    	break;
    		case "LeaveNotOK":
    			System.err.println("LEAVEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: LEAVEROOM id_room", session, "notification");
    			break;
    		case "LeaveOK":
    			id_room = status.get(1);
    			room = chatController.getChatRoom(id_room);
				room.removeUser(session);
    	    	msg = new TextMessage( "ha abandonado la sala");
    	    	room.sendMessageRoom(msg, sender, "chat");	
    	    	userController.removeActiveChat(session);
    	    	break;
    		case "DeleteNotOK":
    			System.err.println("DELETEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: DELETEROOM id_room", session, "notification");
    			break;
    		case "DeleteOK":
    			id_room = status.get(1);
    			room = chatController.getChatRoom(id_room);
    			chatController.removeChatRoom(id_room);
    	    	msg = new TextMessage( "(Administrador) ha eliminado la sala " + id_room);
    	    	room.sendMessageRoom(msg, sender, "chat");
				// Avisar al resto de usuarios de que su activeroom es null
    	    	room.sendMessageRoom(msg, sender, "kick");
    	    	userController.removeActiveChat(session);
    	    	break;
    		case "KickRNotOK":
    			System.err.println("KICKROOM id_room id_user");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: KICKROOM id_room id_user", session, "notification");
    			break;
    		case "KickROK":
				id_user = status.get(1);
    			id_room = status.get(2);
    			room = chatController.getChatRoom(id_room);
    			WebSocketSession userSession = room.getUser(id_user);
    			userController.sendNotificationToUser("Has sido expulsado de la sala " + id_room, userSession, "kick");
				// Eliminar al usuario
				room.removeUser(id_user);
				// Notificar al resto de usuarios de la sala
    	    	msg = new TextMessage( "(Administrador) ha expulsado de la sala " + id_room + " a " + id_user);
    	    	room.sendMessageRoom(msg, sender, "chat");
    	    	break;
    		case "RoomExists":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("Ya existe la sala " + id_room, session, "notification");
    			break;
    		case "RoomNotExists":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No existe la sala " + id_room, session, "notification");
    			break;
    		case "NotAdmin":
    			id_room = status.get(1);
    			userController.sendNotificationToUser("No eres el administrador de la sala " + id_room, session, "notification");
    			break;
    		case "UserNotRoom":
				id_user = status.get(1);
    			id_room = status.get(2);
    			userController.sendNotificationToUser("El usuario " + id_user + " no pertenece a la sala " + id_room, session, "notification");	
    			break;
    		default:
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Comando desconocido", session, "notification");

    	}
	    
	}
}
