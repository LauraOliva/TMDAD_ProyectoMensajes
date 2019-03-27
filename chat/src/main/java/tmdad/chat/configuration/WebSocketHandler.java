package tmdad.chat.configuration;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tmdad.chat.model.ChatRoom;

public class WebSocketHandler extends TextWebSocketHandler{
	private int connections = 0;
	ChatRoom room = null;
	
	@Override 
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Sesion creada: " + connections);
		String username = "User" + connections;
		String roomId = "0";
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
		}
		connections++;
		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
	    System.out.println("New Text Message Received");
	    String sender = ChatRoom.getUserUsernameMap().get(session);
	    room.sendMessageRoom(message, sender);
	}
}
