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
	ChatRoom activeRoom = null;
	ChatRoomController controller = new ChatRoomController();
	
	@Setter @Getter static Map<WebSocketSession, String> userUsernameMap = new ConcurrentHashMap<>();
	
	@Override 
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Sesion creada: " + connections);
		String username = "User" + connections;
		userUsernameMap.put(session, username);
		/*String roomId = "0";
		if(connections == 0){
			// Crear sala
			System.out.println("Sala creada: " + roomId);
			System.out.println("Administrador: " + username);
			room = new ChatRoom(roomId, username, session);
		}
		else{
			// Añadir usuario a la sala
			System.out.println("Nuevo usuario: " + username);
			room.addUser(session, username);
		}*/
		connections++;
		
	}

	public void sendNotificationToUser(String not, WebSocketSession session){
		JSONObject notification = new JSONObject();
		notification.put("type", "notification");
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
	    
	    String sender = userUsernameMap.get(session);
	    System.out.println("New Text Message Received From " + sender);
	    System.out.println(message.getPayload());
	    
	    // Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
	    if(type.equals("chat")){
	    	TextMessage msg = new TextMessage( payload.getString("content").trim());
	    	activeRoom.sendMessageRoom(msg, sender);	
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
	    			sendNotificationToUser("Error en el número de parámetros: CREATEROOM id_room", session);
	    		}
	    		else{
	    			String id = command[1];
	    			activeRoom = new ChatRoom(id, sender, session);
	    			controller.addChatRoom(activeRoom);
	    			sendNotificationToUser("Sala creada con éxito", session);
	    	    	TextMessage msg = new TextMessage( "ha creado la sala " + id);
	    	    	activeRoom.sendMessageRoom(msg, sender);	
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("joinroom")){
	    		System.out.println("JoinRoom");
	    		if(command.length != 2){
	    			System.err.println("JOINROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: JOINROOM id_room", session);
	    		}
	    		else{
	    			String id = command[1];
	    			activeRoom = controller.getChatRoom(id);
	    			if(activeRoom != null){
		    			activeRoom.addUser(session, sender);
		    	    	TextMessage msg = new TextMessage( "se ha unido a la sala");
		    	    	activeRoom.sendMessageRoom(msg, sender);	
	    			}
	    			sendNotificationToUser("No existe la sala " + id, session);
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("leaveroom")){
	    		System.out.println("LeaveRoom");
	    		if(command.length != 2){
	    			System.err.println("LEAVEROOM id_room");
	    			// Notificar al usuario
	    			sendNotificationToUser("Error en el número de parámetros: LEAVEROOM id_room", session);
	    		}
	    		else{
	    			String id = command[1];
	    			activeRoom = controller.getChatRoom(id);
	    			if(activeRoom != null){
		    			activeRoom.removeUser(session);
		    	    	TextMessage msg = new TextMessage( "ha abandonado la sala");
		    	    	activeRoom.sendMessageRoom(msg, sender);	
		    	    	activeRoom = null;
	    			}
	    			sendNotificationToUser("No existe la sala " + id, session);
	    		}
	    		
	    	}
	    	else if(command[0].toLowerCase().equals("changeRoom")){
	    		System.out.println("ChangeRoom");
	    		
	    	}
	    	else{
    			System.err.println("Comando desconocido");
    			// Notificar al usuario
    			sendNotificationToUser("Comando desconocido", session);
	    	}
	    	
	    	
	    }
	    
	}
}
