package ru.hh.vsplitter.modelbuilder.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.vsplitter.Document;
import ru.hh.vsplitter.Schema;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.liblinear.LibLinearClassifier;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.DocumentVectorizer;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TrainModel extends Command {
  private static final double DEFAULT_REGULARIZATION = 1.0;
  private static final double DEFAULT_TOLERANCE = 0.01;

  private final Logger log = LoggerFactory.getLogger(TrainModel.class);

  @Option(name="-o", metaVar = "OUTPUT file/dir", usage="output file name", required = true)
  private String output;

  @Option(name="-d", metaVar = "file", usage = "term dict location", required = true)
  private String termDictLocation;

  @Option(name="-t", usage="stopping criterion of classification")
  private Double tolerance;

  @Option(name="-r", usage="regularization cost")
  private Double regularization;

  @Argument(required = true, metaVar = "FILE", usage = "input")
  private String input;

  public TrainModel(String[] args) throws CmdLineException {
    super(args);
  }

  private ObjectMapper mapper = new ObjectMapper();
  private TypeFactory typeFactory = mapper.getTypeFactory();
  private MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

  private Schema schema;

  @Override
  public void run() throws IOException {
    log.info("Loading input vectors data");

    final DocumentVectorizer vectorizer = (DocumentVectorizer) StoreUtils.<Document>loadVectorizer(
        Files.newInputStream(FileSystems.getDefault().getPath(termDictLocation)));

    schema = vectorizer.getSchema();
    Map<String, Collection<DocVector>> trainingData = new HashMap<>();
    for (String line : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> data = mapper.readValue(line, mapType);

      String type = data.get("class");
      Collection<DocVector> vectors = trainingData.get(type);
      if (vectors == null) {
        vectors = new ArrayList<>();
        trainingData.put(type, vectors);
      }

      vectors.add(vectorizer.vectorize(new Document(schema, data)));
    }

    log.info("Loaded training data");

    if (regularization == null) {
      regularization = DEFAULT_REGULARIZATION;
    }
    if (tolerance == null) {
      tolerance = DEFAULT_TOLERANCE;
    }

    Classifier<Document> classifier = LibLinearClassifier.train(trainingData, vectorizer, regularization, tolerance);
    classifier.save(Files.newOutputStream(FileSystems.getDefault().getPath(output)));
  }
}
