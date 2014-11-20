package ru.hh.vsplitter.classify;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

public interface Classifier<T> extends Serializable {

  Collection<String> getClasses();

  String classify(T input) throws ClassifierException;

  void save(OutputStream outputStream) throws IOException;

}
