package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class RingConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

    // First line is the header
    String line = br.readLine();
    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Name", fields[0]);
      jsonObject.put("Cost", fields[1]);
      jsonObject.put("Extrinsic granted", fields[2]);
      if (fields.length > 3) {
        jsonObject.put("Notes", fields[3]);
      }
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("rings.txt");
  }
}
