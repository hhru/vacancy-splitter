package ru.hh.vsplitter.ioutil;

import com.google.common.base.Throwables;
import com.google.common.io.InputSupplier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

class FileLinesIterator implements Iterator<String> {
  private String line;
  private BufferedReader reader;
  private final InputSupplier<Reader> readerSupplier;

  public FileLinesIterator(InputSupplier<Reader> readerSupplier) {
    this.readerSupplier = readerSupplier;
  }

  void initReader() {
    if (reader == null) {
      try {
        reader = new BufferedReader(readerSupplier.getInput());
        readLine();
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  void readLine() throws IOException {
    line = reader.readLine();
    if (line == null) {
      reader.close();
    }
  }

  @Override
  public boolean hasNext() {
    initReader();
    return line != null;
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    String current = line;
    try {
      readLine();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return current;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
