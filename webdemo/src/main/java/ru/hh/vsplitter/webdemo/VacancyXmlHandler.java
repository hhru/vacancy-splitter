package ru.hh.vsplitter.webdemo;

import com.google.common.collect.ImmutableSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

public class VacancyXmlHandler extends DefaultHandler {
  private static final String OPEN_MARKER = "##OPEN_MARKER";
  private static final String CLOSE_MARKER = "##CLOSE_MARKER";

  private final Language language;

  private StringBuilder output = new StringBuilder();

  private StringBuilder holded = new StringBuilder();
  private StringBuilder textToClassify = new StringBuilder();

  private Set<BlockType> encounteredTypes = new HashSet<>();

  boolean fetch;
  final boolean ulMode;
  boolean text = false;

  private BlocksClassifier classifier;

  private static final ImmutableSet<String> markupTags = ImmutableSet.of(
      "b", "strong", "em", "i", "u"
  );

  public VacancyXmlHandler(BlocksClassifier classifier, Language language, boolean ulMode) {
    this.classifier = classifier;
    this.language = language;
    this.ulMode = ulMode;
    if (ulMode) {
      fetch = false;
    } else {
      fetch = true;
    }
  }

  private String makeStart(String tag) {
    return "<" + tag + '>';
  }

  private String makeEnd(String tag) {
    return "</" + tag + '>';
  }

  private void openMarker() {
    text = true;
    holded.append(OPEN_MARKER);
  }

  private void closeMarker() {
    text = false;
    holded.append(CLOSE_MARKER);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (markupTags.contains(qName)) {
      return;
    }

    if (fetch && text) {
      closeMarker();
    }

    if (ulMode) {
      if (qName.equals("ul")) {
        fetch = true;
      }
    } else {
      classify();
    }

    if (fetch) {
      holded.append(makeStart(qName));
    } else {
      output.append(makeStart(qName));
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (markupTags.contains(qName)) {
      return;
    }

    if (fetch && text) {
      closeMarker();
    }

    if (fetch) {
      holded.append(makeEnd(qName));
    } else {
      output.append(makeEnd(qName));
    }

    if (ulMode) {
      if (qName.equals("ul")) {
        classify();
        fetch = false;
      }
    } else {
      classify();
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (fetch) {
      textToClassify.append(ch, start, length);
      if (!text) {
        openMarker();
      }
      holded.append(ch, start, length);
    } else {
      output.append(ch, start, length);
    }
  }

  @Override
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
    super.unparsedEntityDecl(name, publicId, systemId, notationName);
  }

  private void classify() {
    if (textToClassify.length() > 0) {
      String text = textToClassify.toString();
      textToClassify = new StringBuilder();
      BlockType type = classifier.classify(text, language);

      String open = "";
      String close = "";

      if (type != null && type != BlockType.NOTHING) {
        encounteredTypes.add(type);
        open = "<span class=\"" + type.getInternalName() + "\">";
        close = "</span>";
      }

      String chunk = holded.toString();
      holded = new StringBuilder();

      chunk = chunk.replaceAll(OPEN_MARKER, open);
      chunk = chunk.replaceAll(CLOSE_MARKER, close);
      output.append(chunk);
    }
  }

  public Set<BlockType> getTypes() {
    return encounteredTypes;
  }

  public String getOutput() {
    classify();
    return output.toString();
  }

}
