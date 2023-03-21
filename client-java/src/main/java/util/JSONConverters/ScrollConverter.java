package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrollConverter extends JSONConverter {
  public String getFileName() {
    return "scrolls";
  }

  @Override
  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("Weight", 5);
      objectNode.put("Name", fields[0]);
      objectNode.put("Cost", Integer.parseInt(fields[1]));
      objectNode.put("Relative probability", Double.parseDouble(fields[2].replace("%", "")));
      objectNode.put("Ink", Integer.parseInt(fields[3]));
      objectNode.put("Appearance", fields[4]);
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
