package ru.hh.vsplitter.modelbuilder;

import com.google.common.base.Throwables;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.OptionHandlerFilter;
import ru.hh.vsplitter.modelbuilder.command.Experiment;
import ru.hh.vsplitter.modelbuilder.command.Predict;
import ru.hh.vsplitter.modelbuilder.command.TermDict;
import ru.hh.vsplitter.modelbuilder.command.TrainModel;
import ru.hh.vsplitter.modelbuilder.command.VectorizeDoc;
import java.util.Arrays;

public class Runner {
  public static void main(String[] args) {
    (new Runner()).run(args);
  }

  private void run(String[] args) {
    String command = args[0];
    try {
      args = Arrays.copyOfRange(args, 1, args.length);

      switch (command) {
        case "termdict":
          new TermDict(args).run();
          break;
        case "vectorize":
          new VectorizeDoc(args).run();
          break;
        case "train":
          new TrainModel(args).run();
          break;
        case "predict":
          new Predict(args).run();
          break;
        case "experiment":
          new Experiment(args).run();
          break;
        default:
          throw new IllegalArgumentException("Illegal Command");
      }
    } catch (CmdLineException e) {
      System.err.println(e.getLocalizedMessage());

      System.err.println("\nUsage: java -jar modelbuilder.jar" + e.getParser().printExample(OptionHandlerFilter.ALL));
      e.getParser().printUsage(System.err);
    } catch (Throwable t) {
      throw Throwables.propagate(t);
    }
  }
}
