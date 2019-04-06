package tmdad.chat.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;
import tmdad.chat.model.ChatRoom;

public class UserController {
	public static Map<WebSocketSession, String> userUsernameMap = new ConcurrentHashMap<>();
	@Setter @Getter private Map<WebSocketSession, ChatRoom> activeRoomMap = new ConcurrentHashMap<>();
	
	public void newUser(String username, WebSocketSession session){
		userUsernameMap.put(session, username);
	}
	
	public String getUsername(WebSocketSession session){
		return userUsernameMap.get(session);
	}
	
	public WebSocketSession getUser(String username){
		for (Entry<WebSocketSession, String> entry : userUsernameMap.entrySet()) {
	        if (entry.getValue().equals(username)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public ChatRoom getActiveChat(WebSocketSession session){
		return activeRoomMap.get(session);
	}
	
	public void removeActiveChat(WebSocketSession session){
		activeRoomMap.remove(session);
	}
	
	public void addActiveChat(WebSocketSession session, ChatRoom room){
		activeRoomMap.put(session, room);
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
}
