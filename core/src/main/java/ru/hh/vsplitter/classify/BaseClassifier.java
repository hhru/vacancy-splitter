package ru.hh.vsplitter.classify;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public abstract class BaseClassifier implements Classifier {

  protected final List<String> classes;
  protected final Vectorizer vectorizer;

  public BaseClassifier(Collection<String> classes, Vectorizer vectorizer) {
    Preconditions.checkNotNull(classes);
    Preconditions.checkNotNull(vectorizer);
    this.classes = ImmutableList.copyOf(classes);
    this.vectorizer = vectorizer;
  }

  protected abstract String classify(DocVector vector) throws ClassifierException;

  public String classify(String text) throws ClassifierException {
    DocVector vector = vectorizer.vectorize(text);
    return !vector.isEmpty() ? classify(vector) : null;
  }

  // it is just java serialization now, but it can be changed later
  @Override
  public void save(OutputStream outputStream) throws IOException {
    try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
      oos.writeObject(this);
    }
  }

  @Override
  public final Collection<String> getClasses() {
    return classes;
  }
}
