package ru.hh.vsplitter.webdemo;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.split.SplitterException;
import ru.hh.vsplitter.split.VacancyBlock;
import ru.hh.vsplitter.split.VacancySplitter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import static ru.hh.vsplitter.split.VacancyBlock.CONDITIONS;
import static ru.hh.vsplitter.split.VacancyBlock.REQUIREMENTS;
import static ru.hh.vsplitter.split.VacancyBlock.RESPONSIBILITIES;

public class ThreadSafeSplitter {

  private static final Map<String, VacancyBlock> BLOCK_MAPPING= ImmutableMap.of(
      "req", REQUIREMENTS, "res", RESPONSIBILITIES, "con", CONDITIONS
  );

  private ThreadLocal<VacancySplitter> splitterHolder = new ThreadLocal<>();

  private String rusModel;
  private String engModel;

  public void setRusModel(String rusModel) {
    this.rusModel = rusModel;
  }

  public void setEngModel(String engModel) {
    this.engModel = engModel;
  }

  private VacancySplitter getSplitter() {
    VacancySplitter splitter = splitterHolder.get();
    if (splitter == null) {
      try (
          ObjectInputStream rusStream = new ObjectInputStream(getClass().getResourceAsStream(rusModel));
          ObjectInputStream engStream = new ObjectInputStream(getClass().getResourceAsStream(engModel))
      ) {
        splitter = new VacancySplitter((Classifier) engStream.readObject(), (Classifier) rusStream.readObject(), BLOCK_MAPPING);
        splitterHolder.set(splitter);
      } catch (IOException | ClassNotFoundException e) {
        throw Throwables.propagate(e);
      }
    }
    return splitter;
  }

  public Map<VacancyBlock, String> split(String xml) {
    try {
      return getSplitter().split(xml);
    } catch (SplitterException e) {
      throw Throwables.propagate(e);
    }
  }

}
