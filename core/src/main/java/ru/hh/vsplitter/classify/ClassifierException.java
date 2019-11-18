package ru.hh.vsplitter.classify;

public class ClassifierException extends Exception {
  private static final long serialVersionUID = -1089378829939395108L;

  public ClassifierException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassifierException(String message) {
    super(message);
  }

}
