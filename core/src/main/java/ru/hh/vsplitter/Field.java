package ru.hh.vsplitter;

import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;

public abstract class Field<T> {

  public abstract String name();
  public abstract Vectorizer<T> vectorizer();
  public abstract DocVector vectorize(Document document);

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  protected final Class<T> type;

  protected Field(Class<T> type) {
    this.type = type;
  }

  public final Class<T> type() {
    return type;
  }

}
