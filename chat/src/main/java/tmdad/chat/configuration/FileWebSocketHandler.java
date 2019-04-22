package tmdad.chat.configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import tmdad.chat.bbdd.DBAdministrator;
import tmdad.chat.controller.ChatRoomController;
import tmdad.chat.controller.UserController;


@Component
public class FileWebSocketHandler extends BinaryWebSocketHandler {

	ChatRoomController chatController = new ChatRoomController();
	DBAdministrator dbAdministrator = new DBAdministrator();
	
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        System.err.println("hello");
        String sender = UserController.getUsername(session);
        String id = dbAdministrator.getActiveRoom(sender);
        chatController.sendFileRoom(id, message, sender, dbAdministrator);
    }
    
}