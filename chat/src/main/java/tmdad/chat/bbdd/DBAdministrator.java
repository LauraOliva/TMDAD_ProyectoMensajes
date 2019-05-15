package tmdad.chat.bbdd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import tmdad.chat.controller.MsgChecker.typeMessage;
import tmdad.chat.model.Chatroom;
import tmdad.chat.model.Mensaje;
import tmdad.chat.model.Usuario;

@Component
public class DBAdministrator {
	
	@Autowired
	private UsuarioRepository userRepository;
	@Autowired 
	private MensajeRepository msgRepository;
	@Autowired 
	private ChatroomRepository chatRepository;
		

	public static Map<String, WebSocketSession> userUsernameMap = new ConcurrentHashMap<>();
	
	/* Obtiene el nombre de usuario correspondiente a la sesion session */
	public static String getUsername(WebSocketSession session){
		for (Entry<String, WebSocketSession> entry : userUsernameMap.entrySet()) {
	        if (entry.getValue().equals(session)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/* TABLA USUARIO */
	
	/* Inserta un nuevo usuario en la base de datos de chat */
	public void insertUser(String username, boolean root){
		Usuario u = new Usuario(username, root, null);
		userRepository.save(u);
	}

	/* Elimina usuario con nombre de usuario username de la base de datos chat */
	public void removeUser(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		userRepository.delete(u);
	}

	/* Comprueba si el usuario con nombre de usuario username de la base de datos de chat */
	public boolean existsUser(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u == null) return false;
		return true;
	}
	
	/* Modifica la sala activa del usuario con nombre de usuario username a id_room */
	public void setActiveRoom(String username, String id_room){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null){
			u.setActiveroom(id_room);
			userRepository.save(u);
		}
	}
	
	/* Obtiene el numero de usuarios almacenados en la base de datos */
	public int getNumUsers(){
		return userRepository.findNumUsers();
	}
	
	/* Obtiene el numero de usuarios activos de la base de datos */
	public int getNumActiveUsers(){
		int u = 0;
		List<String> usernames = userRepository.findUsernames();
		for(int i = 0; i < usernames.size(); i++){
			WebSocketSession session = userUsernameMap.get(usernames.get(i));
			if(session != null && session.isOpen()){
				u++;
			}
		}
		return u;
	}
	
	/* Obtiene el nombre de la sala activa de usuario con nombre de usuarios username */
	public String getActiveRoom(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null) return u.getActiveroom();
		return null;
	}
	
