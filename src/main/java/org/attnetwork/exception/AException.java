package org.attnetwork.exception;

public class AException extends RuntimeException {
  private String code;
  private String[] args;

  public AException() {
    super();
  }

  public AException(String message) {
    super(message);
  }

  public AException(Throwable e) {
    super(e);
  }
}
