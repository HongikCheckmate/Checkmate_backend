package project.project1.util;

import java.util.HashMap;
import java.util.Map;

public class QueryParamParser {

    // 예: "name:홍익,nickname:베포"
    public static Map<String, String> parseQuery(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }

        String[] pairs = raw.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2); // value 안에 ":" 들어갈 수도 있으니 limit=2
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }

}
