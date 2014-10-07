package ru.hh.vsplitter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.vsplitter.stem.Stemmer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Vectorizer implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(Vectorizer.class);
  private static final Splitter TOKENIZER =
      Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings().trimResults();

  private transient final Stemmer stemmer = Stemmer.getDefault(); // not thread safe, so not static

  private final TermInfo[] terms;
  private final Map<String, Integer> termIndex;
  private final int totalDocs;

  public static Vectorizer fromDocCorpus(Iterable<String> docs, int threshold) {
    Stemmer stemmer = Stemmer.getDefault();

    Map<String, Integer> termCounts = new HashMap<>();
    int totalDocs = 0;

    for (String doc : docs) {
      List<String> tokens = TOKENIZER.splitToList(doc);

      if (tokens.size() > 0) {
        totalDocs++;

        for (String token : tokens) {
          String stemmed = stemmer.stem(token);
          if (termCounts.containsKey(stemmed)) {
            termCounts.put(stemmed, termCounts.get(stemmed) + 1);
          } else {
            termCounts.put(stemmed, 1);
          }
        }
      }
    }

    log.debug("full term count: {}", termCounts.size());

    if (threshold > 0) {
      Iterator<Map.Entry<String, Integer>> entryIterator = termCounts.entrySet().iterator();
      while (entryIterator.hasNext()) {
        Map.Entry<String, Integer> entry = entryIterator.next();
        if (entry.getValue() < threshold) {
          entryIterator.remove();
        }
      }
      log.debug("cleaned term count: {}", termCounts.size());
    }

    return new Vectorizer(termCounts, totalDocs);
  }

  public Vectorizer(Map<String, Integer> termCounts, int totalDocs) {
    this.totalDocs = totalDocs;
    List<String> sorted = Ordering.natural().sortedCopy(termCounts.keySet());

    terms = new TermInfo[sorted.size()];
    termIndex = new HashMap<>();

    for (int termId = 0; termId < sorted.size(); termId++) {
      String termText = sorted.get(termId).intern();
      TermInfo term = new TermInfo(termText, termId, termCounts.get(termText));
      terms[termId] = term;
      termIndex.put(termText, termId);
    }
  }

  protected double tfIdf(int termId, int termCount, int docLength) {
    return Math.sqrt((double) termCount / docLength) * Math.log((double) totalDocs / terms[termId].docFreq);
  }

  public DocVector vectorize(String doc) {
    final List<String> tokens = TOKENIZER.splitToList(doc);

    SortedMap<Integer, Integer> termCounts = new TreeMap<>();
    for (String token : tokens) {
      String stemmed = stemmer.stem(token);
      if (termIndex.containsKey(stemmed)) {
        Integer termId = termIndex.get(stemmed);
        if (termCounts.containsKey(termId)) {
          termCounts.put(termId, termCounts.get(termId) + 1);
        } else {
          termCounts.put(termId, 1);
        }
      }
    }

    return DocVector.fromId2Value(Maps.transformEntries(termCounts, new Maps.EntryTransformer<Integer, Integer, Double>() {
      @Override
      public Double transformEntry(Integer termId, Integer count) {
        return tfIdf(termId, count, tokens.size());
      }
    }));
  }

  public static final class TermInfo {
    public final String term;
    public final int id;
    public final int docFreq;

    public TermInfo(String term, int id, int docFreq) {
      Preconditions.checkNotNull(term);

      this.term = term;
      this.id = id;
      this.docFreq = docFreq;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TermInfo termInfo = (TermInfo) o;

      return term.equals(termInfo.term) && id == termInfo.id && docFreq == termInfo.docFreq;
    }

    @Override
    public int hashCode() {
      int result = term != null ? term.hashCode() : 0;
      result = 31 * result + id;
      result = 31 * result + docFreq;
      return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Vectorizer that = (Vectorizer) o;

    return totalDocs == that.totalDocs && Arrays.equals(terms, that.terms);
  }

  @Override
  public int hashCode() {
    return 31 * Arrays.hashCode(terms) + totalDocs;
  }
}
