package tmdad.chat.model;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

public class User {
	
	@Getter @Setter private String username;
	
	@Getter @Setter private WebSocketSession session;
	
	public User(String username, WebSocketSession session){
		this.username = username;
		this.session = session;
	}

}
