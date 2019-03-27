package tmdad.chat.model;

import java.io.IOException;
import java.util.ArrayList;

import javax.websocket.Session;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

public class ChatRoom {

	@Setter @Getter private String id;
	
	@Setter @Getter private ArrayList<User> users = new ArrayList<>();
	
	@Setter @Getter private User admin;
	
	public ChatRoom(String id, User admin){
		this.id = id;
		this.admin = admin;
		users.add(admin);
	}
	
	// Enviar el mensaje text a la sala con el identificador id
	public void sendMessageRoom(TextMessage msg){
		for(User u : users){
			WebSocketSession session = u.getSession();
			try {
				session.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// TODO hacer que estos métodos solo los pueda ejecutar el admin
	
	// TODO inviteUser
	
	public void addUser(User u){
		users.add(u);
	}
	
	public void removeUser(User u){
		users.remove(u);
	}
	
}
