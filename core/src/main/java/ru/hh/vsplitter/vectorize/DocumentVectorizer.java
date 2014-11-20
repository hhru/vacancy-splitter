package ru.hh.vsplitter.vectorize;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.hh.vsplitter.Document;
import ru.hh.vsplitter.Field;
import ru.hh.vsplitter.Schema;

public class DocumentVectorizer implements Vectorizer<Document> {
  private static final long serialVersionUID = 1L;

  private final Schema schema;
  private final int dimensions;

  public DocumentVectorizer(Schema schema) {
    this.schema = schema;

    int dimensions = 0;
    for (Field<?> field : schema.fields()) {
      dimensions += field.vectorizer().getDimensionCount();
    }
    this.dimensions = dimensions;
  }

  @Override
  public DocVector vectorize(final Document input) {
    return DocVector.concat(Lists.transform(schema.fields(), new Function<Field<?>, DocVector>() {
      @Override
      public DocVector apply(Field<?> field) {
        return field.vectorize(input);
      }
    }));
  }

  @Override
  public int getDimensionCount() {
    return dimensions;
  }

  public Schema getSchema() {
    return schema;
  }
}
