package ru.hh.vsplitter.ioutil;

import com.google.common.io.CharSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class FileLinesIterator implements Iterator<String> {
  private String line;
  private BufferedReader reader;
  private final CharSource readerSupplier;

  public FileLinesIterator(CharSource readerSupplier) {
    this.readerSupplier = readerSupplier;
  }

  void initReader() {
    if (reader == null) {
      try {
        reader = new BufferedReader(readerSupplier.openStream());
        readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    }
    return current;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
