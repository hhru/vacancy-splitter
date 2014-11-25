package ru.hh.vsplitter.vectorize;

public interface Vectorizer {

  DocVector vectorize(String doc);

  int dimensionCount();
}
