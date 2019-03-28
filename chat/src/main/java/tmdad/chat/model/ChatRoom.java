package tmdad.chat.model;

import java.util.Map.Entry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

public class ChatRoom {

	@Setter @Getter private String id;
	
	@Setter @Getter private Map<WebSocketSession, String> userUsernameMap;
	
	@Setter @Getter private String admin;
	
	@Setter @Getter private int nUser;
	
	public ChatRoom(String id, String admin, WebSocketSession session){
		this.id = id;
		this.admin = admin;
		this.nUser = 1;
		userUsernameMap = new ConcurrentHashMap<>();
		userUsernameMap.put(session, admin);
	}
	
	// Enviar el mensaje text a la sala con el identificador id
	public void sendMessageRoom(TextMessage msg, String sender, String type){
		String timestamp = new SimpleDateFormat("HH:mm").format(new Date());			
		
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", "<b>" + sender + ":</b> " + msg.getPayload() + " (" + timestamp + ")");
		
		TextMessage m = new TextMessage(message.toString());
		
		userUsernameMap.keySet().stream().filter(WebSocketSession::isOpen).forEach(session -> {
	        try {
	            session.sendMessage(m);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}
	
	public WebSocketSession getUser(String username){
		for (Entry<WebSocketSession, String> entry : userUsernameMap.entrySet()) {
	        if (entry.getValue().equals(username)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public String getUser(WebSocketSession session){
		return userUsernameMap.get(session);
	}
		
	public boolean existsUser(String username){
		return userUsernameMap.containsValue(username);
	}
	
	public void addUser(WebSocketSession session, String username){
		userUsernameMap.put(session, username);
		this.nUser++;
	}
	
	public void removeUser(WebSocketSession session){
		userUsernameMap.remove(session);
		this.nUser--;
	}
	
	public void removeUser(String username){
		userUsernameMap.entrySet()
		   .removeIf(entry -> (username.equals(entry.getValue())));
		this.nUser--;
	}
	
}
