package org.attnetwork.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.attnetwork.proto.sl.SeqLanOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SeqLanOperation.getByCode(0);
    SpringApplication.run(Application.class, args);
  }

  private void a() {
    ObjectMapper objectMapper = new ObjectMapper();

  }
}
