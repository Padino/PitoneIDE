package themes;

import com.formdev.flatlaf.FlatLightLaf;

public class FlatLightLafTheme extends FlatLightLaf {
    public static boolean setup() {
        return FlatLightLaf.setup(new FlatLightLafTheme());
    }

    @Override
    public String getName() {
        return "FlatLightLaf";
    }
}