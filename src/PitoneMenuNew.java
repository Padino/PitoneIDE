import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;

public class PitoneMenuNew extends JFrame {
    private JLabel titleLabel;
    private JButton startButton;
    private static String language = "English"; // Set default language to English
    private JComboBox<String> themeComboBox;
    private List<String> themes = new ArrayList<>();

    public PitoneMenuNew() {
        // Load default language
        LanguageLoader.loadLanguage(language);

        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        setTitle("Pitone Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Add the rounded "i" button
        JButton infoButton = new RoundedButton("i");
        infoButton.addActionListener(e -> openSettingsDialog(this));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoButton, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        titleLabel = new JLabel(LanguageLoader.getText("title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Add padding at the top
        topPanel.add(titleLabel, BorderLayout.CENTER);

        startButton = new JButton(LanguageLoader.getText("startButton"));
        startButton.addActionListener(e -> {
            SimpleTextEditor editor = new SimpleTextEditor();
            editor.setVisible(true);
            dispose(); // Close the Pitone Menu window
        });
        add(startButton, BorderLayout.SOUTH);

        // Load default themes
        loadDefaultThemes();
    }

    private void openSettingsDialog(JFrame parentFrame) {
        JDialog settingsDialog = new JDialog(parentFrame, "Settings", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLayout(new BorderLayout());

        // Appearance section
        JPanel appearancePanel = new JPanel(new GridLayout(3, 2));
        appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));

        // Language dropdown
        JComboBox<String> languageComboBox = new JComboBox<>(new String[]{"English", "Italian"});
        languageComboBox.setSelectedItem(language); // Set default language to English
        languageComboBox.addActionListener(e -> {
            language = (String) languageComboBox.getSelectedItem();
            LanguageLoader.loadLanguage(language);
            updateLanguage();
        });
        appearancePanel.add(new JLabel(LanguageLoader.getText("language") + ":"));
        appearancePanel.add(languageComboBox);

        // Theme dropdown
        themeComboBox = new JComboBox<>(themes.toArray(new String[0]));
        themeComboBox.setSelectedItem("FlatLightLaf");
        themeComboBox.addActionListener(e -> {
            String selectedTheme = (String) themeComboBox.getSelectedItem();
            loadTheme(selectedTheme);
        });
        appearancePanel.add(new JLabel("Theme:"));
        appearancePanel.add(themeComboBox);

        // Add Themes button
        JButton addThemeButton = new JButton("Add Themes");
        addThemeButton.addActionListener(e -> addTheme());
        appearancePanel.add(addThemeButton);

        // Info section
        JPanel infoPanel = new JPanel(new GridLayout(1, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));

        // Credits button
        JButton creditsButton = new JButton(LanguageLoader.getText("credits"));
        creditsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(settingsDialog, LanguageLoader.getText("creditsMessage"));
        });
        infoPanel.add(creditsButton);

        // Add Language button
        JButton addLanguageButton = new JButton(LanguageLoader.getText("LangButt"));
        addLanguageButton.addActionListener(e -> addLanguage());
        infoPanel.add(addLanguageButton);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(LanguageLoader.getText("saveSettings"));
        saveButton.addActionListener(e -> settingsDialog.dispose());
        JButton discardButton = new JButton(LanguageLoader.getText("discardSettings"));
        discardButton.addActionListener(e -> settingsDialog.dispose());
        buttonsPanel.add(saveButton);
        buttonsPanel.add(discardButton);

        // Add panels to settings dialog
        settingsDialog.add(appearancePanel, BorderLayout.NORTH);
        settingsDialog.add(infoPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonsPanel, BorderLayout.SOUTH);

        settingsDialog.setLocationRelativeTo(parentFrame);
        settingsDialog.setVisible(true);
    }

    private void addTheme() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getName().endsWith(".java")) {
                try {
                    // Copy the selected file to the themes folder
                    File destFile = new File("src/themes/" + selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Update the themes list
                    String themeName = selectedFile.getName().replace(".java", "");
                    themes.add(themeName);
                    themeComboBox.addItem(themeName);

                    JOptionPane.showMessageDialog(this,LanguageLoader.getText("ThemeSuc"));
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, LanguageLoader.getText("ThemeFail"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, LanguageLoader.getText("ThemeInvalid"), "Invalid File", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void loadDefaultThemes() {
        File themesFolder = new File("src/themes");
        if (themesFolder.exists() && themesFolder.isDirectory()) {
            File[] themeFiles = themesFolder.listFiles((dir, name) -> name.endsWith("Theme.java"));
            if (themeFiles != null && themeFiles.length > 0) {
                for (File themeFile : themeFiles) {
                    String themeName = themeFile.getName().replace(".java", "");
                    themes.add(themeName);
                }
            }
        }
        // Always add Swing as an option
        themes.add("Swing");

        // Load the default theme
        loadTheme(themes.get(0));
    }

    private void loadTheme(String themeName) {
        try {
            if (themeName.equals("Swing")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                // Load custom theme from the themes package
                Class<?> themeClass = Class.forName("themes." + themeName);
                LookAndFeel theme = (LookAndFeel) themeClass.getDeclaredConstructor().newInstance();
                UIManager.setLookAndFeel(theme);
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLanguage() {
        // Update language in PitoneMenu
        titleLabel.setText(LanguageLoader.getText("title"));
        startButton.setText(LanguageLoader.getText("startButton"));

        // Update language in SimpleTextEditor
        SimpleTextEditor.updateLanguage(language);
    }

    private void addLanguage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getName().endsWith(".java")) {
                try {
                    // Copy the selected file to the lang folder
                    File destFile = new File("src/lang/" + selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Update the lang.Language file
                    try (FileWriter writer = new FileWriter("src/META-INF/services/lang.Language", true)) {
                        writer.write("\nlang." + selectedFile.getName().replace(".java", ""));
                    }

                    JOptionPane.showMessageDialog(this, LanguageLoader.getText("LangSuc"));
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, LanguageLoader.getText("LangFail"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, LanguageLoader.getText("LangInvalid"), "Invalid File", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PitoneMenuNew menu = new PitoneMenuNew();
            menu.setVisible(true);
        });
    }
}