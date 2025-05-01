package org.ici.exceptions;

public class ClientNotHandledException extends RuntimeException {
  public ClientNotHandledException(String message) {
    super(message);
  }
}
