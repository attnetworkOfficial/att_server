package org.attnetwork.server;

import org.attnetwork.server.component.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
public class HttpServer {
  private final MessageService messageService;

  @Autowired
  public HttpServer(MessageService messageService) {
    this.messageService = messageService;
  }

  @RequestMapping("**")
  public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
    messageService.process(request.getInputStream(), response.getOutputStream());
    response.getOutputStream().flush();
  }
}
