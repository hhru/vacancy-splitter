package ru.hh.vsplitter.vectorize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;
import ru.hh.vsplitter.stem.Stemmer;
import java.util.List;
import java.util.Map;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static org.testng.Assert.assertEquals;
import static ru.hh.vsplitter.vectorize.DocVector.fromDense;

public class VectorizerTest {

  @Test
  public void testVectorizer() {
    Stemmer stemmer = Stemmer.getDefault();
    Map<String, Integer> termCounts = ImmutableMap.of(
        stemmer.stem("java"), 5,
        stemmer.stem("android"), 3,
        stemmer.stem("python"), 6,
        stemmer.stem("developer"), 12,
        stemmer.stem("senior"), 4
    );

    TfIdfVectorizer vectorizer = new TfIdfVectorizer(termCounts, 13, true);

    assertEquals(vectorizer.vectorize("java software developer"),
        fromDense(
            0.0, sqrt(1.0 / 3.0) * log(13.0 / 12.0), sqrt(1.0 / 3.0) * log(13.0 / 5.0), 0.0, 0.0));
    assertEquals(vectorizer.vectorize("senior python developer"),
        fromDense(
            0.0, sqrt(1.0 / 3.0) * log(13.0 / 12.0), 0.0,
            sqrt(1.0 / 3.0) * log(13.0 / 6.0), sqrt(1.0 / 3.0) * log(13.0 / 4.0)));
    assertEquals(vectorizer.vectorize("senior senior android/java developer"),
        fromDense(
            sqrt(1.0 / 5.0) * log(13.0 / 3.0), sqrt(1.0 / 5.0) * log(13.0 / 12.0),
            sqrt(1.0 / 5.0) * log(13.0 / 5.0), 0.0, sqrt(2.0 / 5.0) * log(13.0 / 4.0)));
    assertEquals(vectorizer.vectorize("c++ as language for os"), fromDense(0.0, 0.0, 0.0, 0.0, 0.0));
  }

  @Test
  public void testCorpus() {
    Stemmer stemmer = Stemmer.getDefault();
    List<String> corpus = ImmutableList.of(
        "senior c++ developer",
        "java development",
        "developer of android os",
        "python numpy developer",
        "java/python",
        "senior python developer",
        "development of java language",
        "development python interpreter of android applications",
        "developer",
        "senior java developer (android)",
        "developers concerned in python",
        "python/java developer",
        "senior developer"
    );

    assertEquals(TfIdfVectorizer.fromDocCorpus(corpus, 3, ImmutableSet.of("of"), true), new TfIdfVectorizer(ImmutableMap.of(
        stemmer.stem("java"), 5,
        stemmer.stem("android"), 3,
        stemmer.stem("python"), 6,
        stemmer.stem("developer"), 12,
        stemmer.stem("senior"), 4
    ), 13, true));
  }

}
