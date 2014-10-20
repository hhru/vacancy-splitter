package ru.hh.vsplitter.vectorize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class DocVector implements Serializable {
  private static final long serialVersionUID = -5229735520323724857L;

  public static class Node implements Serializable {
    private static final long serialVersionUID = 1479191130948761712L;

    public final int termId;
    public final double value;

    public Node(int termId, double value) {
      this.termId = termId;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Node node = (Node) o;
      return termId == node.termId && value == node.value;
    }

    @Override
    public int hashCode() {
      long longBits = Double.doubleToLongBits(value);
      return 31 * termId + (int) (longBits ^ (longBits >>> 32));
    }

    @Override
    public String toString() {
      return "{" + termId + ":" + value + "}";
    }
  }

  private final Node[] nodes;

  private DocVector(Node[] nodes) {
    this.nodes = nodes;
  }

  public static DocVector fromId2Value(SortedMap<Integer, Double> idToValue) {
    Node[] nodes = new Node[idToValue.size()];
    int nodeId = 0;
    for (Map.Entry<Integer, Double> idAndValue : idToValue.entrySet()) {
      nodes[nodeId++] = new Node(idAndValue.getKey(), idAndValue.getValue());
    }
    return new DocVector(nodes);
  }

  public static DocVector fromDense(double... denseVector) {
    List<Node> nodeList = new ArrayList<>();

    for (int id = 0; id < denseVector.length; ++id) {
      if (denseVector[id] != 0.0) {
        nodeList.add(new Node(id, denseVector[id]));
      }
    }

    return new DocVector(nodeList.toArray(new Node[nodeList.size()]));
  }

  public boolean isEmpty() {
    return nodes.length == 0;
  }

  public List<Node> getNodes() {
    return Arrays.asList(nodes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DocVector docVector = (DocVector) o;
    return Arrays.equals(nodes, docVector.nodes);
  }

  @Override
  public int hashCode() {
    return nodes != null ? Arrays.hashCode(nodes) : 0;
  }

  @Override
  public String toString() {
    return "DocVector{" + Arrays.toString(nodes) + "}";
  }
}
