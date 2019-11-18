package ru.hh.vsplitter.split;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.html.HtmlEscapers;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public abstract  class HtmlBlocksHandler extends DefaultHandler {
  protected static final Set<String> IGNORED_TAGS = ImmutableSet.of(
      "b", "i", "u", "strong", "em", "strike", "span"
  );

  private static final CharMatcher VALID_STOP = CharMatcher.anyOf(".!?:");
  private static final CharMatcher REPLACE_STOP = CharMatcher.anyOf(",;");
  private static final CharMatcher ILLEGAL_CHARACTERS = CharMatcher.anyOf("•⋅·◦");
  private static final Pattern ENDS_WITH_ENTITY = Pattern.compile(".*&(#([0-9]+|x[0-9A-Fa-f]+)|[A-Za-z_][A-Za-z_0-9]*);$");

  public abstract  List<String> getTextBlocks();

  protected static String makeSentence(String text) {
    text = CharMatcher.whitespace().collapseFrom(text.trim(), ' ');

    char lastChar = text.charAt(text.length() - 1);
    if (REPLACE_STOP.matches(lastChar) && !ENDS_WITH_ENTITY.matcher(text).matches()) {
      text = text.substring(0, text.length() - 1) + '.';
    } else if (!VALID_STOP.matches(lastChar)) {
      text += ".";
    }
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  protected static String trimAndCollapse(String text) {
    return CharMatcher.whitespace().trimAndCollapseFrom(text, ' ');
  }

  protected abstract void text(String text);

  @Override
  public final void characters(char[] charArray, int start, int length) throws SAXException {
    text(HtmlEscapers.htmlEscaper().escape(ILLEGAL_CHARACTERS.replaceFrom(String.valueOf(charArray, start, length), ' ')));
  }
}
