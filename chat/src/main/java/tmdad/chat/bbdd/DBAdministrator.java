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

import tmdad.chat.controller.CommandChecker.typeMessage;
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
	
	public static String getUsername(WebSocketSession session){
		for (Entry<String, WebSocketSession> entry : userUsernameMap.entrySet()) {
	        if (entry.getValue().equals(session)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/* TABLA USUARIO */
	
	public void insertUser(String username, String pass, boolean root){
		Usuario u = new Usuario(username, pass, root, null);
		userRepository.save(u);
	}
	
	public void removeUser(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		userRepository.delete(u);
	}

	public boolean existsUser(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u == null) return false;
		return true;
	}
	
	public boolean verifyUser(String username, String pass){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null && u.getPassword().equals(pass)) return true;
		return false;
	}
	
	public void setActiveRoom(String username, String id_room){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null){
			u.setActiveroom(id_room);
			userRepository.save(u);
		}
	}
	
	public String getActiveRoom(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null) return u.getActiveroom();
		return null;
	}
	
	public boolean isRoot(String username){
		Usuario u = userRepository.findById(username).orElse(null);
		if(u != null) return u.isRoot();
		return false;
	}
	
	public ArrayList<String> getUsersChat(String id_activeChat){
		ArrayList<String> users = new ArrayList<String>();
		users.addAll(userRepository.findByChat(id_activeChat));
		return users;
	}
	
	/* TABLA CHATROOM */
	
	public void insertChat(String name, String admin, boolean multiple){
		Chatroom c = new Chatroom(admin, multiple, name);
		chatRepository.save(c);
	}

	public boolean existsChat(String name, boolean multiple){
		List<Chatroom> c = chatRepository.findByNameMul(name, multiple);
		if(c == null || c.isEmpty()) return false;
		return true;
	}
	
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
	
	public void removeChat(String name){
		List<Chatroom> c = chatRepository.findByName(name);
		chatRepository.delete(c.get(0));
	}

	public boolean isAdmin(String name, String username){
		List<Chatroom> c = chatRepository.findByName(name);
		if(c != null && !c.isEmpty() && c.get(0).getAdmin().equals(username)) return true;
		return false;
	}

	public boolean isMultiple(String name){
		List<Chatroom> c = chatRepository.findByName(name);
		if(c != null && !c.isEmpty()) return c.get(0).isMultipleusers();
		return false;
	}
	
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
	
	/* TABLA MENSAJE */
	
	public void insertMsg(String sender, String dst, long timestamp, String msg, String type){
		Mensaje m = new Mensaje(sender, dst, timestamp, msg, type);
		msgRepository.save(m);
	}

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
	
	public void removeMsgRoom(String id_room){
		List<Mensaje> mensajes = msgRepository.findMsg(typeMessage.CHAT.toString(), id_room); 
		for(int i = 0; i < mensajes.size(); i++){
			Mensaje mensaje = mensajes.get(i);
			msgRepository.delete(mensaje);
		}
	}
	
	public long getDateJoin(String username, String id_room){
		List<Long> timestamps = msgRepository.findMsgDateBySender(typeMessage.CHAT.toString(), username, "se ha unido a la sala", id_room ); 
		if(!timestamps.isEmpty()) return timestamps.get(0);
		else return 0;
	}
	
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
