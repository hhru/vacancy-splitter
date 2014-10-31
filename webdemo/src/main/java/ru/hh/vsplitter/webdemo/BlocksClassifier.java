package ru.hh.vsplitter.webdemo;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import ru.hh.vsplitter.classify.Classifier;
import ru.hh.vsplitter.classify.ClassifierException;

import java.util.Map;

public class BlocksClassifier {
  private final Map<Language, Classifier> classifiers;

  private static final ImmutableMap<String, BlockType> blockTypes;
  static {
    ImmutableMap.Builder<String, BlockType> blockTypesBuilder = ImmutableMap.builder();
    for (BlockType blockType : BlockType.values()) {
      blockTypesBuilder.put(blockType.getInternalName(), blockType);
    }
    blockTypes = blockTypesBuilder.build();
  }

  public BlocksClassifier(Classifier engClassifier, Classifier rusClassifier) {
    classifiers = ImmutableMap.of(
        Language.ENGLISH, engClassifier,
        Language.RUSSIAN, rusClassifier);
  }

  public BlockType classify(String text, Language language) {
    try {
      BlockType blockType = blockTypes.get(classifiers.get(language).classify(text));
      return blockType != null ? blockType : BlockType.NOTHING;
    } catch (ClassifierException e) {
      throw Throwables.propagate(e);
    }
  }

}
