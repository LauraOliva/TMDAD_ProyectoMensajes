package tmdad.chat.controller;

import java.io.IOException;
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
	
	public void getMsgRoom(WebSocketSession session, String id_room, DBController dbController){
		
		/* TODO obtener solo los mensajes del periodo en el que ha estado en esa sala */
		
		
		// getLastDateJoin()
		
		ArrayList<String> msg = dbController.getMsg(id_room, "chat", UserController.getUsername(session));
		if(session.isOpen()){
			JSONObject message = new JSONObject();
			message.put("type", "chat");
			TextMessage m;
			for(int i = 0; i < msg.size(); i++){
				message.put("content", msg.get(i));
				try {
					System.out.println("send " + msg.get(i));
					m = new TextMessage(message.toString());
					session.sendMessage(m);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
	
	public void sendMessageRoom(String id, TextMessage msg, String sender, String type, DBController dbController){
		ArrayList<String> users = dbController.getUsersChat(id);
		
		Date date= new Date();
		String timestamp = new SimpleDateFormat("HH:mm").format(date);			
		
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", "<b>" + sender + ":</b> " + msg.getPayload() + " (" + timestamp + ")");
		
		TextMessage m = new TextMessage(message.toString());
		long time = date.getTime();
		dbController.insertMsg(sender, id, time, msg.getPayload(), type);
		UserController.userUsernameMap.entrySet().stream().forEach(entry -> {
	        try {
	        	WebSocketSession session = entry.getValue();
	        	if(session.isOpen()){
		        	String u = entry.getKey();
		            if(users.contains(u)){
		            	// Enviar mensaje si esta conectado
		            	if(session.isOpen()) session.sendMessage(m);
		    			
		    			/* TODO si no enviar a la cola */
		            	
		            }
	        	}
	        } catch (Exception e) { e.printStackTrace(); }
	    });
	}
	


}
