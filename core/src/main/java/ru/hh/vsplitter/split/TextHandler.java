package ru.hh.vsplitter.split;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class TextHandler extends DefaultHandler {

  private StringBuilder builder;
  private String lastResult;

  @Override
  public void startDocument() throws SAXException {
    builder = new StringBuilder();
  }

  @Override
  public void characters(char[] charArray, int start, int length) throws SAXException {
    builder.append(charArray, start, length);
  }

  @Override
  public void endDocument() throws SAXException {
    lastResult = builder.toString();
  }

  public String getText() {
    return lastResult;
  }
}
