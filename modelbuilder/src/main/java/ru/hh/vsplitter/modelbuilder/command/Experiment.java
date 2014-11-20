package ru.hh.vsplitter.modelbuilder.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import ru.hh.vsplitter.DefaultField;
import ru.hh.vsplitter.Field;
import ru.hh.vsplitter.Schema;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.vectorize.CountingVectorizer;
import ru.hh.vsplitter.vectorize.DocumentVectorizer;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Experiment extends Command {

  @Argument(required = true, metaVar = "FILE", usage = "input text file")
  private String input;

  public Experiment(String[] args) throws CmdLineException {
    super(args);
  }

  @Override
  public void run() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

    Set<String> stopWords = ImmutableSet.copyOf(FileLinesIterable.fromResource(getClass().getClassLoader(), "stopwords"));
    CountingVectorizer.Builder titleVectorizerBuilder = CountingVectorizer.newBuilder(5, stopWords, true);
    CountingVectorizer.Builder textVectorizerBuilder = CountingVectorizer.newBuilder(5, stopWords, true);

    for (String line : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> document = mapper.readValue(line, mapType);
      titleVectorizerBuilder.feed(document.get("title"));
      textVectorizerBuilder.feed(document.get("text"));
    }

    Field<String> titleField = DefaultField.create("title", String.class, titleVectorizerBuilder.build());
    Field<String> textField = DefaultField.create("text", String.class, textVectorizerBuilder.build());

    Schema schema = new Schema(Arrays.<Field<?>>asList(titleField, textField));
    DocumentVectorizer vectorizer = new DocumentVectorizer(schema);

    int a = 2;
  }
}
