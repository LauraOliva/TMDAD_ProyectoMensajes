package tmdad.chat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.bbdd.DBAdministrator;

public class MsgChecker {
	/* Posibles comandos */
	public static enum commands { HELP, CREATEROOM, CHATUSER, JOINROOM, LEAVEROOM, 
		CLOSEROOM, OPENROOM, INVITEROOM, DELETEROOM, KICKROOM, BROADCAST, GETROOMS, GETUSERSROOM,
		ADDCENSURE, REMOVECENSURE, GETCENSURE}
	/* Tipos de mensajes */
	public static enum typeMessage { CHAT, VERIFY, COMMAND, NOTIFICATION, CLEAN}
	/* Posibles respuestas */
	public static enum reply {HELPOK, CHATOK, VERIFYOK, VERIFYNOK, NOTKNOWN, WRONGCOMMAND, NOTADMIN,
		ROOMNOTEXISTS, ROOMEXISTS, USERINROOM, CREATEOK, CHATUSERMSG, CHATUSERCREATE, USERNOTEXISTS, JOINOK, 
		NOTINVITED, LEAVEOK, CLOSEOK, OPENOK, INVITEOK, DELETEOK, KICKROK, USERNOTROOM, NOACTIVEROOM, NOTROOT, 
		BROADCASTOK, ROOMSOK, USERSROOMOK, ADDCENSUREOK, REMCENSUREOK, GETCENSUREOK}

	private static final int MAX_NUM_USERS = 100;
	/* Numero de usuarios del sistema */
	private final AtomicInteger counter_users = new AtomicInteger(0);
	/* N�mero de usuarios activos */
	public static final AtomicInteger counter_active = new AtomicInteger(0);
	
	/* Devuelve el n�mero de usuarios registrados */
	public int getNumUsers() {
        return counter_users.get();
    }
	
	/*Devuelve el n�mero de usuarios activos */
	public int getNumActiveUsers(){
		return counter_active.get();
	}
	
