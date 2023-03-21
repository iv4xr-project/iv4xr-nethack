package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeaponConverter extends JSONConverter {
  public String getFileName() {
    return "weapons";
  }

  @Override
  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();
    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      line = line.replace("†", "");
      String[] fields = line.split("\t");
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("name", fields[0]);
      objectNode.put("skill", fields[1]);
      objectNode.put("cost", Integer.parseInt(fields[2].replace(" zm", "")));
      objectNode.put("weight", Integer.parseInt(fields[4]));
      objectNode.put("nrHands", Integer.parseInt(fields[3]));
      //        objectNode.put("Prob (‰)", Integer.parseInt(fields[5].replaceAll("[^\\d.]", "")));
      objectNode.put("damageToSmall", Double.parseDouble(fields[6]));
      objectNode.put("damageToLarge", Double.parseDouble(fields[7]));
      objectNode.put("material", fields[8].toUpperCase());
      if (!fields[9].equals("")) {
        objectNode.put("appearance", fields[9]);
      }
      //        objectNode.put("Tile", fields[10]);
      //      objectNode.put("Glyph", fields[11]);
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
