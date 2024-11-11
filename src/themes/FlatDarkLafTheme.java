package themes;

import com.formdev.flatlaf.FlatDarkLaf;

public class FlatDarkLafTheme extends FlatDarkLaf {
    public static boolean setup() {
        return FlatDarkLaf.setup(new FlatDarkLafTheme());
    }

    @Override
    public String getName() {
        return "FlatDarkLaf";
    }
}