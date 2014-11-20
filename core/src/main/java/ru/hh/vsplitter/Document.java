package ru.hh.vsplitter;

import java.util.HashMap;
import java.util.Map;

public final class Document {

  private final Schema schema;
  private final Map<Field<?>, Object> values;

  public Document(Schema schema) {
    this.schema = schema;
    values = new HashMap<>(schema.fields().size());
  }

  public Document(Schema schema, Map<String, ?> values) {
    this(schema);
    for (Map.Entry<String, ?> entry : values.entrySet()) {
      setValue(entry.getKey(), entry.getValue());
    }
  }

  public Schema schema() {
    return schema;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(Field<T> field) {
    return (T) values.get(field);
  }

  public <T> void setValue(String fieldName, T value) {
    values.put(schema.field(fieldName), value);
  }

  public <T> void setValue(Field<T> field, T value) {
    values.put(field, value);
  }

}
