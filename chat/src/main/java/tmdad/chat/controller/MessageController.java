package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.bbdd.DBController;

public class MessageController {

	
	public ArrayList<String> checkMessage(WebSocketSession session, TextMessage message, 
				String sender, DBController dbController) throws IOException {
	    ArrayList<String> result = new ArrayList<>();
	    
		// Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toLowerCase();
	    
		
		// Obtener sala activa del usuario
	    String id_active_room = dbController.getActiveRoom(sender);
	    
	    System.out.println("Tipo = " + type);
	    
	    if(type.equals("chat")){
	    	if(id_active_room != null){
	    		result.add("ChatOK");
	    		result.add(id_active_room);
	    	}
	    	else{
	    		result.add("NoActiveRoom");
	    	}
	    	// False
	    	// Send notification
	    }
	    else if(type.equals("verify")){
	    	String username = payload.getString("content").trim();
			UserController.userUsernameMap.put(username, session);
			String activeRoom = null;
			if(dbController.existsUser(username)){				
				activeRoom = dbController.getActiveRoom(username);
				if(activeRoom != null){
					result.add("VerifyActive");
			    	result.add(username);
			    	result.add(activeRoom);
				}
				else{
					result.add("VerifyNotActive");
			    	result.add(username);
				}
			}
			else{
				dbController.insertUser(username, "1234", true);
				result.add("VerifyNotActive");
		    	result.add(username);
			}
	    	/* TODO añadir contraseña */
	    }
	    else if(type.equals("kick")){
	    	if(id_active_room != null){
	    		result.add("KickOK");
	    		result.add(id_active_room);
	    	}
	    	else{
	    		result.add("NoActiveRoom");
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
		    			if(!dbController.existsChat(id, true)){
		        			dbController.insertChat(id, sender, true);
		        	    	dbController.setActiveRoom(sender, id);
		    				result.add("CreateOK");
		    			}
		    			else result.add("RoomExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "chatuser":
	    			// Comprobar si ya han hablado otras veces
	    			if(command.length != 2)result.add("ChatUserNotOK");
	    			else{
	    				String user2 = command[1];
		    			String possible_name_1 = sender + "-" + user2;
		    			String possible_name_2 = user2 + "-" + sender;	
		    			// Comprobar que existe usuario
		    			if(dbController.existsUser(user2)){
			    			// Si si -> recuperar mensajes
			    			if(dbController.existsChat(possible_name_1, false)){
			    				result.add("ChatUserMsg");
			    				result.add(possible_name_1);
			    				id_active_room = possible_name_1;
			    			}
			    			else if(dbController.existsChat(possible_name_2, false)){
			    				result.add("ChatUserMsg");
			    				result.add(possible_name_2);
			    				id_active_room = possible_name_2;
			    			}
			    			// Si no -> crear sala 
			    			else{
			    				dbController.insertChat(possible_name_1, sender, false);
			    				result.add("ChatUserCreate");
			    				result.add(possible_name_1);
			    				result.add(user2);
			    			}
			    			dbController.setActiveRoom(sender, id_active_room);
		    			}
		    			else{
		    				result.add("UserNotExists");
		    				result.add(user2);
		    			}
	    			}
	    			break;
	    		case "joinroom":
		    		System.out.println("JoinRoom");
		    		if(command.length != 2)result.add("JoinNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id, true)){
		        	    	dbController.setActiveRoom(sender, id);
		    				result.add("JoinOK");
		    			}
		    			else result.add("RoomNotExists");
	    				result.add(id);
		    		}
		    		break;
	    		case "leaveroom":
		    		System.out.println("LeaveRoom");
		    		if(command.length != 2) result.add("LeaveNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id, true) && dbController.getActiveRoom(sender).equals(id)){
		        	    	dbController.removeActiveRoom(sender);
		    				result.add("LeaveOK");
		    			}
		    			else result.add("RoomNotExists");
		    			result.add(id);
		    		}
		    		break;
	    		case "closeroom":
	    			if(command.length != 1)result.add("CloseNotOK");
	    			else{
	    				if(id_active_room != null){
		    	    		result.add("CloseOK");
		    	    		result.add(id_active_room);
		    	    		dbController.removeActiveRoom(sender);
		    	    	}
		    	    	else{
		    	    		result.add("NoActiveRoom");
		    	    	}
	    			}
	    			break;
	    		case "openroom":
	    			if(command.length != 2)result.add("OpenNotOK");
	    			else{
	    				String id = command[1];
		    			long joinRoom = dbController.getDateJoin(sender, id);
		    			// Comprobar que ya se ha unido a esa sala
		    			if(joinRoom == 0){
		    				// Nunca se ha unido a la sala
		    				result.add("NotInvited");
		    			}
		    			else{
		    				result.add("OpenOK");
		    				dbController.setActiveRoom(sender, id);
		    			}
	    				result.add(id);
	    			}
	    			break;
	    		case "deleteroom":
		    		System.out.println("DeleteRoom");
		    		if(command.length != 2) result.add("DeleteNotOK");
		    		else{
		    			String id = command[1];
		    			if(dbController.existsChat(id, true) && dbController.getActiveRoom(sender).equals(id)){
			    			if(dbController.isAdmin(id, sender)){
			    				dbController.removeActiveRoom(sender);
			        	    	dbController.removeChat(id);
			    				result.add("DeleteOK");
			    			}
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
		    			if(dbController.existsChat(id_room, true) && dbController.getActiveRoom(sender).equals(id_room)){
			    			if(dbController.isAdmin(id_room, sender)){
			    				String id_user = command[2];
			    				if(dbController.isUserInChat(sender, id_room)){
			    	    			dbController.removeActiveRoom(id_user);
			    					result.add("KickROK");
			    				}
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
