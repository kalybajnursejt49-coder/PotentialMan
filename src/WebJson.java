import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebJson {
    private final String text;
    private int position;

    private WebJson(String text) {
        this.text = text;
    }

    public static Object parse(String text) {
        WebJson reader = new WebJson(text);
        Object value = reader.readValue();
        reader.skipSpaces();
        if (reader.position != text.length()) {
            throw new IllegalArgumentException("Некорректный JSON");
        }
        return value;
    }

    public static String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof String) {
            String source = (String) value;
            StringBuilder result = new StringBuilder("\"");
            for (int i = 0; i < source.length(); i++) {
                char c = source.charAt(i);
                if (c == '"' || c == '\\') result.append('\\');
                if (c == '\n') result.append("\\n");
                else if (c == '\r') result.append("\\r");
                else if (c == '\t') result.append("\\t");
                else if (c >= 32) result.append(c);
            }
            return result.append('"').toString();
        }
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            StringBuilder result = new StringBuilder("{");
            for (Object key : map.keySet()) {
                if (result.length() > 1) result.append(',');
                result.append(stringify(key.toString())).append(':').append(stringify(map.get(key)));
            }
            return result.append('}').toString();
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder result = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) result.append(',');
                result.append(stringify(list.get(i)));
            }
            return result.append(']').toString();
        }
        throw new IllegalArgumentException("Неподдерживаемый тип JSON");
    }

    private Object readValue() {
        skipSpaces();
        if (position >= text.length()) throw new IllegalArgumentException("Пустой JSON");
        char c = text.charAt(position);
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (text.startsWith("true", position)) { position += 4; return true; }
        if (text.startsWith("false", position)) { position += 5; return false; }
        if (text.startsWith("null", position)) { position += 4; return null; }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        position++;
        skipSpaces();
        if (take('}')) return result;
        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            require(':');
            result.put(key, readValue());
            skipSpaces();
            if (take('}')) return result;
            require(',');
        }
    }

    private List<Object> readArray() {
        List<Object> result = new ArrayList<>();
        position++;
        skipSpaces();
        if (take(']')) return result;
        while (true) {
            result.add(readValue());
            skipSpaces();
            if (take(']')) return result;
            require(',');
        }
    }

    private String readString() {
        require('"');
        StringBuilder result = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position++);
            if (c == '"') return result.toString();
            if (c == '\\') {
                if (position >= text.length()) break;
                char escaped = text.charAt(position++);
                if (escaped == 'n') result.append('\n');
                else if (escaped == 'r') result.append('\r');
                else if (escaped == 't') result.append('\t');
                else if (escaped == 'u' && position + 4 <= text.length()) {
                    result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                    position += 4;
                } else result.append(escaped);
            } else result.append(c);
        }
        throw new IllegalArgumentException("Незакрытая строка JSON");
    }

    private Number readNumber() {
        int start = position;
        while (position < text.length()) {
            char c = text.charAt(position);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') position++;
            else break;
        }
        if (start == position) throw new IllegalArgumentException("Некорректное число JSON");
        String number = text.substring(start, position);
        try {
            if (number.contains(".") || number.contains("e") || number.contains("E")) return Double.parseDouble(number);
            return Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число JSON");
        }
    }

    private void skipSpaces() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) position++;
    }

    private boolean take(char wanted) {
        if (position < text.length() && text.charAt(position) == wanted) {
            position++;
            return true;
        }
        return false;
    }

    private void require(char wanted) {
        if (!take(wanted)) throw new IllegalArgumentException("Некорректный JSON");
    }
}
