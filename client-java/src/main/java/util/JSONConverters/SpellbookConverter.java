package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpellbookConverter extends JSONConverter {
  public String getFileName() {
    return "spellbooks";
  }

  @Override
  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();
    String category = null;
    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      if (fields.length <= 1) {
        category = fields[0];
        continue;
      }
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("Category", category);
      objectNode.put("Name", fields[0]);
      objectNode.put("Spell level", Integer.parseInt(fields[1]));
      objectNode.put("Direction", fields[2]);
      objectNode.put("Relative probability", Double.parseDouble(fields[3].replace("%", "")));
      objectNode.put(
          "Probability conditional on price", Double.parseDouble(fields[4].replace("%", "")));
      objectNode.put("Actions to read", Integer.parseInt(fields[5]));
      if (fields.length > 6) {
        objectNode.put("Skill changes", fields[6]);
      }
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
