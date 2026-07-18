import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static Map<String, String> parse(String json) {
        Map<String, String> result = new HashMap<>();
        String content = json.trim();

        if (content.startsWith("{")) {
            content = content.substring(1);
        }
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }

        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length == 2) {
                String key = clean(parts[0]);
                String value = clean(parts[1]);
                result.put(key, value);
            }
        }

        return result;
    }

    private static String clean(String value) {
        return value.trim().replace("\"", "");
    }
}