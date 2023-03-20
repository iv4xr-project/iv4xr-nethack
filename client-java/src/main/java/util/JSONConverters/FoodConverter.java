package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class FoodConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      line = line.replace("*", "");
      String[] fields = line.split("\t");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Item", fields[0]);
      jsonObject.put("Cost", Integer.parseInt(fields[1]));
      if (!fields[2].equals("varies")) {
        jsonObject.put("Wgt", Integer.parseInt(fields[2]));
      }
      if (!fields[3].equals("—")) {
        jsonObject.put("Prob", Double.parseDouble(fields[3]));
      }
      if (!fields[4].equals("varies")) {
        String[] vals = fields[4].split("–");
        double nutrition;
        if (vals.length == 1) {
          nutrition = Double.parseDouble(vals[0]);
        } else {
          nutrition = (Double.parseDouble(vals[0]) + Double.parseDouble(vals[1])) / 2;
        }
        jsonObject.put("Nutr", nutrition);
      }
      if (!fields[5].equals("varies")) {
        jsonObject.put("Nutr/Wgt", Double.parseDouble(fields[5]));
      }
      if (!fields[6].equals("varies") && fields[6].split("–").length == 1) {
        jsonObject.put("Time", Integer.parseInt(fields[6]));
      }
      if (!fields[7].equals("varies")) {
        jsonObject.put("Nutr/Time", Integer.parseInt(fields[4]));
      }
      jsonObject.put("Conduct", fields[8]);
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("foods.txt");
  }
}
