package ru.pusher.exceptions;

public class PushException extends Exception {
  public PushException() {
  }

  public PushException(String message) {
    super(message);
  }

  public PushException(String message, Throwable cause) {
    super(message, cause);
  }

  public PushException(Throwable cause) {
    super(cause);
  }
}
