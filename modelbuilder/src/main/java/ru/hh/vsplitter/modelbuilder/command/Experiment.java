package ru.hh.vsplitter.modelbuilder.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.vectorize.CountingVectorizer;
import ru.hh.vsplitter.vectorize.DocVector;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.google.common.math.DoubleMath.log2;

public class Experiment extends Command {

  static class Counter {
    private int value = 0;

    public void increment() {
      ++value;
    }

    public int getValue() {
      return value;
    }
  }

  static class EntropyComputer<T> {
    int total = 0;
    private Map<T, Counter> valueCounters = new HashMap<>();

    public void registerOccurrence(T value) {
      ++total;

      Counter counter = valueCounters.get(value);
      if (counter == null) {
        counter = new Counter();
        valueCounters.put(value, counter);
      }
      counter.increment();
    }

    public double getEntropy() {
      double entropy = 0.0;
      double logTotal = log2(total);

      for (Counter counter: valueCounters.values()) {
        double count = counter.getValue();
        entropy -= count * (log2(count) - logTotal) / total;
      }

      return entropy;
    }

    public int getTotal() {
      return total;
    }
  }

  static class ConditionalEntropyComputer<T, A> {
    int total = 0;
    private Map<A, EntropyComputer<T>> entropyPerValue = new HashMap<>();

    public void registerOccurrence(A attribute, T value) {
      ++total;

      EntropyComputer<T> attrEntropy = entropyPerValue.get(attribute);
      if (attrEntropy == null) {
        attrEntropy = new EntropyComputer<>();
        entropyPerValue.put(attribute, attrEntropy);
      }
      attrEntropy.registerOccurrence(value);
    }

    public double getEntropy() {
      double entropy = 0.0;
      for (EntropyComputer<T> computer: entropyPerValue.values()) {
        entropy += (computer.getTotal() * computer.getEntropy()) / total;
      }
      return entropy;
    }
  }

  static class Attribute {
    public String term;
    public int index;
    public double gain;

    public Attribute(int index, String term) {
      this.index = index;
      this.term = term;
    }

    public String toString() {
      return new StringBuilder().append(index).append("  ").append(term).append(" : ").append(gain).toString();
    }
  }

  @Argument(required = true, metaVar = "FILES")
  private String input;

  public Experiment(String[] args) throws CmdLineException {
    super(args);
  }

  @Override
  public void run() throws Exception {
    Set<String> stopWords = ImmutableSet.copyOf(FileLinesIterable.fromResource(getClass().getClassLoader(), "stopwords"));

    CountingVectorizer.Builder builder = CountingVectorizer.newBuilder()
        .setThreshold(3)
        .setStopWords(stopWords)
        .setStem(true)
        .setBoolMode(true);

    for (String line : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> observation = deserializeMap(line);
      builder.feed(observation.get("text"));
    }

    CountingVectorizer vectorizer = builder.build();

    List<DocVector> vectors = new ArrayList<>();
    List<String> classes = new ArrayList<>();
    for (String line : FileLinesIterable.fromPath(FileSystems.getDefault().getPath(input))) {
      Map<String, String> observation = deserializeMap(line);
      DocVector vector = vectorizer.vectorize(observation.get("text"));
      if (!vector.isEmpty()) {
        vectors.add(vector);
        classes.add(observation.get("class"));
      }
    }

    EntropyComputer<String> totalEntropyComputer = new EntropyComputer<>();

    List<Attribute> attributes = new ArrayList<>();
    List<ConditionalEntropyComputer<String, Double>> entropyComputers = new ArrayList<>();

    List<String> terms = vectorizer.terms();
    for (int attr = 0; attr < terms.size(); ++attr) {
      attributes.add(new Attribute(attr, terms.get(attr)));
      entropyComputers.add(new ConditionalEntropyComputer<String, Double>());
    }

    for (int doc = 0; doc < vectors.size(); ++doc) {
      DocVector vector = vectors.get(doc);
      String classValue = classes.get(doc);

      totalEntropyComputer.registerOccurrence(classValue);

      int attr = 0;
      for (DocVector.Node node : vector.getNodes()) {
        while (attr < node.termId) {
          entropyComputers.get(attr).registerOccurrence(0.0, classValue);
          ++attr;
        }

        entropyComputers.get(attr).registerOccurrence(node.value, classValue);
      }

      while (attr < attributes.size()) {
        entropyComputers.get(attr).registerOccurrence(0.0, classValue);
        ++attr;
      }
    }

    double totalEntropy = totalEntropyComputer.getEntropy();
    for (Attribute attribute : attributes) {
      attribute.gain = totalEntropy - entropyComputers.get(attribute.index).getEntropy();
    }

    Collections.sort(attributes, new Comparator<Attribute>() {
      @Override
      public int compare(Attribute o1, Attribute o2) {
        return Doubles.compare(o2.gain, o1.gain);
      }
    });

    for (Attribute attribute : attributes) {
      System.out.println(attribute);
    }
  }

  private ObjectMapper mapper = new ObjectMapper();
  private TypeFactory typeFactory = mapper.getTypeFactory();
  private MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

  private Map<String, String> deserializeMap(String json) {
    try {
      return mapper.reader(mapType).readValue(json);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
