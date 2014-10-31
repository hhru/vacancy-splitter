Vacancy Splitter
======

Vacancy splitter is library intended to help train and use classifier, which splits vacancy texts into meaningful blocks.  
Current implementation uses plain tf-idf vectorizer and linear svm classifier.

Library usage example:

```java
Map<String, VacancyBlock> splitVacancyIntoParts(String vacancyXml) {
    ObjectInputStream englishClassifierStream = ObjectInputStream(...);
    Classifier englishClassifier = (Classifier) englishClassifierStream.readObject();
    
    ObjectInputStream russianClassifierStream = ObjectInputStream(...);
    Classifier russianClassifier = (Classifier) russianClassifierStream.readObject();
    
    Map<String, VacancyBlock> labelToClassMapping = ImmutableMap.of(
        "req", REQUIREMENTS, "res", RESPONSIBILITIES, "con", CONDITIONS
    );
    
    VacancySplitter splitter = new VacancySplitter(englishClassifier, russianClassifier, labelToClassMapping);
    return splitter.split(vacancyText);
}
```

* Note that split method of VacancySplitter accepts only **valid xml** as input.
* VacancySplitter **is not thread-safe**, and it's creation could be fairly expensive. Instances of this class are 
intended to be reused within single thread for proper performance results.
* Models in model/ directory are valid ready-to-use java serialized Classifier instances.


This project is being developed for [hh.ru](http://hh.ru) online hiring services.
