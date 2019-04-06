package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.model.ChatRoom;

public class MessageController {

	
	public ArrayList<String> checkMessage(WebSocketSession session, TextMessage message, 
				UserController userController, ChatRoomController chatController) throws IOException {
	    ArrayList<String> result = new ArrayList<>();
	    
		// Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
	    // Obtener nombre de usuario
		String sender = userController.getUsername(session);
		
		// Obtener sala activa del usuario
	    ChatRoom activeRoom = userController.getActiveChat(session);
	    
	    if(type.equals("chat")){
	    	if(activeRoom != null){
	    		result.add("ChatOK");
	    	}
	    	else{
	    		result.add("ChatNotOK");
	    	}
	    	// False
	    	// Send notification
	    }
	    else if(type.equals("kick")){
	    	if(activeRoom != null){
	    		result.add("KickOK");
	    	}
	    	else{
	    		result.add("KickNotOK");
	    	}
	    	// False
	    	// Send notification
	    }
	    else if(type.equals("command")){
	    	String content = payload.getString("content").trim();
	    	/*
	    	 * Estructura de los comandos
	    	 * COMMAND parameter [, parameter]
	    	 */
	    	
	    	String[] command = content.split("\\s+");
	    	String cmd = command[0].toLowerCase();
	    	switch(cmd){
	    		case "createroom":
		    		System.out.println("CreateRoom");
		    		if(command.length != 2)result.add("CreateNotOK");
		    		else{
		    			String id = command[1];
		    			if(!chatController.existsChatRoom(id)) result.add("CreateOK");
		    			else result.add("RoomExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "joinroom":
		    		System.out.println("JoinRoom");
		    		if(command.length != 2)result.add("JoinNotOK");
		    		else{
		    			String id = command[1];
		    			ChatRoom room = chatController.getChatRoom(id);
		    			if(room != null) result.add("JoinOK");
		    			else result.add("RoomNotExists");
	    				result.add(id);
		    		}
		    		break;
	    		case "leaveroom":
		    		System.out.println("LeaveRoom");
		    		if(command.length != 2) result.add("LeaveNotOK");
		    		else{
		    			String id = command[1];
		    			if(chatController.getChatRoom(id) != null)result.add("LeaveOK");
		    			else result.add("RoomNotExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "deleteroom":
		    		System.out.println("DeleteRoom");
		    		if(command.length != 2) result.add("DeleteNotOK");
		    		else{
		    			String id = command[1];
		    			ChatRoom room = chatController.getChatRoom(id);
		    			if(room != null){
			    			String admin = room.getAdmin();
			    			if(admin.equals(sender)) result.add("DeleteOK");
			    			else result.add("NotAdmin");
		    			}
		    			else result.add("RoomNotExists");
	    				result.add(id);
		    		}
		    		break;
	    		case "kickroom":
		    		System.out.println("KickRoom");
		    		if(command.length != 3) result.add("KickRNotOK");
		    		else{
		    			String id_room = command[1];
		    			ChatRoom room = chatController.getChatRoom(id_room);
		    			if(room != null){
			    			String admin = room.getAdmin();
			    			if(admin.equals(sender)){
			    				String id_user = command[2];
			    				if(room.existsUser(id_user)) result.add("KickROK");
			    				else result.add("UserNotRoom");
		    					result.add(id_user);
			    			}
			    			else result.add("NotAdmin");
		    			}
		    			else result.add("RoomNotExists");
	    				result.add(id_room);
		    		}
		    		break;
	    		default:
	    			result.add("NotKnown");
	    	}
	    }
	    else{
	    	result.add("NotKnown");
	    }
	    return result;
	    
	}
}
