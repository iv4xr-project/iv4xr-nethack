package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ToolConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

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
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Category", category);
      jsonObject.put("Name", fields[0]);
      jsonObject.put("Cost", Integer.parseInt(fields[1].replace(" zm", "")));
      jsonObject.put("Weight", Integer.parseInt(fields[2]));
      jsonObject.put("Cost:weight ratio", Double.parseDouble(fields[3]));
      jsonObject.put("Use (where not obvious)", fields[4]);
      //        if (!fields[5].equals("")) {
      //          jsonObject.put("Prob (‰)", Integer.parseInt(fields[5]));
      //        }
      jsonObject.put("Creation", fields[6]);
      jsonObject.put("Magic?", fields[7].equals("Yes"));
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("tools.txt");
  }
}
