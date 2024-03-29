package ru.hh.vsplitter.ioutil;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class FileLinesIterable implements Iterable<String> {

  private final CharSource readerSupplier;

  private FileLinesIterable(CharSource readerSupplier) {
    this.readerSupplier = readerSupplier;
  }

  public static FileLinesIterable fromPath(final Path path) {
    return new FileLinesIterable(new CharSource() {
      @Override
      public Reader openStream() throws IOException {
        return Files.newBufferedReader(path, Charsets.UTF_8);
      }
    });
  }

  public static FileLinesIterable fromResource(final ClassLoader loader, final String resourcePath) {
    return new FileLinesIterable(new CharSource() {
      @Override
      public Reader openStream() throws IOException {
        return new BufferedReader(new InputStreamReader(loader.getResourceAsStream(resourcePath)));
      }
    });
  }

  @Override
  public Iterator<String> iterator() {
    return new FileLinesIterator(readerSupplier);
  }
}
