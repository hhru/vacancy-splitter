package ru.hh.vsplitter.stem;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static ru.hh.vsplitter.stem.Stemmer.Language.ENGLISH;
import static ru.hh.vsplitter.stem.Stemmer.Language.RUSSIAN;

public class StemmerTest {

  @Test
  public void stemmingTest() {
    Stemmer stemmer = new Stemmer(RUSSIAN, ENGLISH);

    assertEquals(stemmer.stem("java"), "java");
    assertEquals(stemmer.stem("developer"), "develop");
    assertEquals(stemmer.stem("программисты"), "программист");
    assertEquals(stemmer.stem("знание"), "знан");
    assertEquals(stemmer.stem("разработка"), "разработк");
    assertEquals(stemmer.stem("cats"), "cats");
  }

}
