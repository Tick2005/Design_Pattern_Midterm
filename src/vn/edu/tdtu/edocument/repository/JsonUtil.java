package vn.edu.tdtu.edocument.repository;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bộ đọc/ghi JSON phẳng tối giản (không phụ thuộc thư viện ngoài). */
final class JsonUtil {
    private JsonUtil() { }

    static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    static Map<String, String> parseFlat(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = json.indexOf('{') + 1;
        while (i < json.length()) {
            while (i < json.length() && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
            if (i >= json.length() || json.charAt(i) == '}') break;
            int[] pos = {i};
            String key = readString(json, pos);
            i = pos[0];
            while (json.charAt(i) == ':' || Character.isWhitespace(json.charAt(i))) i++;
            if (json.charAt(i) == '"') {
                pos[0] = i;
                map.put(key, readString(json, pos));
                i = pos[0];
            } else {
                int end = i;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
                String raw = json.substring(i, end).trim();
                map.put(key, "null".equals(raw) ? null : raw);
                i = end;
            }
        }
        return map;
    }

    /** Đọc chuỗi bắt đầu tại pos[0] (đang là dấu "), cập nhật pos[0] sau dấu " đóng. */
    private static String readString(String s, int[] pos) {
        StringBuilder sb = new StringBuilder();
        int i = pos[0] + 1;
        while (s.charAt(i) != '"') {
            char c = s.charAt(i++);
            if (c == '\\') {
                char n = s.charAt(i++);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        pos[0] = i + 1;
        return sb.toString();
    }
}
