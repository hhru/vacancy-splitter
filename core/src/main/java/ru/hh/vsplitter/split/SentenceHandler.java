package ru.hh.vsplitter.split;

import com.google.common.collect.ImmutableList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

class SentenceHandler extends HtmlBlocksHandler {

  private StringBuilder tagText;

  // output
  private List<String> textBlocks = new ArrayList<>();

  @Override
  public void startDocument() throws SAXException {
    tagText = new StringBuilder();
    textBlocks.clear();
  }

  private void finishTagText() {
    String text = trimAndCollapse(tagText.toString());

    if (!text.isEmpty()) {
      text = makeSentence(text);
      textBlocks.add(text);
    }

    tagText.setLength(0);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (!IGNORED_TAGS.contains(qName)) {
      finishTagText();
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (!IGNORED_TAGS.contains(qName)) {
      finishTagText();
    }
  }

  @Override
  public void endDocument() throws SAXException {
    finishTagText();
  }

  @Override
  public void text(String text)  {
    tagText.append(text);
  }

  @Override
  public List<String> getTextBlocks() {
    return ImmutableList.copyOf(textBlocks);
  }

}
