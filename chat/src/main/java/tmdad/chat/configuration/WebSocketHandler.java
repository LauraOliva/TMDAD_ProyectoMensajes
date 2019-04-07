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
		String username = "user" + connections;
		if(dbController.existsUser(username)){
			System.out.println("Ya existe un usuario con nombre " + username);		
		}
		else{
			dbController.insertUser(username, "1234", true);
		}
		userController.newUser(username, session);
		connections++;
		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	        
		// Obtener nombre de usuario
		String sender = UserController.userUsernameMap.get(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    
	    // Obtener sala activa del usuario
	    String id_active_room = dbController.getActiveRoom(sender);
	    System.out.println("id_room_db = " + id_active_room);
	    
	    if(id_active_room == null){
	    	System.err.println("No hay chat activo");
	    }
	    else{
	    	System.out.println("Chat activo: " + id_active_room);
	    }    

    	ArrayList<String> status = msgController.checkMessage(session, message, userController, chatController, dbController);
    	TextMessage msg;
    	String id_room;
		String id_user;
		
		/* TODO almacenar los mensajes */
		
		/* TODO recuperar los mensajes */
		
		/* TODO recuperar las notificaciones */
		
		/* TODO almacenar las notificaciones en la bbdd de tipo not */
		
		/* TODO convesacion 2 personas */
		
		/* TODO change room o algo para que el usuario se pueda cambiar de salas 
		 * sin hacer join y leave 
		 */
		
		/* TODO invite room */
		
		/* TODO si el administrados de una sala sala abandona la sala pasar los privilegios 
		 * a otro usuario
		 */
    	
    	switch(status.get(0)){
    		case "ChatOK":
    			msg = new TextMessage( payload.getString("content").trim());
    			chatController.sendMessageRoom(id_active_room, msg, sender, "chat", dbController);
    	    	//activeRoom.sendMessageRoom(msg, sender, "chat");
    			break;
    		case "ChatNotOK":
    			/* TODO */
    			break;
    		case "KickOK":
    			System.out.println(sender + " kick");
    	    	//userController.removeActiveChat(session);
    	    	dbController.removeActiveRoom(sender);
    	    	break;
    		case "KickNotOK":
    			/* TODO */
    			break;
    		case "CreateNotOK":
    			System.err.println("CREATEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: CREATEROOM id_room", session, "notification", dbController);
    			break;
    		case "CreateOK":
    			id_room = status.get(1);
    			//room = new ChatRoom(id_room, sender, session);
    			//chatController.addChatRoom(room);
    			dbController.insertChat(id_room, sender, true);
    			userController.sendNotificationToUser("Sala creada con éxito", session, "notification", dbController);
    	    	msg = new TextMessage( sender + " ha creado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    	    	//userController.addActiveChat(session, room);
    	    	dbController.setActiveRoom(sender, id_room);
    	    	break;
    		case "JoinNotOK":
    			System.err.println("JOINROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: JOINROOM id_room", session, "notification", dbController);
    			break;
    		case "JoinOK":
    			id_room = status.get(1);
    			//room = chatController.getChatRoom(id_room);
				//room.addUser(session, sender);
    	    	msg = new TextMessage( "se ha unido a la sala " + id_room);
    			userController.sendNotificationToUser("Te has unido a la sala " + id_room, session, "notification", dbController);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
    	    	//userController.addActiveChat(session, room);
    	    	dbController.setActiveRoom(sender, id_room);
    	    	chatController.getMsgRoom(session, id_room, dbController);
    	    	break;
    		case "LeaveNotOK":
    			System.err.println("LEAVEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: LEAVEROOM id_room", session, "notification", dbController);
    			break;
    		case "LeaveOK":
    			id_room = status.get(1);
    			//room = chatController.getChatRoom(id_room);
				//room.removeUser(session);
    	    	msg = new TextMessage( "ha abandonado la sala");
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);	
    	    	//userController.removeActiveChat(session);
    	    	dbController.removeActiveRoom(sender);
    	    	break;
    		case "DeleteNotOK":
    			System.err.println("DELETEROOM id_room");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: DELETEROOM id_room", session, "notification", dbController);
    			break;
    		case "DeleteOK":
    			id_room = status.get(1);
    			//room = chatController.getChatRoom(id_room);
    			//chatController.removeChatRoom(id_room);
    	    	msg = new TextMessage( "(Administrador) ha eliminado la sala " + id_room);
    	    	chatController.sendMessageRoom(id_room, msg, sender, "chat", dbController);
				// Avisar al resto de usuarios de que su activeroom es null
    	    	chatController.sendMessageRoom(id_room, msg, sender, "kick", dbController);
    	    	//userController.removeActiveChat(session);
    	    	dbController.removeActiveRoom(sender);
    	    	dbController.removeChat(id_room);
    	    	break;
    		case "KickRNotOK":
    			System.err.println("KICKROOM id_room id_user");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Error en el número de parámetros: KICKROOM id_room id_user", session, "notification", dbController);
    			break;
    		case "KickROK":
				id_user = status.get(1);
    			id_room = status.get(2);
    			//room = chatController.getChatRoom(id_room);
    			WebSocketSession userSession = userController.getUser(id_user);
    			userController.sendNotificationToUser("Has sido expulsado de la sala " + id_room, userSession, "kick", dbController);
				// Eliminar al usuario
				//room.removeUser(id_user);
    			dbController.removeActiveRoom(id_user);
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
    		default:
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			userController.sendNotificationToUser("Comando desconocido", session, "notification", dbController);

    	}
	    
	}
}
