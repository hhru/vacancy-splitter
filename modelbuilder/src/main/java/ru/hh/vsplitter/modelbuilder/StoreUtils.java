package ru.hh.vsplitter.modelbuilder;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import ru.hh.vsplitter.classify.BaseClassifier;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

public class StoreUtils {

  public static void saveVectorizer(Vectorizer vectorizer, OutputStream outputStream) throws IOException {
    try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
      out.writeObject(vectorizer);
    }
  }

  public static Vectorizer loadVectorizer(InputStream inputStream) throws IOException {
    try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
      return (Vectorizer) in.readObject();
    } catch (ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

  public static void saveMatrix(List<DocVector> rows, OutputStream outputStream) throws IOException {
    try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
      out.writeObject(ImmutableList.copyOf(rows));
    }
  }

  @SuppressWarnings("unchecked")
  public static List<DocVector> loadMatrix(InputStream inputStream) throws IOException {
    try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
      return (List<DocVector>) in.readObject();
    } catch (ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

  public static BaseClassifier loadClassifier(InputStream inputStream) throws IOException {
    try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
      return (BaseClassifier) in.readObject();
    } catch (ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

}

