package nethack;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import util.MutationData;

public class Sandbox {
  @ParameterizedTest
  @MethodSource("parameters")
  public void myTest(MutationData.Mutant mutant) {
    System.out.println(mutant.path);
    System.out.println(mutant.line_number);
    System.out.println(mutant.content);
  }

  static Stream<MutationData.Mutant> parameters() {
    // Create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON file and parse it into Data object
    MutationData data = null;
    try {
      data =
          objectMapper.readValue(
              new File("src/test/resources/mutation_info.json"), MutationData.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return data.mutants.stream();
  }

  @Test
  public void test2() {
    System.out.println("      LINE 1: HIHIHIHIHI");
    System.out.print("         LINE 2: HIHIHIHIHI\n");
    System.out.printf("        LINE 3: %s\n", "HIHIHIHIHI");
    System.out.printf("         LINE 4: %s%n", "HIHIHIHIHI");
  }

  public static void main(String[] args) throws IOException {
    // Create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // Read JSON file and parse it into Data object
    MutationData data =
        objectMapper.readValue(
            new File("src/test/resources/mutation_info.json"), MutationData.class);

    // Access the parsed data
    System.out.println("File: " + data.file);
    System.out.println("Lines: " + data.lines);
    System.out.println("Original: " + data.original);
    System.out.println("Mutants: " + data.mutants);
  }
}
