package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ArmorConverter extends JSONConverter {
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
      jsonObject.put("Cost", Integer.parseInt(fields[1]));
      jsonObject.put("Weight", Integer.parseInt(fields[2]));
      jsonObject.put("AC", Integer.parseInt(fields[3]));
      double weightPerAC = fields[4].equals("Infinite") ? 0 : Double.parseDouble(fields[4]);
      jsonObject.put("Weight per AC (+0)", weightPerAC);
      String[] maxAc = fields[5].split(",");
      int maxAcValue = Integer.parseInt(maxAc[maxAc.length - 1].split(" ")[0]);
      jsonObject.put("Max AC", maxAcValue);
      String[] weightPerMaxAc = fields[6].split(",");
      double weightPerMaxAcValue =
          Double.parseDouble(weightPerMaxAc[weightPerMaxAc.length - 1].split(" ")[0]);
      jsonObject.put("Weight per max AC", weightPerMaxAcValue);
      jsonObject.put("Material", fields[7]);
      jsonObject.put("Effect", fields[8].replace("%", ""));
      jsonObject.put(
          "Magical Cancellation", fields[9].equals("") ? 0 : Integer.parseInt(fields[9]));
      if (!fields[10].equals("") && !fields[10].equals("--")) {
        jsonObject.put("Prob", Integer.parseInt(fields[10]));
      }
      jsonObject.put("Magical", fields[11].equals("Yes"));
      if (!fields[12].equals("--")) {
        jsonObject.put("Appearance", fields[12]);
      }
      jsonObject.put("Turns to (un)equip", Integer.parseInt(fields[13]));
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("armor.txt");
  }
}
