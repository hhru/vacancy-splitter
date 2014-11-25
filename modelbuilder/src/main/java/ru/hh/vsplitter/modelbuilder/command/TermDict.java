package ru.hh.vsplitter.modelbuilder.command;

import com.google.common.collect.ImmutableSet;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.TfIdfVectorizer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static com.google.common.collect.Iterables.concat;

public class TermDict extends Command {
  private static final int DEFAULT_THRESHOLD = 5;

  @Option(name="-b", aliases = {"--binary"}, depends = {"-o"}, usage = "output binary data")
  private boolean binary;

  @Option(name="-o", aliases = {"--output"}, metaVar = "OUTPUT file/dir", usage="output file name (required for binary output)")
  private String output;

  @Option(name="--threshold", usage = "document frequency threshold for term dictionary")
  private Integer threshold;

  @Option(name="-s", aliases = {"--stem"}, usage = "stem terms, meaningful only with \"termdict\" command")
  private boolean stem;

  @Argument(required = true, metaVar = "FILES", usage = "input text files")
  private List<String> input;

  public TermDict(String[] args) throws CmdLineException {
    super(args);
  }

  public void run() throws IOException {
    List<Iterable<String>> fileLinesList = new ArrayList<>();
    for (String fileName: input) {
      fileLinesList.add(FileLinesIterable.fromPath(FileSystems.getDefault().getPath(fileName)));
    }

    Set<String> stopWords = ImmutableSet.copyOf(FileLinesIterable.fromResource(getClass().getClassLoader(), "stopwords"));
    TfIdfVectorizer vectorizer = TfIdfVectorizer.fromDocCorpus(
        concat(fileLinesList), threshold != null ? threshold : DEFAULT_THRESHOLD, stopWords, stem);

    OutputStream outputStream = null;
    if (output != null) {
      outputStream = Files.newOutputStream(FileSystems.getDefault().getPath(output));
    }

    try {
      if (!binary) {
        PrintStream printStream;
        if (outputStream != null) {
          printStream = new PrintStream(outputStream);
        } else {
          printStream = System.out;
        }

        for (TfIdfVectorizer.TermInfo termInfo : vectorizer.terms()) {
          printStream.println(termInfo);
        }
      } else {
        StoreUtils.saveVectorizer(vectorizer, outputStream);
      }
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }

}
