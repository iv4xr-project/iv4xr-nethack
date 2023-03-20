package util.JSONConverters;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class GemsConverter extends JSONConverter {
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
      jsonObject.put("Description", fields[1]);
      jsonObject.put("Minimum level", Integer.parseInt(fields[2]));
      jsonObject.put("Cost", Integer.parseInt(fields[3]));
      jsonObject.put("Weight", Integer.parseInt(fields[4]));
      jsonObject.put("Hardness", fields[5]);
      jsonObject.put("Prob (‰)", Integer.parseInt(fields[6]));
      jsonObject.put("Material", fields[7]);
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  @Override
  void convert() {
    convertFile("gems.txt");
  }
}
