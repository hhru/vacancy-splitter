package ru.hh.vsplitter.stem;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.RussianStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stemmer {

  private static final int DEFAULT_PRE_STEM_LIMIT = 5;
  private static final int DEFAULT_POST_STEM_LIMIT = 4;

  public enum Language {
    RUSSIAN(new RussianStemmer()), ENGLISH(new EnglishStemmer());

    final SnowballProgram program;

    Language(SnowballProgram program) {
      this.program = program;
    }

    SnowballProgram createProgramInstance() {
      return program;
    }
  }

  private final List<SnowballProgram> programs;
  private final int preStemLimit;
  private final int postStemLimit;

  public static Stemmer getDefault() {
    return new Stemmer(Language.RUSSIAN, Language.ENGLISH);
  }

  public static Stemmer getDummy() {
    return new Stemmer();
  }

  public Stemmer(Language... languages) {
    this(Arrays.asList(languages), DEFAULT_PRE_STEM_LIMIT, DEFAULT_POST_STEM_LIMIT);
  }

  public Stemmer(List<Language> languages, int preStemLimit, int postStemLimit) {
    this.programs = new ArrayList<>();
    for (Language language : languages) {
      programs.add(language.createProgramInstance());
    }
    this.preStemLimit = preStemLimit;
    this.postStemLimit = postStemLimit;
  }

  public String stem(String word) {
    word = word.trim().toLowerCase();
    if (word.length() < preStemLimit) {
      return word;
    }

    var current = word;
    for (SnowballProgram program : programs) {
      program.setCurrent(current);
      var isStemmed = program.stem() && program.getCurrentBufferLength() < current.length();
      if (isStemmed) {
        current = program.getCurrent();
      }
      if (isStemmed && current.length() >= postStemLimit) {
        return current;
      }
    }

    return word;
  }

}
