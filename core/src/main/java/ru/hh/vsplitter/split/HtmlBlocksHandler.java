package ru.hh.vsplitter.split;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.html.HtmlEscapers;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.List;
import java.util.Set;

public abstract  class HtmlBlocksHandler extends DefaultHandler {
  protected static final Set<String> IGNORED_TAGS = ImmutableSet.of(
      "b", "i", "u", "strong", "em", "strike", "span"
  );

  private static final CharMatcher VALID_STOP = CharMatcher.anyOf(".!?:");
  private static final CharMatcher REPLACE_STOP = CharMatcher.anyOf(",;");
  private static final CharMatcher ILLEGAL_CHARACTERS = CharMatcher.anyOf("•⋅·◦");

  public abstract  List<String> getTextBlocks();

  protected static String makeSentence(String text) {
    text = CharMatcher.WHITESPACE.collapseFrom(text.trim(), ' ');

    char lastChar = text.charAt(text.length() - 1);
    if (REPLACE_STOP.matches(lastChar)) {
      text = text.substring(0, text.length() - 1) + '.';
    } else if (!VALID_STOP.matches(lastChar)) {
      text = text +  ".";
    }
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  protected static String trimAndCollapse(String text) {
    return CharMatcher.WHITESPACE.trimAndCollapseFrom(text, ' ');
  }

  protected abstract void text(String text);

  @Override
  public final void characters(char[] charArray, int start, int length) throws SAXException {
    text(HtmlEscapers.htmlEscaper().escape(ILLEGAL_CHARACTERS.replaceFrom(String.valueOf(charArray, start, length), ' ')));
  }
}
