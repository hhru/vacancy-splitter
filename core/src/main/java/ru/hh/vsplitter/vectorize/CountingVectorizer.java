package ru.hh.vsplitter.vectorize;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.vsplitter.stem.Stemmer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

public class CountingVectorizer implements Vectorizer<String> {
  private static final Logger log = LoggerFactory.getLogger(CountingVectorizer.class);
  private static final Splitter TOKENIZER =
      Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings().trimResults();

  private transient Stemmer stemmer; // not thread safe, so not static

  private final boolean stem;
  private final String[] terms;
  private final Map<String, Integer> termIndex;
  private final int totalDocs;

  private static final Function<String, String> LOWERCASE = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return input.toLowerCase();
    }
  };

  public CountingVectorizer(Set<String> terms, int totalDocs, boolean stem) {
    this.totalDocs = totalDocs;
    this.stem = stem;
    List<String> sorted = Ordering.natural().sortedCopy(terms);

    this.terms = new String[sorted.size()];
    termIndex = new HashMap<>();

    for (int termId = 0; termId < sorted.size(); termId++) {
      String termText = sorted.get(termId);
      this.terms[termId] = termText;
      termIndex.put(termText, termId);
    }

    initStemmer();
  }

  private void initStemmer() {
    stemmer = stem ? Stemmer.getDefault() : Stemmer.getDummy();
  }

  public static class Builder {
    private final Set<String> stopWords;
    private final boolean stem;
    private final Stemmer stemmer;

    int totalDocs = 0;
    Set<String> terms = new HashSet<>();

    Builder(Set<String> stopWords, boolean stem) {
      this.stopWords = stopWords;
      this.stem = stem;

      stemmer = stem ? Stemmer.getDefault() : Stemmer.getDummy();
    }

    public Builder feed(String doc) {
      List<String> tokens = ImmutableList.copyOf(
          Iterables.transform(filter(TOKENIZER.split(doc), not(Predicates.in(stopWords))), LOWERCASE));

      if (tokens.size() > 0) {
        totalDocs++;

        for (String token : tokens) {
          String stemmed = stemmer.stem(token);
          terms.add(stemmed);
        }
      }
      return this;
    }

    public CountingVectorizer build() {
      log.debug("term count: {}", terms.size());

      return new CountingVectorizer(terms, totalDocs, stem);
    }
  }

  public static Builder newBuilder(int threshold, Set<String> stopWords, boolean stem) {
    return new Builder(stopWords, stem);
  }


  @Override
  public DocVector vectorize(String input) {
    return null;
  }

  @Override
  public int getDimensionCount() {
    return 0;
  }
}
