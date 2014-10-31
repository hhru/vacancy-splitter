package ru.hh.vsplitter.webdemo;

import com.google.common.base.CharMatcher;

public enum Language {
  ENGLISH(CharMatcher.anyOf("abcdefghijklmnopqrstuvwxyz")),
  RUSSIAN(CharMatcher.anyOf("абвгдеёжзийклмнопрстуфхцчшщъыьёюя"));

  final CharMatcher matcher;
  Language(CharMatcher charMatcher) {
    matcher = charMatcher;
  }

  public static Language fromText(String text) {
    int rusCount = RUSSIAN.matcher.countIn(text);
    int engCount = ENGLISH.matcher.countIn(text);
    return (rusCount > engCount) ? RUSSIAN : ENGLISH;
  }
}
