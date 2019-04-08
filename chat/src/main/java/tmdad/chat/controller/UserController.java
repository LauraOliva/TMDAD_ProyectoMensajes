package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.bbdd.DBController;

public class UserController {
	public static Map<String, WebSocketSession> userUsernameMap = new ConcurrentHashMap<>();

	
	public WebSocketSession getSession(String username){
		return userUsernameMap.get(username);
	}
	
	public static String getUsername(WebSocketSession session){
		for (Entry<String, WebSocketSession> entry : userUsernameMap.entrySet()) {
	        if (entry.getValue().equals(session)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public void sendNotificationToUser(String not, WebSocketSession session, String type, DBController dbController){
		JSONObject notification = new JSONObject();
		notification.put("type", type);
		notification.put("content", "<b>System</b>: " + not);
		TextMessage message = new TextMessage(notification.toString());
		try { 
			// Enviar notificacion si esta conectado
			if(session.isOpen()){
				session.sendMessage(message);
			}
			
			/* TODO si no enviar a la cola */
			
			String u = getUsername(session);
			Date date= new Date();
			long time = date.getTime();
			dbController.insertMsg("System", u, time , not, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getNotificationUser(WebSocketSession session, DBController dbController){
		ArrayList<String> not = dbController.getMsg(getUsername(session), "notification", getUsername(session));
		if(session.isOpen()){
			JSONObject message = new JSONObject();
			message.put("type", "notification");
			TextMessage m;
			for(int i = 0; i < not.size(); i++){
				message.put("content", not.get(i));
				try {
					System.out.println("send " + not.get(i));
					m = new TextMessage(message.toString());
					session.sendMessage(m);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
}
