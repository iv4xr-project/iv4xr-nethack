package util;

import java.util.List;

public class MutationData {
  public String file;
  public List<Integer> lines;
  public List<Line> original;
  public List<Mutant> mutants;

  public static class Line {
    public String line_number;
    public String content;
  }

  public static class Mutant {
    public String path;
    public String line_number;
    public String content;

    public String toString() {
      return String.format("path:%s", path);
    }
  }
}
