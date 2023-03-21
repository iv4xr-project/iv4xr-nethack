package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArmorConverter extends JSONConverter {
  public String getFileName() {
    return "armor";
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
      objectNode.put("Cost", Integer.parseInt(fields[1]));
      objectNode.put("Weight", Integer.parseInt(fields[2]));
      objectNode.put("AC", Integer.parseInt(fields[3]));
      double weightPerAC = fields[4].equals("Infinite") ? 0 : Double.parseDouble(fields[4]);
      objectNode.put("Weight per AC (+0)", weightPerAC);
      String[] maxAc = fields[5].split(",");
      int maxAcValue = Integer.parseInt(maxAc[maxAc.length - 1].split(" ")[0]);
      objectNode.put("Max AC", maxAcValue);
      String[] weightPerMaxAc = fields[6].split(",");
      double weightPerMaxAcValue =
          Double.parseDouble(weightPerMaxAc[weightPerMaxAc.length - 1].split(" ")[0]);
      objectNode.put("Weight per max AC", weightPerMaxAcValue);
      objectNode.put("Material", fields[7]);
      objectNode.put("Effect", fields[8].replace("%", ""));
      objectNode.put(
          "Magical Cancellation", fields[9].equals("") ? 0 : Integer.parseInt(fields[9]));
      if (!fields[10].equals("") && !fields[10].equals("--")) {
        objectNode.put("Prob", Integer.parseInt(fields[10]));
      }
      objectNode.put("Magical", fields[11].equals("Yes"));
      if (!fields[12].equals("--")) {
        objectNode.put("Appearance", fields[12]);
      }
      objectNode.put("Turns to (un)equip", Integer.parseInt(fields[13]));
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
