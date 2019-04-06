package tmdad.chat.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;
import tmdad.chat.bbdd.DBController;
import tmdad.chat.model.ChatRoom;

public class ChatRoomController {
	

	@Setter @Getter static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
	
	@Getter @Setter private int nChatRooms;
	
	public ChatRoomController() {
		nChatRooms = 0;
	}
	
	public void sendMessageRoom(String id, TextMessage msg, String sender, String type, DBController dbController){
		ArrayList<String> users = dbController.getUsersChat(id);
		
		String timestamp = new SimpleDateFormat("HH:mm").format(new Date());			
		
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", "<b>" + sender + ":</b> " + msg.getPayload() + " (" + timestamp + ")");
		
		TextMessage m = new TextMessage(message.toString());
		
		UserController.userUsernameMap.entrySet().stream().forEach(entry -> {
	        try {
	        	WebSocketSession session = entry.getKey();
	        	if(session.isOpen()){
		        	String u = entry.getValue();
		            if(users.contains(u)){
		            	session.sendMessage(m);
		            }
		            
		            /* TODO si no esta conectado mandarlo a RabbitMQTT */
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}
	


}
