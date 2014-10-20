package ru.hh.vsplitter.classify;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

public interface Classifier extends Serializable {

  Collection<String> getClasses();

  String classify(String text) throws ClassifierException;

  void save(OutputStream outputStream) throws IOException;

}
