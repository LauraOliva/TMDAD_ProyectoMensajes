package tmdad.chat.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;
import tmdad.chat.bbdd.DBAdministrator;

public class ChatRoomController {
	
	
	public void getMsgRoom(WebSocketSession session, String id_room, DBAdministrator dbAdministrator){
		
		boolean multiple = dbAdministrator.isMultiple(id_room);
		
		ArrayList<String> msg = dbAdministrator.getMsg(id_room, "chat", UserController.getUsername(session), multiple);
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
	
	public void sendMessageRoom(String id, TextMessage msg, String sender, String type, DBAdministrator dbAdministrator){
		ArrayList<String> users = dbAdministrator.getUsersChat(id);
		
		Date date= new Date();
		String timestamp = new SimpleDateFormat("HH:mm").format(date);			
		
		JSONObject message = new JSONObject();
		message.put("type", type);
		message.put("content", "<b>" + sender + ":</b> " + msg.getPayload() + " (" + timestamp + ")");
		
		TextMessage m = new TextMessage(message.toString());
		long time = date.getTime();
		dbAdministrator.insertMsg(sender, id, time, msg.getPayload(), type);
		UserController.userUsernameMap.entrySet().stream().forEach(entry -> {
	        try {
	        	WebSocketSession session = entry.getValue();
	        	if(session.isOpen()){
		        	String u = entry.getKey();
		            if(users.contains(u)){
		            	// Enviar mensaje si esta conectado
		            	if(session.isOpen()) session.sendMessage(m);
		            	
		            }
	        	}
	        } catch (Exception e) { e.printStackTrace(); }
	    });
	}
	
	public void sendFileRoom(String id, BinaryMessage file, String sender, DBAdministrator dbAdministrator){
		ArrayList<String> users = dbAdministrator.getUsersChat(id);
		
		/* TODO insertar fichero en la bbdd */
		
		UserController.userUsernameMap.entrySet().stream().forEach(entry -> {
	        try {
	        	WebSocketSession session = entry.getValue();
	        	if(session.isOpen()){
		        	String u = entry.getKey();
		            if(users.contains(u)){
		            	// Enviar mensaje si esta conectado
		            	if(session.isOpen()) session.sendMessage(file);
		            	
		            }
	        	}
	        } catch (Exception e) { e.printStackTrace(); }
	    });
	}
	


}
