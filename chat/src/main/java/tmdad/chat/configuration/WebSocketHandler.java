package tmdad.chat.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.Getter;
import lombok.Setter;
import tmdad.chat.controller.ChatRoomController;
import tmdad.chat.model.ChatRoom;

public class WebSocketHandler extends TextWebSocketHandler{
	private int connections = 0;
	ChatRoomController controller = new ChatRoomController();
	
	@Setter @Getter static Map<WebSocketSession, String> userUsernameMap = new ConcurrentHashMap<>();
	@Setter @Getter static Map<WebSocketSession, ChatRoom> activeRoomMap = new ConcurrentHashMap<>();
	
	@Override 
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Sesion creada: " + connections);
		String username = "user" + connections;
		userUsernameMap.put(session, username);
		connections++;
		
	}

	public void sendNotificationToUser(String not, WebSocketSession session, String type){
		JSONObject notification = new JSONObject();
		notification.put("type", type);
		notification.put("content", "<b>System</b>: " + not);
		TextMessage message = new TextMessage(notification.toString());
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	    
		// Obtener nombre de usuario
	    String sender = userUsernameMap.get(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
	    // Obtener sala activa del usuario
	    ChatRoom activeRoom = activeRoomMap.get(session);
	    
	    if(activeRoom == null){
	    	System.err.println("No hay chat activo");
	    }
	    else{
	    	System.out.println("Chat activo: " + activeRoom.getId());
	    }
	    
	    if(type.equals("chat") && activeRoom != null){
	    	TextMessage msg = new TextMessage( payload.getString("content").trim());
	    	activeRoom.sendMessageRoom(msg, sender, "chat");	
	    }
	    else if(type.equals("kick") && activeRoom != null){
	    	System.out.println(sender + " kick");
	    	activeRoomMap.remove(session);
	    }
	    else if(type.equals("command")){
	    	String content = payload.getString("content").trim();
	    	/*
	    	 * Estructura de los comandos
	    	 * COMMAND parameter [, parameter]
	    	 */
	    	
	    	String[] command = content.split("\\s+");
	    	if(command[0].toLowerCase().equals("createroom")){
	    		System.out.println("CreateRoom");
	    		if(command.length != 2){
	    			System.err.println("CREATEROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: CREATEROOM id_room", session, "notification");
	    		}
	    		else{
	    			String id = command[1];
	    			if(!controller.existsChatRoom(id)){
		    			ChatRoom room = new ChatRoom(id, sender, session);
		    			controller.addChatRoom(room);
		    			sendNotificationToUser("Sala creada con éxito", session, "notification");
		    	    	TextMessage msg = new TextMessage( sender + " ha creado la sala " + id);
		    	    	controller.sendMessageRoom(id, msg, sender, "chat");
		    	    	activeRoomMap.put(session, room);
	    			}
	    			else{
		    			sendNotificationToUser("Ya existe la sala " + id, session, "notification");
	    			}
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("joinroom")){
	    		System.out.println("JoinRoom");
	    		if(command.length != 2){
	    			System.err.println("JOINROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: JOINROOM id_room", session, "notification");
	    		}
	    		else{
	    			String id = command[1];
	    			ChatRoom room = controller.getChatRoom(id);
	    			if(room != null){
	    				room.addUser(session, sender);
		    	    	TextMessage msg = new TextMessage( "se ha unido a la sala " + id);
		    	    	room.sendMessageRoom(msg, sender, "chat");
		    	    	activeRoomMap.put(session, room);
	    			}
	    			else{
	    				sendNotificationToUser("No existe la sala " + id, session, "notification");
	    			}
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("leaveroom")){
	    		System.out.println("LeaveRoom");
	    		if(command.length != 2){
	    			System.err.println("LEAVEROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: LEAVEROOM id_room", session, "notification");
	    		}
	    		else{
	    			String id = command[1];
	    			ChatRoom room = controller.getChatRoom(id);
	    			if(room != null){
	    				room.removeUser(session);
		    	    	TextMessage msg = new TextMessage( "ha abandonado la sala");
		    	    	room.sendMessageRoom(msg, sender, "chat");	
		    	    	activeRoomMap.remove(sender);
	    			}
	    			else{
	    				sendNotificationToUser("No existe la sala " + id, session, "notification");
	    			}
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("deleteroom")){
	    		System.out.println("DeleteRoom");
	    		if(command.length != 2){
	    			System.err.println("DELETEROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: DELETEROOM id_room", session, "notification");
	    		}
	    		else{
	    			String id = command[1];
	    			ChatRoom room = controller.getChatRoom(id);
	    			if(room != null){
		    			String admin = room.getAdmin();
		    			if(admin.equals(sender)){
		    				controller.removeChatRoom(id);
			    	    	TextMessage msg = new TextMessage( "(Administrador) ha eliminado la sala " + id);
			    	    	room.sendMessageRoom(msg, sender, "chat");
		    				// Avisar al resto de usuarios de que su activeroom es null
			    	    	room.sendMessageRoom(msg, sender, "kick");
			    	    	activeRoomMap.remove(session);
		    			}
		    			else{
			    			sendNotificationToUser("No eres el administrador de la sala " + id, session, "notification");		    				
		    			}
	    			}
	    			else{
	    				sendNotificationToUser("No existe la sala " + id, session, "notification");
	    			}
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("kickroom")){
	    		System.out.println("KickRoom");
	    		if(command.length != 3){
	    			System.err.println("KICKROOM id_room id_user");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: KICKROOM id_room id_user", session, "notification");
	    		}
	    		else{
	    			String id_room = command[1];
	    			ChatRoom room = controller.getChatRoom(id_room);
	    			if(room != null){
		    			String admin = room.getAdmin();
		    			if(admin.equals(sender)){
		    				String id_user = command[2];
		    				if(room.existsUser(id_user)){
			    				// Notificar al usuario
			    				WebSocketSession userSession = room.getUser(id_user);
			    				sendNotificationToUser("Has sido expulsado de la sala " + id_room, userSession, "kick");
			    				// Eliminar al usuario
			    				room.removeUser(id_user);
		    					// Notificar al resto de usuarios de la sala
				    	    	TextMessage msg = new TextMessage( "(Administrador) ha expulsado de la sala " + id_room + " a " + id_user);
				    	    	room.sendMessageRoom(msg, sender, "chat");
		    				}
		    				else{
				    			sendNotificationToUser("El usuario " + id_user + " no pertenece a la sala " + id_room, session, "notification");			    					
		    				}
		    			}
		    			else{
			    			sendNotificationToUser("No eres el administrados de la sala " + id_room, session, "notification");		    				
		    			}
	    			}
	    			else{
	    				sendNotificationToUser("No existe la sala " + id_room, session, "notification");
	    			}
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("inviteRoom")){
	    		System.out.println("InviteRoom");
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("changeRoom")){
	    		System.out.println("ChangeRoom");
	    		
	    	}
	    	else{
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			sendNotificationToUser("Comando desconocido", session, "notification");
	    	}
	    	
	    	
	    }
	    
	}
}
