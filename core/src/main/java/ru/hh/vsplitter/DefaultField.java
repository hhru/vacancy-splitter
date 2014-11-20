package ru.hh.vsplitter;

import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.ObjectStreamException;
import java.io.Serializable;

public final class DefaultField<T> extends Field<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Vectorizer<T> vectorizer;
  private final String name;

  protected DefaultField(String name, Class<T> type, Vectorizer<T> vectorizer) {
    super(type);
    this.name = name;
    this.vectorizer = vectorizer;
  }

  public static <T> Field<T> create(String name, Class<T> type, Vectorizer<T> vectorizer) {
    return new DefaultField<T>(name, type, vectorizer);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Vectorizer<T> vectorizer() {
    return vectorizer;
  }

  @Override
  public DocVector vectorize(Document document) {
    return vectorizer.vectorize(document.getValue(this));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DefaultField that = (DefaultField) o;

    return that.type.equals(type) && that.name.equals(name);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  private static class SerialForm<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public Class<T> type;
    public Vectorizer<T> vectorizer;
    public String name;


    public SerialForm(DefaultField<T> field) {
      type = field.type;
      vectorizer = field.vectorizer;
      name = field.name;
    }

    protected Object readResolve() throws ObjectStreamException {
      return create(name, type, vectorizer);
    }
  }

  protected Object writeReplace() throws ObjectStreamException {
    return new SerialForm<>(this);
  }

}
