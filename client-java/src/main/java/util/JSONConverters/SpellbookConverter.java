package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class SpellbookConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

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
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Category", category);
      jsonObject.put("Name", fields[0]);
      jsonObject.put("Spell level", Integer.parseInt(fields[1]));
      jsonObject.put("Direction", fields[2]);
      jsonObject.put("Relative probability", Double.parseDouble(fields[3].replace("%", "")));
      jsonObject.put(
          "Probability conditional on price", Double.parseDouble(fields[4].replace("%", "")));
      jsonObject.put("Actions to read", Integer.parseInt(fields[5]));
      if (fields.length > 6) {
        jsonObject.put("Skill changes", fields[6]);
      }
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("spellbooks.txt");
  }
}
