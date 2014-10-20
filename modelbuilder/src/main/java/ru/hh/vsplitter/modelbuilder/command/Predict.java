package ru.hh.vsplitter.modelbuilder.command;

import com.google.common.collect.Iterables;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.ClassifierException;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.modelbuilder.StoreUtils;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Predict extends Command {

  @Option(name="-m", aliases = {"--model"}, usage = "model file location", required = true)
  private String modelLocation;

  @Argument(required = true, metaVar = "INPUT")
  private List<String> input;

  public Predict(String[] args) throws CmdLineException {
    super(args);
  }

  @Override
  public void run() throws IOException, ClassifierException {
    List<Iterable<String>> fileLinesList = new ArrayList<>();
    for (String fileName: input) {
      fileLinesList.add(FileLinesIterable.fromPath(FileSystems.getDefault().getPath(fileName)));
    }
    Classifier classifier = StoreUtils.loadClassifier(Files.newInputStream(FileSystems.getDefault().getPath(modelLocation)));

    for (String doc : Iterables.concat(fileLinesList)) {
      String docClass = classifier.classify(doc);
      System.out.println(docClass + "|" + doc);
    }
  }
}
