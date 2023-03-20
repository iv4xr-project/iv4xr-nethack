package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.*;

public class AmuletConverter extends JSONConverter {
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Amulet", fields[0]);
      jsonObject.put(
          "Relative probability", Double.parseDouble(fields[1].replaceAll("[^\\d.]", "")));
      jsonObject.put("Cost", 150);
      jsonObject.put("weight", 20);
      if (fields.length >= 3) {
        jsonObject.put("When eaten", fields[2]);
      }
      if (fields.length >= 4) {
        jsonObject.put("Notes", fields[3]);
      }
      if (fields.length >= 5) {
        jsonObject.put("produces the line...", fields[4]);
      }
      if (fields.length >= 6) {
        jsonObject.put("Other sources", fields[5]);
      }
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("amulets.txt");
  }
}
