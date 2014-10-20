package ru.hh.vsplitter.modelbuilder.command;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.liblinear.LibLinearClassifier;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainModel extends Command {
  private static final double DEFAULT_REGULARIZATION = 1.0;
  private static final double DEFAULT_TOLERANCE = 0.001;

  private final Logger log = LoggerFactory.getLogger(TrainModel.class);

  @Option(name="-o", metaVar = "OUTPUT file/dir", usage="output file name", required = true)
  private String output;

  @Option(name="-d", metaVar = "file", usage = "term dict location", required = true)
  private String termDictLocation;

  @Option(name="-c", metaVar = "CLASSES", usage = "classes of docs (one for each input)", required = true)
  private List<String> classes;

  @Option(name="-t", usage="stopping criterion of classification")
  private Double tolerance;

  @Option(name="-r", usage="regularization cost")
  private Double regularization;

  @Argument(required = true, metaVar = "FILES", usage = "input matrices of classes")
  private List<String> input;

  public TrainModel(String[] args) throws CmdLineException {
    super(args);
    if (classes.size() != input.size()) {
      throw new CmdLineException(parser, Messages.ILLEGAL_LIST, "classes should contain information about all inputs");
    }
  }

  @Override
  public void run() throws IOException {
    log.info("Loading input vectors data");

    final Vectorizer vectorizer = StoreUtils.loadVectorizer(Files.newInputStream(FileSystems.getDefault().getPath(termDictLocation)));
    Map<String, Collection<DocVector>> trainingData = new HashMap<>();

    for (int i = 0; i < input.size(); ++i) {
      String docClass = classes.get(i);
      List<DocVector> matrix = StoreUtils.loadMatrix(Files.newInputStream(FileSystems.getDefault().getPath(input.get(i))));
      trainingData.put(docClass, matrix);
    }

    log.info("Loaded training data");

    if (regularization == null) {
      regularization = DEFAULT_REGULARIZATION;
    }
    if (tolerance == null) {
      tolerance = DEFAULT_TOLERANCE;
    }

    Classifier classifier = LibLinearClassifier.train(trainingData, vectorizer, regularization, tolerance);
    classifier.save(Files.newOutputStream(FileSystems.getDefault().getPath(output)));
  }
}
