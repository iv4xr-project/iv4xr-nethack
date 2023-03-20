package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScrollConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Weight", 5);
      jsonObject.put("Name", fields[0]);
      jsonObject.put("Cost", Integer.parseInt(fields[1]));
      jsonObject.put("Relative probability", Double.parseDouble(fields[2].replace("%", "")));
      jsonObject.put("Ink", Integer.parseInt(fields[3]));
      jsonObject.put("Appearance", fields[4]);
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("scrolls.txt");
  }
}
