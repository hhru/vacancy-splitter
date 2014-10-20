package ru.hh.vsplitter.classify.liblinear;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.vsplitter.classify.BaseClassifier;
import ru.hh.vsplitter.classify.ClassifierException;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibLinearClassifier extends BaseClassifier {
  private static final long serialVersionUID = -3211309612063885934L;

  private static final Logger log = LoggerFactory.getLogger(LibLinearClassifier.class);
  private static final Logger libLinearLog = LoggerFactory.getLogger(Linear.class);
  static {
    Linear.setDebugOutput(new PrintStream(System.out) {
      public void print(String s) {
        libLinearLog.info(s.trim());
      }
    });
  }


  private final Model model;

  private LibLinearClassifier(Collection<String> classes, Vectorizer vectorizer, Model model) {
    super(classes, vectorizer);
    Preconditions.checkNotNull(model);
    this.model = model;
  }

  @Override
  protected String classify(DocVector vector) throws ClassifierException {
    int classNumber = (int) Math.round(Linear.predict(model, toFeatures(vector)));
    if (classNumber < 0 || classNumber >= classes.size()) {
      throw new ClassifierException("liblinear returned unknown class");
    }
    return classes.get(classNumber);
  }

  private static Feature[] toFeatures(DocVector vector) {
    List<DocVector.Node> nodes = vector.getNodes();
    Feature[] features = new Feature[nodes.size()];

    int previous = -1;
    for (int index = 0; index < nodes.size(); ++index) {
      DocVector.Node node = nodes.get(index);
      if (node.termId <= previous) {
        throw new IllegalArgumentException("Wrong term id order");
      }
      previous = node.termId;
      features[index] = new FeatureNode(node.termId + 1, node.value);
    }

    return features;
  }

  private static final SolverType SOLVER_TYPE = SolverType.L2R_L2LOSS_SVC;
  private static final double CONSTRAINT_COST = 1.0;
  private static final double STOPPING_CRITERION = 1e-3;

  public static LibLinearClassifier train(Map<String, ? extends Collection<DocVector>> trainingData, Vectorizer vectorizer) {
    return train(trainingData, vectorizer, CONSTRAINT_COST, STOPPING_CRITERION);
  }

  public static LibLinearClassifier train(Map<String, ? extends Collection<DocVector>> trainingData, Vectorizer vectorizer,
                                          double constraintCost, double stoppingCriterion) {
    return train(trainingData, vectorizer, SOLVER_TYPE, constraintCost, stoppingCriterion);
  }

  public static LibLinearClassifier train(Map<String, ? extends Collection<DocVector>> trainingData,
                                          Vectorizer vectorizer,
                                          SolverType solverType, double regularization, double stoppingCriterion) {
    List<String> classes = ImmutableList.copyOf(trainingData.keySet());
    Map<String, Integer> classToInt = new HashMap<>();
    for (int classIndex = 0; classIndex < classes.size(); ++classIndex) {
      classToInt.put(classes.get(classIndex), classIndex);
    }

    int totalTrainingSize = 0;
    for (Collection<DocVector> vectors : trainingData.values()) {
      totalTrainingSize += vectors.size();
    }

    Problem problem = new Problem();
    problem.l = totalTrainingSize;
    problem.n = vectorizer.terms().size();
    problem.x = new Feature[totalTrainingSize][];
    problem.y = new double[totalTrainingSize];

    int observationNumber = 0;

    for (Map.Entry<String, ? extends Collection<DocVector>> entry : trainingData.entrySet()) {
      int classNum = classToInt.get(entry.getKey());
      for (DocVector vector : entry.getValue()) {
        problem.x[observationNumber] = toFeatures(vector);
        problem.y[observationNumber] = classNum;
        ++observationNumber;
      }
    }


    Parameter parameter = new Parameter(solverType, regularization, stoppingCriterion);
    log.info("Converted all entries to svm representation, actual training started");
    Model model = Linear.train(problem, parameter);
    return new LibLinearClassifier(classes, vectorizer, model);
  }

  public static LibLinearClassifier loadFromStream(InputStream inputStream) throws IOException {
    try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
      return (LibLinearClassifier) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

  private static class SerialForm implements Serializable {
    private static final long serialVersionUID = 433858939919659541L;

    public List<String> classes;
    public Vectorizer vectorizer;
    public byte[] model;

    public SerialForm(LibLinearClassifier classifier) {
      classes = classifier.classes;
      vectorizer = classifier.vectorizer;

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos))) {
        classifier.model.save(writer);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }

      model = bos.toByteArray();
    }

    protected Object readResolve() throws ObjectStreamException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(model)));
      try {
        return new LibLinearClassifier(classes, vectorizer, Model.load(reader));
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  protected Object writeReplace() throws ObjectStreamException {
    return new SerialForm(this);
  }

  @Override
  public boolean equals(Object another) {
    if (this == another) {
      return true;
    }
    if (another == null || getClass() != another.getClass()) {
      return false;
    }

    LibLinearClassifier that = (LibLinearClassifier) another;

    return classes.equals(that.classes) && vectorizer.equals(that.vectorizer) && model.equals(that.model);
  }

  @Override
  public int hashCode() {
    int code = classes.hashCode();
    code = 31 * code + vectorizer.hashCode();
    return 31 * code + model.hashCode();
  }
}
