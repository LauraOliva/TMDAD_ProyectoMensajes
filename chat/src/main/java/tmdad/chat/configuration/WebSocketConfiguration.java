package tmdad.chat.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer{

	@Autowired 
    MsgWebSocketHandler mwsHandler;
	
	@Autowired 
	FileWebSocketHandler fwsHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
	    registry.addHandler(mwsHandler, "/msg").setAllowedOrigins("*");
	    registry.addHandler(fwsHandler, "/file").setAllowedOrigins("*");
	}
	
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
	    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
	    // Tamaño maximo 1 MB
	    container.setMaxBinaryMessageBufferSize(1024000);
	    return container;
	}
}
