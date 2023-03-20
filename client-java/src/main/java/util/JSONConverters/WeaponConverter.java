package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeaponConverter extends JSONConverter {
  @Override
  protected JSONArray convertUsingReader(BufferedReader br) throws IOException {
    JSONArray jsonArray = new JSONArray();

    // First line is the header
    String line = br.readLine();
    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      line = line.replace("†", "");
      String[] fields = line.split("\t");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Weapon", fields[0]);
      jsonObject.put("Skill", fields[1]);
      jsonObject.put("Cost", Integer.parseInt(fields[2].replace(" zm", "")));
      jsonObject.put("Hands", Integer.parseInt(fields[3]));
      jsonObject.put("Weight", Integer.parseInt(fields[4]));
      //        jsonObject.put("Prob (‰)", Integer.parseInt(fields[5].replaceAll("[^\\d.]", "")));
      jsonObject.put("Dam. (S)", Double.parseDouble(fields[6]));
      jsonObject.put("Dam. (L)", Double.parseDouble(fields[7]));
      jsonObject.put("Material", fields[8]);
      jsonObject.put("Appearance", fields[9]);
      //        jsonObject.put("Tile", fields[10]);
      jsonObject.put("Glyph", fields[11]);
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("weapons.txt");
  }
}
