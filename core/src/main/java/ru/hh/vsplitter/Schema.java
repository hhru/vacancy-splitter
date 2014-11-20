package ru.hh.vsplitter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;

public final class Schema implements Serializable {
  private static final long serialVersionUID = 1L;

  final ImmutableList<Field<?>> fields;
  final ImmutableMap<String, Field<?>> fieldMap;

  public Schema(List<Field<?>> fields) {
    this.fields = ImmutableList.copyOf(fields);
    ImmutableMap.Builder<String, Field<?>> mapBuilder = ImmutableMap.builder();
    for (Field<?> field : fields) {
      mapBuilder.put(field.name(), field);
    }
    fieldMap = mapBuilder.build();
  }

  public List<Field<?>> fields() {
    return fields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Schema schema = (Schema) o;

    return fields.equals(schema.fields);
  }

  @Override
  public int hashCode() {
    return fields.hashCode();
  }

  public <T> Field<T> field(String name) {
    return (Field<T>) fieldMap.get(name);
  }
}
