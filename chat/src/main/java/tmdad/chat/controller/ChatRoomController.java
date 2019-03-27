package tmdad.chat.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	
	public ChatRoom getChatRoom(String id){
		return chatRooms.get(id);
	}
	
	public void addChatRoom(ChatRoom c){
		chatRooms.put(c.getId(), c);
	}
	
	public void removeChatRoom(String id){
		chatRooms.remove(id);
	}
	

}
