package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.bbdd.DBController;

public class MessageController {

	
	public ArrayList<String> checkMessage(WebSocketSession session, TextMessage message, 
				UserController userController, ChatRoomController chatController, DBController dbController) throws IOException {
	    ArrayList<String> result = new ArrayList<>();
	    
		// Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
	    // Obtener nombre de usuario
		String sender = UserController.getUsername(session);
		
		// Obtener sala activa del usuario
	    String id_active_room = dbController.getActiveRoom(sender);
	    
	    if(type.equals("chat")){
	    	if(id_active_room != null){
	    		result.add("ChatOK");
	    	}
	    	else{
	    		result.add("ChatNotOK");
	    	}
	    	// False
	    	// Send notification
	    }
	    else if(type.equals("kick")){
	    	if(id_active_room != null){
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
		    			if(!dbController.existsChat(id)) result.add("CreateOK");
		    			else result.add("RoomExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "joinroom":
		    		System.out.println("JoinRoom");
		    		if(command.length != 2)result.add("JoinNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id)) result.add("JoinOK");
		    			else result.add("RoomNotExists");
	    				result.add(id);
		    		}
		    		break;
	    		case "leaveroom":
		    		System.out.println("LeaveRoom");
		    		if(command.length != 2) result.add("LeaveNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id) && dbController.getActiveRoom(sender).equals(id)){
		    				result.add("LeaveOK");
		    			}
		    			else result.add("RoomNotExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "deleteroom":
		    		System.out.println("DeleteRoom");
		    		if(command.length != 2) result.add("DeleteNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id) && dbController.getActiveRoom(sender).equals(id)){
			    			if(dbController.isAdmin(id, sender)) result.add("DeleteOK");
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
		    			if(dbController.existsChat(id_room) && dbController.getActiveRoom(sender).equals(id_room)){
			    			if(dbController.isAdmin(id_room, sender)){
			    				String id_user = command[2];
			    				if(dbController.isUserInChat(sender, id_room)) result.add("KickROK");
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
