package ru.hh.vsplitter.vectorize;

import java.io.Serializable;

public interface Vectorizer<T> extends Serializable {

  DocVector vectorize(T input);

  public int getDimensionCount();

}
