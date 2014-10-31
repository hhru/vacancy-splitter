package ru.hh.vsplitter.webdemo;

public enum BlockType {
  REQUIREMENTS("req"), RESPONSIBILITIES("res"), CONDITIONS("con"), NOTHING("not");

  private final String internalName;

  BlockType(String internalName) {
    this.internalName = internalName;
  }

  public String getInternalName() {
    return internalName;
  }

}
