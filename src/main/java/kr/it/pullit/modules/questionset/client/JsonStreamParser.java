package kr.it.pullit.modules.questionset.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonStreamParser {

  private int depth = 0;
  private boolean inString = false;
  private boolean isEscape = false;
  private int objStartIndex = 0;
  private int objCurIndex = 0;
  private StringBuilder jsonObjStream = new StringBuilder();

  public void reset() {
    depth = 0;
    inString = false;
    isEscape = false;
    objStartIndex = 0;
    objCurIndex = 0;
    jsonObjStream = new StringBuilder();
  }

  public Optional<String> getJsonObjStream() {
    return Optional.of(jsonObjStream.toString());
  }

  public List<String> findCompleteJsonObject(String streamStr) {
    List<String> jsonObjects = new ArrayList<>();
    if (streamStr == null) {
      return jsonObjects;
    }

    jsonObjStream.append(streamStr);

    String jsonStreamStr = jsonObjStream.toString();
    int strLen = jsonStreamStr.length();

    for (; objCurIndex < strLen; objCurIndex++) {
      char strChar = jsonStreamStr.charAt(objCurIndex);
      if (inString) {
        if (isEscape) {
          isEscape = false;
        } else if (strChar == '\\') {
          isEscape = true;
        } else if (strChar == '"') {
          inString = false;
        }
        continue;
      }

      if (strChar == '"') {
        inString = true;
        continue;
      }

      if (strChar == '{') {
        depth++;
        if (depth == 1) {
          objStartIndex = objCurIndex;
        }
        continue;
      }

      if (strChar == '}') {
        depth--;
        if (depth == 0) {
          jsonObjects.add(jsonStreamStr.substring(objStartIndex, objCurIndex + 1));
        }
        continue;
      }
    }

    return jsonObjects;
  }
}
