package ru.hh.vsplitter.modelbuilder.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class VectorizeDoc extends Command {

  @Option(name="-b", aliases = {"--binary"}, depends = {"-o"}, usage = "output binary data")
  private boolean binary;

  @Option(name="-o", aliases = {"--output"}, metaVar = "OUTPUT file/dir", usage="output file name (required for binary output)")
  private String output;

  @Option(name="-d", aliases = {"--term-dict"}, metaVar = "file", usage = "term dict location", required = true)
  private String termDictLocation;

  @Argument(required = true, metaVar = "FILES", usage = "input text files")
  private List<String> input;

  public VectorizeDoc(String[] args) throws CmdLineException {
    super(args);
  }

  @Override
  public void run() throws IOException {
    final Vectorizer vectorizer = StoreUtils.loadVectorizer(
        Files.newInputStream(FileSystems.getDefault().getPath(termDictLocation)));

    List<Iterable<String>> fileLinesList = new ArrayList<>();
    for (String fileName: input) {
      fileLinesList.add(FileLinesIterable.fromPath(FileSystems.getDefault().getPath(fileName)));
    }

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

        for (String line : concat(fileLinesList)) {
          DocVector vector = vectorizer.vectorize(line);
          if (!vector.isEmpty()) {
            printStream.println(vectorizer.vectorize(line));
          }
        }
      } else {
        List<DocVector> matrix = ImmutableList.copyOf(
            filter(
                transform(concat(fileLinesList), new Function<String, DocVector>() {
                  @Override
                  public DocVector apply(String line) {
                    return vectorizer.vectorize(line);
                  }
                }), new Predicate<DocVector>() {
                  @Override
                  public boolean apply(DocVector vector) {
                    return !vector.isEmpty();
                  }
                }
            )
        );
        StoreUtils.saveMatrix(matrix, outputStream);
      }
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }
}
