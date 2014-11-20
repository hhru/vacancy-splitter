package ru.hh.vsplitter.modelbuilder.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import ru.hh.vsplitter.Document;
import ru.hh.vsplitter.Schema;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.ClassifierException;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.DocumentVectorizer;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import static ru.hh.vsplitter.modelbuilder.StoreUtils.loadClassifier;

public class Predict extends Command {
  @Option(name="-d", metaVar = "file", usage = "term dict location", required = true)
  private String termDictLocation;

  @Option(name="-m", aliases = {"--model"}, usage = "model file location", required = true)
  private String modelLocation;

  @Argument(required = true, metaVar = "INPUT")
  private String input;

  private ObjectMapper mapper = new ObjectMapper();
  private TypeFactory typeFactory = mapper.getTypeFactory();
  private MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

  public Predict(String[] args) throws CmdLineException {
    super(args);
  }

  @Override
  public void run() throws IOException, ClassifierException {
    Classifier<Document> classifier = loadClassifier(Files.newInputStream(FileSystems.getDefault().getPath(modelLocation)));
    DocumentVectorizer vectorizer = (DocumentVectorizer) StoreUtils.<Document>loadVectorizer(
        Files.newInputStream(FileSystems.getDefault().getPath(termDictLocation)));
    Schema schema = vectorizer.getSchema();

    for (String doc : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> data = mapper.readValue(doc, mapType);

      String docClass = classifier.classify(new Document(schema, data));
      System.out.println(docClass + "|" + data.get("class"));
    }
  }
}
