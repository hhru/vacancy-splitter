package ru.hh.vsplitter.vectorize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;
import ru.hh.vsplitter.stem.Stemmer;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static ru.hh.vsplitter.vectorize.DocVector.fromDense;

public class CountingVectorizerTest {

  @Test
  public void testVectorizer() {
    Stemmer stemmer = Stemmer.getDefault();
    Set<String> termCounts = ImmutableSet.of(
        stemmer.stem("java"),
        stemmer.stem("android"),
        stemmer.stem("python"),
        stemmer.stem("developer"),
        stemmer.stem("senior")
    );

    CountingVectorizer vectorizer = new CountingVectorizer(termCounts, true);

    assertEquals(vectorizer.vectorize("java software developer"), fromDense(0.0, 1.0, 1.0, 0.0, 0.0));
    assertEquals(vectorizer.vectorize("senior python developer"), fromDense(0.0, 1.0, 0.0, 1.0, 1.0));
    assertEquals(vectorizer.vectorize("senior senior android/java developer"), fromDense(1.0, 1.0, 1.0, 0.0, 2.0));
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

    assertEquals(CountingVectorizer.fromDocCorpus(corpus, 3, ImmutableSet.of("of"), true),
        new CountingVectorizer(ImmutableSet.of(
            stemmer.stem("java"),
            stemmer.stem("android"),
            stemmer.stem("python"),
            stemmer.stem("developer"),
            stemmer.stem("senior")), true));
  }

}
