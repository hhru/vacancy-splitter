package ru.hh.vsplitter.split;

import com.google.common.collect.ImmutableList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

class UlHandler extends HtmlBlocksHandler {
  private static final String UL_TAG = "ul";

  private boolean fetch;
  private int nestingLevel;

  private StringBuilder blockText;
  private StringBuilder tagText;

  // output
  private List<String> textBlocks = new ArrayList<>();

  @Override
  public void startDocument() throws SAXException {
    tagText = new StringBuilder();
    blockText = new StringBuilder();

    fetch = false;
    nestingLevel = 0;

    textBlocks.clear();
  }

  private void finishTagText() {
    String text = trimAndCollapse(tagText.toString());

    if (!text.isEmpty()) {
      text = makeSentence(text);

      if (blockText.length() > 0) {
        blockText.append(' ');
      }
      blockText.append(text);
    }

    tagText.setLength(0);
  }

  private void finishBlockText() {
    String text = blockText.toString();
    if (!text.isEmpty()) {
      textBlocks.add(text);
    }
    blockText.setLength(0);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals(UL_TAG)) {
      ++nestingLevel;
      fetch = true;
    }

    if (!IGNORED_TAGS.contains(qName) && fetch) {
      finishTagText();
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (!IGNORED_TAGS.contains(qName) && fetch) {
      finishTagText();
    }

    if (fetch && qName.equals(UL_TAG)) {
      --nestingLevel;
      if (nestingLevel == 0) {
        fetch = false;
        finishBlockText();
      }
    }
  }

  @Override
  public void endDocument() throws SAXException {
    finishTagText();
    finishBlockText();
  }

  @Override
  public void text(String text) {
    if (fetch) {
      tagText.append(text);
    }
  }

  @Override
  public List<String> getTextBlocks() {
    return ImmutableList.copyOf(textBlocks);
  }
}
