package org.attnetwork.server;

import org.attnetwork.server.component.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Configuration
@EnableWebSocket
public class WebSocketServer implements WebSocketConfigurer {
  private final MessageService messageService;

  @Autowired
  public WebSocketServer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    BinaryWebSocketHandler binaryWebSocketHandler = new BinaryWebSocketHandler() {
      @Override
      public void afterConnectionEstablished(WebSocketSession session) {
        // TODO close illegal connection
      }

      @Override
      public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        messageService.processWebSocket(session, message, os);
        session.sendMessage(new BinaryMessage(os.toByteArray()));
      }
    };
    registry.addHandler(binaryWebSocketHandler, "/ws");
  }

}
