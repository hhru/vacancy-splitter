package ru.hh.vsplitter.webdemo;

import com.google.common.base.Throwables;
import org.xml.sax.helpers.DefaultHandler;
import ru.hh.vsplitter.classify.Classifier;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Set;

import static java.util.Arrays.asList;
import static ru.hh.vsplitter.webdemo.BlockType.REQUIREMENTS;
import static ru.hh.vsplitter.webdemo.BlockType.RESPONSIBILITIES;

public class MarkerInjector {

  private ThreadLocal<BlocksClassifier> classifierHolder = new ThreadLocal<>();
  private ThreadLocal<SAXParserFactory> parserFactoryHolder = new ThreadLocal<>();
  private String rusModel;
  private String engModel;

  public void setRusModel(String rusModel) {
    this.rusModel = rusModel;
  }

  public void setEngModel(String engModel) {
    this.engModel = engModel;
  }

  private BlocksClassifier getClassifier() {
    BlocksClassifier classifier = classifierHolder.get();
    if (classifier == null) {
      try (
          ObjectInputStream rusStream = new ObjectInputStream(getClass().getResourceAsStream(rusModel));
          ObjectInputStream engStream = new ObjectInputStream(getClass().getResourceAsStream(engModel))
          ) {
        classifier = new BlocksClassifier((Classifier) engStream.readObject(), (Classifier) rusStream.readObject());
        classifierHolder.set(classifier);
      } catch (IOException | ClassNotFoundException e) {
        throw Throwables.propagate(e);
      }
    }
    return classifier;
  }

  private SAXParserFactory getFactory() {
    SAXParserFactory factory = parserFactoryHolder.get();
    if (factory == null) {
      factory = SAXParserFactory.newInstance();
      parserFactoryHolder.set(factory);
    }
    return factory;
  }

  public String inject(String input) {
    TextHandler textHandler = new TextHandler();
    parseXml(input, textHandler);
    Language language = Language.fromText(textHandler.getText());

    VacancyXmlHandler handler = new VacancyXmlHandler(getClassifier(), language, true);
    parseXml(input, handler);

    Set<BlockType> types = handler.getTypes();
    if ((language == Language.RUSSIAN && types.size() < 3) ||
        (language == Language.ENGLISH && !(types.containsAll(asList(REQUIREMENTS, RESPONSIBILITIES))))) {
      handler = new VacancyXmlHandler(getClassifier(), language, false);
      parseXml(input, handler);
    }

    return handler.getOutput();
  }

  private void parseXml(String input, DefaultHandler handler) {
    SAXParserFactory factory = getFactory();
    try(InputStream inputStream = new ByteArrayInputStream(input.getBytes())) {
      SAXParser xmlParser = factory.newSAXParser();
      xmlParser.parse(inputStream, handler);
    } catch (Exception e) {
      Throwables.propagate(e);
    }
  }

}
