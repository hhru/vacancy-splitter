package ru.hh.vsplitter.stem;

import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stemmer {

  private static final int DEFAULT_PRE_STEM_LIMIT = 5;
  private static final int DEFAULT_POST_STEM_LIMIT = 4;

  public enum Language {
    RUSSIAN(RussianProgram.class), ENGLISH(EnglishProgram.class);

    final Class<? extends SnowballProgram> programClass;
    Language(Class<? extends SnowballProgram> programClass) {
      this.programClass = programClass;
    }

    SnowballProgram createProgramInstance() {
      try {
        return programClass.newInstance();
      } catch (IllegalAccessException | InstantiationException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  private final List<SnowballProgram> programs;
  private final int preStemLimit;
  private final int postStemLimit;

  public static Stemmer getDefault() {
    return new Stemmer(Language.RUSSIAN, Language.ENGLISH);
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

    for (SnowballProgram program : programs) {
      StringBuilder buffer = new StringBuilder(word);
      if (program.stem(buffer) && buffer.length() >= postStemLimit) {
        return buffer.toString();
      }
    }

    return word;
  }

}
