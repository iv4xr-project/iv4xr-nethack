package util.jsonConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoodConverter extends JSONConverter {
  public String getFileName() {
    return "foods";
  }

  @Override
  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      line = line.replace("*", "");
      String[] fields = line.split("\t");
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("name", fields[0]);
      objectNode.put("cost", Integer.parseInt(fields[1]));
      if (!fields[2].equals("varies")) {
        objectNode.put("weight", Integer.parseInt(fields[2]));
      }
      //      if (!fields[3].equals("—")) {
      //        objectNode.put("probability", Double.parseDouble(fields[3]));
      //      }
      if (!fields[4].equals("varies")) {
        String[] vals = fields[4].split("–");
        double nutrition;
        if (vals.length == 1) {
          nutrition = Double.parseDouble(vals[0]);
        } else {
          nutrition = (Double.parseDouble(vals[0]) + Double.parseDouble(vals[1])) / 2;
        }
        objectNode.put("nutrition", nutrition);
      }
      if (!fields[5].equals("varies")) {
        objectNode.put("nutritionPerWeight", Double.parseDouble(fields[5]));
      }
      if (!fields[6].equals("varies") && fields[6].split("–").length == 1) {
        objectNode.put("consumeTime", Integer.parseInt(fields[6]));
      }
      if (!fields[7].equals("varies")) {
        objectNode.put("nutritionPerConsumeTime", Integer.parseInt(fields[4]));
      }
      objectNode.put("foodConduct", fields[8].toUpperCase());
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
