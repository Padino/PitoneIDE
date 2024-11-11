import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LanguageLoader {
    private static final String TRAD_FOLDER = "trad";
    private static final String ENGLISH_FILE = "eng.txt";
    private static final String ITALIAN_FILE = "it.txt";
    private static Map<String, String> texts = new HashMap<>();

    public static void loadLanguage(String language) {
        String fileName = language.equals("Italian") ? ITALIAN_FILE : ENGLISH_FILE;
        File file = new File(TRAD_FOLDER, fileName);
        texts.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    texts.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getText(String key) {
        return texts.getOrDefault(key, key);
    }
}