	/* Env�a a la sesi�n sesion los mensajes de la sala id_room almacenados en la base de datos */
	public void getMsgRoom(WebSocketSession session, String id_room, DBAdministrator dbAdministrator){
		
		boolean multiple = dbAdministrator.isMultiple(id_room);
		
		ArrayList<String> msg = dbAdministrator.getMsg(id_room, typeMessage.CHAT.toString(), DBAdministrator.getUsername(session), multiple);
		if(session.isOpen()){
			JSONObject message = new JSONObject();
			message.put("type", typeMessage.CLEAN.toString());
			try {
				session.sendMessage(new TextMessage(message.toString()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			message.put("type", typeMessage.CHAT.toString());
			TextMessage m;
			for(int i = 0; i < msg.size(); i++){
				message.put("content", msg.get(i));
				try {
					m = new TextMessage(message.toString());
					session.sendMessage(m);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
	
	/* Devuelve las salas a las que pertenece el usuario sender */
	private String getRooms(String sender, DBAdministrator dbAdministrator){
		String rooms = "";
		List<String> r = dbAdministrator.getRooms(sender);
		if(r.isEmpty()){
			rooms = "No tienes salas";
		}
		for(int i = 0; i < r.size(); i++){
			rooms = rooms + "<div>" + r.get(i) + "</div>";
		}
		
		return rooms;
	}
	
	/* Devuelve los usuarios de la sala id_room */
	private String getUsersRoom(String id_room, String sender, DBAdministrator dbAdministrator){
		String users = "";
		
		if(!dbAdministrator.isUserInChat(sender, id_room)){
			users = "No estas en la sala " + id_room;
		}
		else{
			ArrayList<String> u = dbAdministrator.getUsersRoom(id_room);
			for(int i = 0; i < u.size(); i++){
				users = users + "<div>" + u.get(i) + "</div>";
			}
		}
		return users;
	}
	
	/* Devuelve un string que contiene la ayuda necesaria para ejecutar los diferentes comandos */
	private String getHelp(){
		String help = "";
		for (commands cmd : commands.values()) { 
			if(cmd.equals(commands.KICKROOM) || cmd.equals(commands.INVITEROOM)){
				help += "<div><b>" + cmd.toString() + "</b> id_room id_user </div>";
			}
			else if(cmd.equals(commands.CLOSEROOM) || cmd.equals(commands.HELP) || cmd.equals(commands.GETROOMS)
					|| cmd.equals(commands.GETCENSURE)){
				help += "<div><b>" + cmd.toString() + "</b> </div>";
			}
			else if(cmd.equals(commands.CHATUSER)){
				help += "<div><b>" + cmd.toString() + "</b> id_user </div>";
			}
			else if(cmd.equals(commands.BROADCAST)){
				help += "<div><b>" + cmd.toString() + "</b> msg </div>";
			}
			else if(cmd.equals(commands.ADDCENSURE) || cmd.equals(commands.REMOVECENSURE)){
				help += "<div><b>" + cmd.toString() + "</b> word </div>";
			}
			else{
				help += "<div><b>" + cmd.toString() + "</b> id_room </div>";
	    	}
        }
		return help;
	}
	
	/* Comprueba si se puede crear una nueva sala. Si es posible crea la sala */
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
	
	/* Devuelve true si y solo si el comando tiene el n�mero de par�metros correcto */
	private boolean checkLengthCommand(String[] command, commands cmd){
		return (command.length != 2 && (!cmd.equals(commands.KICKROOM) && !cmd.equals(commands.INVITEROOM) 
    			&& !cmd.equals(commands.CLOSEROOM) && !cmd.equals(commands.HELP) && !cmd.equals(commands.BROADCAST) 
    			&& !cmd.equals(commands.GETROOMS) && !cmd.equals(commands.GETCENSURE))
    			|| (command.length != 3 && (cmd.equals(commands.KICKROOM) || cmd.equals(commands.INVITEROOM)))
    			|| (command.length != 1 && (cmd.equals(commands.CLOSEROOM) || cmd.equals(commands.HELP) || cmd.equals(commands.GETROOMS)
    					|| cmd.equals(commands.GETCENSURE))
    			|| (command.length < 2 && cmd.equals(commands.BROADCAST))));
	}
	
	/* Devuelve true si y solo si el comando cmd solo puede ser ejecutado por el administrador de la sala y lo esta
	 * intentando ejecutar un usuario que no es el administrador de la sala*/
	private boolean checkAdmin(String[] command, commands cmd, String sender, DBAdministrator dbAdministrator){
		return ((cmd.equals(commands.INVITEROOM) || cmd.equals(commands.DELETEROOM) || cmd.equals(commands.KICKROOM))
    			&& !dbAdministrator.isAdmin(command[1], sender));
	}
	
	/* Devuelve true si y solo si el comando command solo puede ser ejecutado sobre una sala existente y la 
	 * sala no existe
	 */
	private boolean checkRoom(String[] command, commands cmd, DBAdministrator dbAdministrator){
		return (((cmd.equals(commands.DELETEROOM) || cmd.equals(commands.KICKROOM)|| cmd.equals(commands.LEAVEROOM)
	    		|| cmd.equals(commands.JOINROOM) || cmd.equals(commands.INVITEROOM)))	
    			&& !dbAdministrator.existsChat(command[1], true));
	}
	
	/* Debuelbe true si y solo si el un usuario se intenta unir a una sala en la que ya est� o si se intenta invitar a 
	 * un usuario que ya esta en la sala 
	 */
	private boolean checkInvitation(commands cmd, DBAdministrator dbAdministrator, String sender, String id_room){
		return ((cmd.equals(commands.JOINROOM) || cmd.equals(commands.INVITEROOM)) && dbAdministrator.isUserInChat(sender, id_room));
	}
	
	/* Devuelve true si y solo si el comando cmd s�lo puede ser ejecutado por el administrador del sistema
	 * y lo est� intentando ejecutar un usuario que no es el administrador
	 */
	private boolean checkRoot(commands cmd, DBAdministrator dbAdministrator, String sender){
		return ((cmd.equals(commands.BROADCAST) || cmd.equals(commands.ADDCENSURE) || cmd.equals(commands.REMOVECENSURE)
				|| cmd.equals(commands.GETCENSURE)) && !dbAdministrator.isRoot(sender));
	}
	
	/* Comprueba si un usuario existe o si un usuario nuevo puede ser creado (el numero de usuarios registrados no 
	 * supera 100.
	 */
	private ArrayList<String> verify(WebSocketSession session, String username, DBAdministrator dbAdministrator){
		ArrayList<String> result = new ArrayList<String>();
		DBAdministrator.userUsernameMap.put(username, session);
		int numUsers;

		if(dbAdministrator.existsUser(username)){
			while(true) {
	            int existingValue = getNumActiveUsers();
	            int newValue = dbAdministrator.getNumActiveUsers();
	            if(counter_active.compareAndSet(existingValue, newValue)) {
	                break;
	            }
	        }
			dbAdministrator.setActiveRoom(username, null);
			result.add(reply.VERIFYOK.toString());
	    	result.add(username);
		}
		else{
			while(true) {
	            int existingValue = getNumUsers();
	            int newValue = dbAdministrator.getNumUsers()+1;
	            if(counter_users.compareAndSet(existingValue, newValue)) {
	            	numUsers = newValue;
	                break;
	            }
	        }
			if (numUsers > MAX_NUM_USERS){
				result.add(reply.VERIFYNOK.toString());
			}
			else{
				while(true) {
		            int existingValue = getNumActiveUsers();
		            int newValue = dbAdministrator.getNumActiveUsers();
		            if(counter_active.compareAndSet(existingValue, newValue)) {
		                break;
		            }
		        }
				boolean isRoot = false;
				if(username.equals("root")) isRoot = true;
				dbAdministrator.insertUser(username, isRoot);
				result.add(reply.VERIFYOK.toString());
		    	result.add(username);
			}
			
		}
		return result;
	}
	
	/* Comprueba si dos usuarios ya tienen una sala privada creada. Si si, se recuperan los mensajes.
	 * Si no, se crea una nueva sala.
	 */
	private ArrayList<String> checkChatUser(WebSocketSession session, String user2, String sender, DBAdministrator dbAdministrator){
		ArrayList<String> result = new ArrayList<String>();
		String id_active_room = "";
		String possible_name_1 = sender + "-" + user2;
		String possible_name_2 = user2 + "-" + sender;	
		// Comprobar que existe usuario
		if(dbAdministrator.existsUser(user2)){
			// Si si -> recuperar mensajes
			if(dbAdministrator.existsChat(possible_name_1, false)){
				result.add(reply.CHATUSERMSG.toString());
				result.add(possible_name_1);
				id_active_room = possible_name_1;
				getMsgRoom(session, possible_name_1, dbAdministrator);
			}
			else if(dbAdministrator.existsChat(possible_name_2, false)){
				result.add(reply.CHATUSERMSG.toString());
				result.add(possible_name_2);
				id_active_room = possible_name_2;
				getMsgRoom(session, possible_name_2, dbAdministrator);
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
		return result;
	}
	
	/* Comprueba que el mensaje es correcto. Las comprobaciones dependen del tipo de mensaje.
	 * Devuelve una lista de strings con los datos necesarios para el procesado correcto del mensaje.
	 */
	public ArrayList<String> checkMessage(WebSocketSession session, TextMessage message, 
				String sender, DBAdministrator dbAdministrator) throws IOException {
	    ArrayList<String> result = new ArrayList<>();
	    
		// Leer el json para saber que tipo de mensaje es
		JSONObject payload = null;
		payload = new JSONObject(message.getPayload());
		
	    String type = payload.getString("type").trim().toUpperCase();
	    String content = payload.getString("content").trim();
	    
		// Obtener sala activa del usuario
	    String id_active_room = null;
	    if(sender != null) id_active_room = dbAdministrator.getActiveRoom(sender);

	    if((type.equals(typeMessage.CHAT.toString())) 
	    	&& id_active_room == null){
	    		result.add(reply.NOACTIVEROOM.toString());
	    		return result;
	    }
	    
	    if(type.equals(typeMessage.CHAT.toString())){
    		result.add(reply.CHATOK.toString());
    		result.add(id_active_room);

	    }
	    else if(type.equals(typeMessage.VERIFY.toString())){
	    	result = verify(session, payload.getString("content").trim(), dbAdministrator);
	    }
	    else if(type.equals(typeMessage.COMMAND.toString())){
	    	
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
	    	if(checkLengthCommand(command, cmd)){
	    		result.add(reply.WRONGCOMMAND.toString());
	    		return result;
	    	}
	    	
	    	// Comprobar que si el usuario intenta invitar, borrar o echar a alguien de una sala, sea el administrador de la sala
	    	if(checkAdmin(command, cmd, sender, dbAdministrator)){
	    		result.add(reply.NOTADMIN.toString());
				result.add(command[1]);
	    		return result;
	    	}
	    	
	    	// Comprobar que la sala existe en caso de que el usuario quiera borrarla, dejarla, unirse, invitar a alguien o echar a alguien
	    	if(checkRoom(command, cmd, dbAdministrator)){
	    		result.add(reply.ROOMNOTEXISTS.toString());
				result.add(command[1]);
				return result;
	    	}
	    	
	    	// Comprobar que si se quiere unir un usuario o si se quiere invitar a un usuario que �ste no est� en la sala
	    	if(checkInvitation(cmd, dbAdministrator, sender, id_room)){
	    		result.add(reply.USERINROOM.toString());
				if (cmd.equals(commands.INVITEROOM)) result.add(command[2]);
				else result.add(sender);
				return result;
	    	}
	    	
	    	if(checkRoot(cmd, dbAdministrator, sender)){
	    		result.add(reply.NOTROOT.toString());
	    		return result;
	    	}
	    	
	    	switch(cmd){
	    		case HELP:
	    			String help = getHelp();
        	    	result.add(reply.HELPOK.toString());
    				result.add(help);
	    			break;
	    		case GETROOMS:
	    			String rooms = getRooms(sender, dbAdministrator);
	    			result.add(reply.ROOMSOK.toString());
	    			result.add(rooms);
	    			break;
	    		case GETUSERSROOM:
	    			String users = getUsersRoom(command[1], sender, dbAdministrator);
	    			result.add(reply.USERSROOMOK.toString());
	    			result.add(users);
	    			break;
	    		case BROADCAST:
    				result.add(reply.BROADCASTOK.toString());
    				String [] aux = content.split(" ", 2);
    				result.add(aux[1]);
	    			break;
	    		case CREATEROOM:
		    		result = checkCreate(command[1], dbAdministrator, sender);
		    		break;
	    		case CHATUSER:
    				result = checkChatUser(session, command[1], sender, dbAdministrator);
	    			break;
	    		case JOINROOM:
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
	    			if(id_room.equals(id_active_room)){
	    				dbAdministrator.setActiveRoom(sender, null);
	    			}
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
	    			// Comprobar que ya se ha unido a esa sala
	    			if(!dbAdministrator.isUserInChat(sender, id_room)) result.add(reply.NOTINVITED.toString());
	    			else{
	    				result.add(reply.OPENOK.toString());
	    				dbAdministrator.setActiveRoom(sender, id_room);
	        			getMsgRoom(session, id_room, dbAdministrator);
	    			}
    				result.add(id_room);
	    			
	    			break;
	    		case INVITEROOM:
		    		id_room = command[1];
    				id_user = command[2];
    				result.add(reply.INVITEOK.toString());
					result.add(id_user);
					result.add(id_room);
	    			break;
	    		case DELETEROOM:
		    		String id = command[1];
    				//dbAdministrator.removeActiveRoom(sender);
		    		//dbAdministrator.setActiveRoom(sender, null);
		    		ArrayList<String> users_room= dbAdministrator.getUsersRoom(id);
		    		
        	    	dbAdministrator.removeChat(id);
		    		dbAdministrator.removeMsgRoom(id);
        	    	result.add(reply.DELETEOK.toString());
    				result.add(id);
    				result.addAll(users_room);
		    		
		    		break;
	    		case KICKROOM:
		    		id_room = command[1];
    				id_user = command[2];
    				if(dbAdministrator.isUserInChat(sender, id_room)){
    	    			//dbAdministrator.removeActiveRoom(id_user);
    					if(dbAdministrator.getActiveRoom(id_user).equals(id_room))
    						dbAdministrator.setActiveRoom(id_user, null);
    	    			result.add(reply.KICKROK.toString());
    				}
    				else result.add(reply.USERNOTROOM.toString());
					result.add(id_user);
    				result.add(id_room);
		    		break;
	    		case ADDCENSURE:
	    			result.add(reply.ADDCENSUREOK.toString());
	    			result.add(command[1]);
	    			break;
	    		case REMOVECENSURE:
	    			result.add(reply.REMCENSUREOK.toString());
	    			result.add(command[1]);
	    			break;
	    		case GETCENSURE:
	    			result.add(reply.GETCENSUREOK.toString());
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
