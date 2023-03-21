package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToolConverter extends JSONConverter {
  public String getFileName() {
    return "tools";
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
      if (fields.length <= 2) {
        category = fields[0];
        continue;
      }
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("Category", category);
      objectNode.put("Name", fields[0]);
      objectNode.put("Cost", Integer.parseInt(fields[1].replace(" zm", "")));
      objectNode.put("Weight", Integer.parseInt(fields[2]));
      objectNode.put("Cost:weight ratio", Double.parseDouble(fields[3]));
      objectNode.put("Use (where not obvious)", fields[4]);
      //        if (!fields[5].equals("")) {
      //          objectNode.put("Prob (‰)", Integer.parseInt(fields[5]));
      //        }
      objectNode.put("Creation", fields[6]);
      objectNode.put("Magic?", fields[7].equals("Yes"));
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
