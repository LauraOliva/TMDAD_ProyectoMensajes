package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.bbdd.DBAdministrator;

public class MessageParser {
	public static enum commands { CREATEROOM, CHATUSER, JOINROOM, LEAVEROOM, 
		CLOSEROOM, OPENROOM, INVITEROOM, DELETEROOM, KICKROOM }
	public static enum typeMessage { CHAT, VERIFY, KICK, COMMAND}
	public static enum reply { CHATOK, VERIFYACTIVE, VERIFYNOTACTIVE, KICKOK, NOTKNOWN, WRONGCOMMAND, NOTADMIN,
		ROOMNOTEXISTS, ROOMEXISTS, USERINROOM, CREATEOK, CHATUSERMSG, CHATUSERCREATE, USERNOTEXISTS, JOINOK, 
		NOTINVITED, LEAVEOK, CLOSEOK, OPENOK, INVITEOK, DELETEOK, KICKROK, USERNOTROOM, NOACTIVEROOM}
	
	private ArrayList<String> checkCreate(String id, DBAdministrator dbAdministrator, String sender){
		ArrayList<String> result = new ArrayList<String>();

		if(!dbAdministrator.existsChat(id, true)){
			dbAdministrator.insertChat(id, sender, true);
	    	dbAdministrator.setActiveRoom(sender, id);
			result.add(reply.CREATEOK.toString());
		}
		else result.add(reply.ROOMEXISTS.toString());
		result.add(id);
		
		return result;
	}
	
