package ru.hh.vsplitter.classify.liblinear;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.ClassifierException;
import ru.hh.vsplitter.ioutil.FileLinesIterable;
import ru.hh.vsplitter.vectorize.DocVector;
import ru.hh.vsplitter.vectorize.Vectorizer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;

public class LibLinearClassifierTest {

  static final List<String> CLASSES = ImmutableList.of("requirements", "responsibilities", "conditions", "nothing");

  private Vectorizer vectorizer;
  private Map<String, List<DocVector>> docCorpus;

  @BeforeClass
  public void before() {
    Map<String, List<String>> trainingData = new HashMap<>();

    for (String className : CLASSES) {
      trainingData.put(className, ImmutableList.copyOf(
          FileLinesIterable.fromResource(Thread.currentThread().getContextClassLoader(), "training/" + className)));
    }

    vectorizer = Vectorizer.fromDocCorpus(Iterables.concat(trainingData.values()), 3, Collections.<String>emptySet(), true);

    docCorpus = new HashMap<>();
    for (String className : CLASSES) {
      List<DocVector> currentVectors = new ArrayList<>();
      for (String doc : trainingData.get(className)) {
        currentVectors.add(vectorizer.vectorize(doc));
      }
      docCorpus.put(className, currentVectors);
    }
  }

  @Test
  public void testClassification() throws IOException, URISyntaxException, ClassifierException {
    Classifier classifier = LibLinearClassifier.train(docCorpus, vectorizer);

    assertEquals(classifier.classify("опыт работы в строительстве"), "requirements");
    assertEquals(classifier.classify("высшее образование"), "requirements");
    assertEquals(classifier.classify("ответственность и внимательность"), "requirements");

    assertEquals(classifier.classify("бумажная работа"), "responsibilities");
    assertEquals(classifier.classify("ведение переговоров"), "responsibilities");
    assertEquals(classifier.classify("косультирование клиентов"), "responsibilities");

    assertEquals(classifier.classify("оформление по тк рф"), "conditions");
    assertEquals(classifier.classify("свободный график работы"), "conditions");
    assertEquals(classifier.classify("дружный коллектив"), "conditions");
  }

  @Test
  public void testSerialization() throws IOException, ClassNotFoundException {
    // need to serialize at first time because of minor bug in liblinear double serialization
    Classifier original = fromByteArray(toByteArray(LibLinearClassifier.train(docCorpus, vectorizer)));
    byte[] copy = toByteArray(original);
    assertEquals(original, fromByteArray(copy));
  }

  private static byte[] toByteArray(Classifier classifier) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      classifier.save(bos);
      return bos.toByteArray();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private static Classifier fromByteArray(byte[] bytes) {
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return (Classifier) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

}
