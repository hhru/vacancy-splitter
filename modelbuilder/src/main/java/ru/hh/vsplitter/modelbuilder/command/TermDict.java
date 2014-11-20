package ru.hh.vsplitter.modelbuilder.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import ru.hh.vsplitter.DefaultField;
import ru.hh.vsplitter.Document;
import ru.hh.vsplitter.Field;
import ru.hh.vsplitter.Schema;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.DocumentVectorizer;
import ru.hh.vsplitter.vectorize.TfIdfVectorizer;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
  private String input;

  public TermDict(String[] args) throws CmdLineException {
    super(args);
  }

  public void run() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

    Set<String> stopWords = ImmutableSet.copyOf(FileLinesIterable.fromResource(getClass().getClassLoader(), "stopwords"));

    TfIdfVectorizer.Builder titleVectorizerBuilder = TfIdfVectorizer.newBuilder(
        threshold != null ? threshold : DEFAULT_THRESHOLD, stopWords, stem);
    TfIdfVectorizer.Builder textVectorizerBuilder = TfIdfVectorizer.newBuilder(
        threshold != null ? threshold : DEFAULT_THRESHOLD, stopWords, stem);

    for (String line : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> document = mapper.readValue(line, mapType);
      titleVectorizerBuilder.feed(document.get("title"));
      textVectorizerBuilder.feed(document.get("text"));
    }

    Field<String> titleField = DefaultField.create("title", String.class, titleVectorizerBuilder.build());
    Field<String> textField = DefaultField.create("text", String.class, textVectorizerBuilder.build());

    Schema schema = new Schema(Arrays.<Field<?>>asList(titleField, textField));
    Vectorizer<Document> vectorizer = new DocumentVectorizer(schema);

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

        for (Field<?> field : schema.fields()) {
          TfIdfVectorizer fieldVectorizer = (TfIdfVectorizer) field.vectorizer();
          printStream.println(field.name() + " vectorizer:");
          for (TfIdfVectorizer.TermInfo termInfo : fieldVectorizer.terms()) {
            printStream.println(termInfo);
          }
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