	public ArrayList<String> checkMessage(WebSocketSession session, TextMessage message, 
				String sender, DBAdministrator dbAdministrator) throws IOException {
	    ArrayList<String> result = new ArrayList<>();
	    
		// Leer el json para saber que tipo de mensaje es
	    JSONObject payload = new JSONObject(message.getPayload());
	    String type = payload.getString("type").trim().toUpperCase();
	    
		
		// Obtener sala activa del usuario
	    System.out.println("Semder " + sender);
	    String id_active_room = null;
	    if(sender != null) id_active_room = dbAdministrator.getActiveRoom(sender);
	    

	    System.out.println("Tipo = " + type);
	    System.out.println("Sala = " + id_active_room);
	    System.out.println("Sender = " + sender);
	    
	    if((type.equals(typeMessage.CHAT.toString()) || type.equals(typeMessage.KICK.toString())) 
	    	&& id_active_room == null){
	    		result.add(reply.NOACTIVEROOM.toString());
	    		return result;
	    }
	    
	    if(type.equals(typeMessage.CHAT.toString())){
    		result.add(reply.CHATOK.toString());
    		result.add(id_active_room);

	    }
	    else if(type.equals(typeMessage.VERIFY.toString())){
	    	String username = payload.getString("content").trim();
	    	id_active_room = dbAdministrator.getActiveRoom(username);
			UserController.userUsernameMap.put(username, session);
			if(dbAdministrator.existsUser(username) && id_active_room != null){
				result.add(reply.VERIFYACTIVE.toString());
		    	result.add(username);
		    	result.add(id_active_room);
			}
			else{
				dbAdministrator.insertUser(username, "1234", true);
				result.add(reply.VERIFYNOTACTIVE.toString());
		    	result.add(username);
			}
	    	/* TODO añadir contraseña */
	    }
	    else if(type.equals(typeMessage.KICK.toString())){
	    	result.add(reply.KICKOK.toString());
    		result.add(id_active_room);	    	
	    }
	    else if(type.equals(typeMessage.COMMAND.toString())){
	    	String content = payload.getString("content").trim();
	    	/*
	    	 * Estructura de los comandos
	    	 * COMMAND parameter [, parameter]
	    	 */
	    	
	    	String[] command = content.split("\\s+");
	    	commands cmd;
	    	try{
	    		cmd = commands.valueOf(command[0].toUpperCase());
	    	} catch (IllegalArgumentException e){
	    		result.add(reply.NOTKNOWN.toString());
    			return result;
	    	}

	    	String id_room = "", id_user = "";
	    	
	    	/* Comprobar que los comandos son correctos y que los puede realizar ese usuario */

	    	// Comprobar la longitud de los comandos 
	    	if(command.length != 2 && (!cmd.equals(commands.KICKROOM) && !cmd.equals(commands.INVITEROOM) && !cmd.equals(commands.CLOSEROOM))
	    			|| (command.length != 3 && (cmd.equals(commands.KICKROOM) || cmd.equals(commands.INVITEROOM)))
	    			|| (command.length != 1 && cmd.equals(commands.CLOSEROOM))){
	    		result.add(reply.WRONGCOMMAND.toString());
	    		return result;
	    	}
	    	
	    	// Comprobar que si el usuario intenta invitar, borrar o echar a alguien de una sala, sea el administrador de la sala
	    	if((cmd.equals(commands.INVITEROOM) || cmd.equals(commands.DELETEROOM) || cmd.equals(commands.KICKROOM))
	    			&& !dbAdministrator.isAdmin(command[1], sender)){
	    		result.add(reply.NOTADMIN.toString());
				result.add(command[1]);
	    		return result;
	    	}
	    	
	    	// Comprobar que la sala existe en caso de que el usuario quiera borrarla, dejarla, unirse, invitar a alguien o echar a alguien
	    	if(((cmd.equals(commands.DELETEROOM) || cmd.equals(commands.KICKROOM)|| cmd.equals(commands.LEAVEROOM)
	    		|| cmd.equals(commands.JOINROOM) || cmd.equals(commands.INVITEROOM)))	
	    			&& !dbAdministrator.existsChat(command[1], true)){
	    		result.add(reply.ROOMNOTEXISTS.toString());
				result.add(command[1]);
				return result;
	    	}
	    	
	    	// Comprobar que si se quiere unir un usuario o si se quiere invitar a un usuario que éste no esté en la sala
	    	if((cmd.equals(commands.JOINROOM) || cmd.equals(commands.INVITEROOM)) && dbAdministrator.isUserInChat(sender, id_room)){
	    		result.add(reply.USERINROOM.toString());
				if (cmd.equals(commands.INVITEROOM)) result.add(command[2]);
				else result.add(sender);
				return result;
	    	}
	    		
	    	
	    	switch(cmd){
	    		case CREATEROOM:
		    		System.out.println("CreateRoom");
		    		result = checkCreate(command[1], dbAdministrator, sender);
		    		break;
	    		case CHATUSER:

    				String user2 = command[1];
	    			String possible_name_1 = sender + "-" + user2;
	    			String possible_name_2 = user2 + "-" + sender;	
	    			// Comprobar que existe usuario
	    			if(dbAdministrator.existsUser(user2)){
		    			// Si si -> recuperar mensajes
		    			if(dbAdministrator.existsChat(possible_name_1, false)){
		    				result.add(reply.CHATUSERMSG.toString());
		    				result.add(possible_name_1);
		    				id_active_room = possible_name_1;
		    			}
		    			else if(dbAdministrator.existsChat(possible_name_2, false)){
		    				result.add(reply.CHATUSERMSG.toString());
		    				result.add(possible_name_2);
		    				id_active_room = possible_name_2;
		    			}
		    			// Si no -> crear sala 
		    			else{
		    				dbAdministrator.insertChat(possible_name_1, sender, false);
		    				id_active_room = possible_name_1;
		    				result.add(reply.CHATUSERCREATE.toString());
		    				result.add(possible_name_1);
		    				result.add(user2);
		    			}
		    			dbAdministrator.setActiveRoom(sender, id_active_room);
	    			}
	    			else{
	    				result.add(reply.USERNOTEXISTS.toString());
	    				result.add(user2);
	    			}
	    			
	    			break;
	    		case JOINROOM:
		    		System.out.println("JoinRoom");
		    		id_room = command[1];
    				if(dbAdministrator.hasBeenInvited(sender, id_room)){
	        	    	dbAdministrator.setActiveRoom(sender, id_room);
	        	    	result.add(reply.JOINOK.toString());
	    				result.add(id_room);

    				}
    				else{
    					result.add(reply.NOTINVITED.toString());
	    				result.add(id_room);
    				}
		    		break;
	    		case LEAVEROOM:
	    			id_room = command[1];
        	    	result.add(reply.LEAVEOK.toString());
	    			result.add(id_room);
		    		break;
	    		case CLOSEROOM:
    				if(id_active_room != null){
	    	    		//dbAdministrator.removeActiveRoom(sender);
    					dbAdministrator.setActiveRoom(sender, null);
	    	    	}
    				result.add(reply.CLOSEOK.toString());
	    			break;
	    		case OPENROOM:
	    			id_room = command[1];
	    			long joinRoom = dbAdministrator.getDateJoin(sender, id_room);
	    			// Comprobar que ya se ha unido a esa sala
	    			if(joinRoom == 0){
	    				// Nunca se ha unido a la sala
	    				result.add(reply.NOTINVITED.toString());
	    			}
	    			else{
	    				result.add(reply.OPENOK.toString());
	    				dbAdministrator.setActiveRoom(sender, id_room);
	    			}
    				result.add(id_room);
	    			
	    			break;
	    		case INVITEROOM:
	    			System.out.println("inviteroom");
		    		id_room = command[1];
    				id_user = command[2];
    				result.add(reply.INVITEOK.toString());
					result.add(id_user);
					result.add(id_room);
	    			break;
	    		case DELETEROOM:
		    		System.out.println("DeleteRoom");
		    		String id = command[1];
    				//dbAdministrator.removeActiveRoom(sender);
		    		dbAdministrator.setActiveRoom(sender, null);
        	    	dbAdministrator.removeChat(id);
        	    	result.add(reply.DELETEOK.toString());
    				result.add(id);
		    		
		    		break;
	    		case KICKROOM:
		    		System.out.println("KickRoom");
		    		id_room = command[1];
    				id_user = command[2];
    				if(dbAdministrator.isUserInChat(sender, id_room)){
    	    			//dbAdministrator.removeActiveRoom(id_user);
    					dbAdministrator.setActiveRoom(id_user, null);
    	    			result.add(reply.KICKROK.toString());
    				}
    				else result.add(reply.USERNOTROOM.toString());
					result.add(id_user);
    				result.add(id_room);
		    		break;
	    		default:
	    			break;
	    	}
	    }
	    else{
	    	result.add(reply.NOTKNOWN.toString());
	    }
	    return result;
	    
	}
}
