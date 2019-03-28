package tmdad.chat.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;
import tmdad.chat.model.ChatRoom;

public class ChatRoomController {
	

	@Setter @Getter static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
	
	@Getter @Setter private int nChatRooms;
	
	public ChatRoomController() {
		nChatRooms = 0;
	}
	
	public void sendMessageRoom(String id, TextMessage msg, String sender, String type){
		ChatRoom room = chatRooms.get(id);
		room.sendMessageRoom(msg, sender, type);
	}
	
	public boolean existsChatRoom(String id){
		return chatRooms.containsKey(id);
	}
	
	public ChatRoom getChatRoom(String id){
		return chatRooms.get(id);
	}
	
	public void addChatRoom(ChatRoom c){
		chatRooms.put(c.getId(), c);
		
		
		
		nChatRooms++;
	}
	
	public void removeChatRoom(String id){
		chatRooms.remove(id);
		nChatRooms--;
	}
	

}