	/* Comprueba si el usuario con nombre de usuario username es el administrador del sistema o superusuario */
	public boolean isRoot(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null) return u.isRoot();
		return false;
	}
	
	/* TABLA CHATROOM */
	
	/* Inserta una nueva sala en la base de datos chat */
	public void insertChat(String name, String admin, boolean multiple){
		Chatroom c = new Chatroom(admin, multiple, name);
		chatRepository.save(c);
	}

	/* Comprueba si la sala con nombre name y privada o publica (multiple) existe */ 
	public boolean existsChat(String name, boolean multiple){
		List<Chatroom> c = chatRepository.findByNameMul(name, multiple);
		if(c == null || c.isEmpty()) return false;
		return true;
	}
	
	/* Obtiene los nombres de las salas del usuario sender */
	public List<String> getRooms(String sender){
		ArrayList<String> r = new ArrayList<>();
		List<String> rooms = chatRepository.findNames();
		for(int i = 0; i < rooms.size(); i++){
			if(isUserInChat(sender, rooms.get(i))){
				r.add(rooms.get(i));
			}
		}
		return r;
		
	}
	
	/* Elimina el chat de nombre name de la base de datos chat */
	public void removeChat(String name){
		List<Chatroom> c = chatRepository.findByName(name);
		chatRepository.delete(c.get(0));
	}

	/* Comprueba si el usuario username es el administrador de la sala name */
	public boolean isAdmin(String name, String username){
		List<Chatroom> c = chatRepository.findByName(name);
		if(c != null && !c.isEmpty() && c.get(0).getAdmin().equals(username)) return true;
		return false;
	}

	/* Comprueba si la sala con nombre name es publica */
	public boolean isMultiple(String name){
		List<Chatroom> c = chatRepository.findByName(name);
		if(c != null && !c.isEmpty()) return c.get(0).isMultipleusers();
		return false;
	}
	
	/* Comprueba si el usuario username esta en la sala id_room */
	public boolean isUserInChat(String username, String id_room){		
		if(id_room == null || id_room.equals("")) return false;
		
		long time_join = 0, time_leave = 0;
		time_join = getDateJoin(username, id_room);
		
		List<Long> timestamps;
		timestamps = msgRepository.findMsgDateBySender(typeMessage.CHAT.toString(), username, "ha abandonado la sala", id_room); 
		if(timestamps != null && !timestamps.isEmpty()) time_leave = timestamps.get(0);
		else{
			timestamps = msgRepository.findMsgDate(typeMessage.CHAT.toString(), id_room, "(Administrador) ha expulsado de la sala " + id_room + " a " + username); 
			if(timestamps != null && !timestamps.isEmpty()) time_leave = timestamps.get(0);
		}
		
		if (time_join == 0) return false;
		else if(time_join != 0 && time_leave == 0) return true;
		else if(time_join < time_leave) return false;
		else if(time_join >= time_leave) return true;
		else return false;
	}
	
	/* Devuelve la lista de usuarios que pertenecen a la sala id_room */
	public ArrayList<String> getUsersRoom(String id_room){
		ArrayList<String> u = new ArrayList<>();
		List<String> usernames = userRepository.findUsernames();
		for(int i = 0; i < usernames.size(); i++){
			if(isUserInChat(usernames.get(i), id_room)){
				u.add(usernames.get(i));
			}
		}
		return u;
	}
	
	/* Devuelve el numero de usuarios de la sala id_room */
	public int getNumUsersRoom(String id_room){
		int numUsers = 0;
		List<String> usernames = userRepository.findUsernames();
		for(int i = 0; i < usernames.size(); i++){
			if(isUserInChat(usernames.get(i), id_room)){
				numUsers++;
			}
		}
		return numUsers;
	}
	
	/* TABLA MENSAJE */
	
	/* Inserta un nuevo mensaje en la base de datos de chat */
	public void insertMsg(String sender, String dst, long timestamp, String msg, String type){
		Mensaje m = new Mensaje(sender, dst, timestamp, msg, type);
		msgRepository.save(m);
	}

	/* Devuelve una lista de mensajes de la sala con nombre id */
	public ArrayList<String> getMsg(String id, String type, String username, boolean multiple){
		List<Mensaje> mensajes;
		ArrayList<String> msgs = new ArrayList<>();
		if(type.equals("chat") && multiple){
			long time = getDateJoin(username, id);
			mensajes = msgRepository.findMsgChat(type, id, time, multiple); 
		}
		else{
			mensajes = msgRepository.findMsg(type, id); 
		}
		
		for(int i = 0; i < mensajes.size(); i++){
			Mensaje mensaje = mensajes.get(i);
			String s = mensaje.getSender();
	    	long t = mensaje.getTimestamp();
	    	String m = mensaje.getMsg();
	    	String timestamp = new SimpleDateFormat("HH:mm").format(t);	
	    	String msg = "<b>" + s + ":</b> " + m + " (" + timestamp + ")";
	    	msgs.add(msg);
		}
		return msgs;

	}
	
	/* Elimina los mensajes de la sala con nombre id_room de la base de datos chat */
	public void removeMsgRoom(String id_room){
		List<Mensaje> mensajes = msgRepository.findMsg(typeMessage.CHAT.toString(), id_room); 
		for(int i = 0; i < mensajes.size(); i++){
			Mensaje mensaje = mensajes.get(i);
			msgRepository.delete(mensaje);
		}
	}
	
	/* Devuelve el timestamp en el que se unio el usuario username a la sala id_room */
	public long getDateJoin(String username, String id_room){
		List<Long> timestamps = msgRepository.findMsgDateBySender(typeMessage.CHAT.toString(), username, "se ha unido a la sala", id_room ); 
		if(!timestamps.isEmpty()) return timestamps.get(0);
		else return 0;
	}
	
	/* Devuelve true si el usuario username ha sido invitado a la sala id_room */
	public boolean hasBeenInvited(String username, String id_room){
		long time_inv = 0, time_leave = 0;
		List<Long> timestamps;
		timestamps = msgRepository.findMsgDate(typeMessage.CHAT.toString(), id_room, "Se ha invitado a unirse a la sala a " + username ); 
		if(timestamps != null && !timestamps.isEmpty()) time_inv = timestamps.get(0);
		
		timestamps = msgRepository.findMsgDateBySender(typeMessage.CHAT.toString(), username, "ha abandonado la sala", id_room); 
		if(timestamps != null && !timestamps.isEmpty()) time_leave = timestamps.get(0);
		else{
			timestamps = msgRepository.findMsgDate(typeMessage.CHAT.toString(), id_room, "(Administrador) ha expulsado de la sala " + id_room + " a " + username); 
			if(timestamps != null && !timestamps.isEmpty()) time_leave = timestamps.get(0);
		}

		if (time_inv == 0) return false;
		else if(time_inv != 0 && time_leave == 0) return true;
		else if(time_inv < time_leave) return false;
		else if(time_inv >= time_leave) return true;
		else return false;
	}
	
	
}
