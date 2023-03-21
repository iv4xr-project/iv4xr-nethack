package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RingConverter extends JSONConverter {
  public String getFileName() {
    return "rings";
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
      objectNode.put("Name", fields[0]);
      objectNode.put("Cost", fields[1]);
      objectNode.put("Extrinsic granted", fields[2]);
      if (fields.length > 3) {
        objectNode.put("Notes", fields[3]);
      }
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
