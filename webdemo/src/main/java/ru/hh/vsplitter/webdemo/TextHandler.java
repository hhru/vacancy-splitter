package ru.hh.vsplitter.webdemo;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TextHandler extends DefaultHandler {

  private StringBuilder output = new StringBuilder();

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    output.append(ch, start, length);
  }

  public String getText() {
    return output.toString();
  }
}
