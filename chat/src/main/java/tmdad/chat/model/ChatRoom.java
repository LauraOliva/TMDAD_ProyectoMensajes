package tmdad.chat.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;

public class ChatRoom {

	@Setter @Getter private String id;
	
	@Setter @Getter static Map<WebSocketSession, String> userUsernameMap = new ConcurrentHashMap<>();
	
	@Setter @Getter private String admin;
	
	@Setter @Getter private int nUser;
	
	public ChatRoom(String id, String admin, WebSocketSession session){
		this.id = id;
		this.admin = admin;
		this.nUser = 0;
		userUsernameMap.put(session, admin);
	}
	
	// Enviar el mensaje text a la sala con el identificador id
	public void sendMessageRoom(TextMessage msg, String sender){
		String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		JSONObject json = new JSONObject();
		json.put("event", "login");
		json.put("data", sender + ": " + msg.getPayload() + " (" + timestamp + ")"); 
		String txt = String.valueOf(json);				
		TextMessage m = new TextMessage(txt);
		
		userUsernameMap.keySet().stream().filter(WebSocketSession::isOpen).forEach(session -> {
	        try {
	            session.sendMessage(m);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}
	
	// TODO hacer que estos métodos solo los pueda ejecutar el admin
	
	// TODO inviteUser
	
	public void getUserSession(String username){
		
	}
	
	public void addUser(WebSocketSession session, String username){
		userUsernameMap.put(session, username);
		this.nUser++;
	}
	
	public void removeUser(WebSocketSession session){
		userUsernameMap.remove(session);
		this.nUser--;
	}
	
}
