package ru.hh.vsplitter.split;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class SentenceHandler extends HtmlBlocksHandler {

  private static final Set<String> IGNORED_TAGS = ImmutableSet.of(
      "b", "i", "u", "strong", "em", "strike", "span"
  );

  StringBuilder tagText;

  // output
  private List<String> textBlocks = new ArrayList<>();

  @Override
  public void startDocument() throws SAXException {
    tagText = new StringBuilder();
    textBlocks.clear();
  }

  private void finishTagText() {
    String text = tagText.toString().trim();

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
  public void characters(char[] charArray, int start, int length) throws SAXException {
    tagText.append(charArray, start, length);
  }

  @Override
  public List<String> getTextBlocks() {
    return ImmutableList.copyOf(textBlocks);
  }

}
