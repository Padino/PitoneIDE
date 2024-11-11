// src/SimpleTextEditor.java
import lang.Language;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.BasicCompletion;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SimpleTextEditor extends JFrame {
    private static String language = "English";
    private JComboBox<String> languageComboBox;
    private RSyntaxTextArea textArea;
    private JButton saveButton;
    private JButton openButton;
    private JButton runButton;
    private JLabel statusBar;
    private Process process;
    private BufferedWriter processInputWriter;
    private Process electronProcess;

    // Settings fields
    private boolean showCopilot = true;
    private boolean darkMode = false;
    private boolean showLineNumbers = true; // Default to on
    private Dimension defaultEditorSize = new Dimension(1024, 768);
    private int defaultTextSize = 12;
    private Color defaultColor = Color.BLACK;
    private String defaultFont = "Arial";

    private List<Language> languages = new ArrayList<>();

    public SimpleTextEditor() {
        // Set the system property to use the screen menu bar on macOS
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        setTitle("Pitone");
        setSize(defaultEditorSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load and set the icon image
        try {
            Image icon = ImageIO.read(new File("img/logo.jpeg"));
            setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setFont(new Font(defaultFont, Font.PLAIN, defaultTextSize));
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(showLineNumbers);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateStatusBar();
            }

            public void removeUpdate(DocumentEvent e) {
                updateStatusBar();
            }

            public void changedUpdate(DocumentEvent e) {
                updateStatusBar();
            }
        });

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Save Button
        saveButton = new JButton(LanguageLoader.getText("saveButton"));
        saveButton.addActionListener(new SaveAction());
        controlsPanel.add(saveButton);

        // Open Button
        openButton = new JButton(LanguageLoader.getText("openButton"));
        openButton.addActionListener(new OpenAction());
        controlsPanel.add(openButton);

        // Run Button
        runButton = new JButton(LanguageLoader.getText("runButton"));
        runButton.addActionListener(new RunAction());
        controlsPanel.add(runButton);

        add(controlsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Copilot Button
        JButton copilotButton = new JButton(LanguageLoader.getText("copilotButton"));
        copilotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        // Check if the electron-app directory exists
                        File electronAppDir = new File(System.getProperty("user.dir"), "electron-app");
                        if (!electronAppDir.exists()) {
                            throw new FileNotFoundException("Electron app directory not found: " + electronAppDir.getAbsolutePath());
                        }

                        // Ensure npm dependencies are installed
                        ProcessBuilder npmInstallProcessBuilder = new ProcessBuilder("npm", "install");
                        npmInstallProcessBuilder.directory(electronAppDir);
                        npmInstallProcessBuilder.redirectErrorStream(true);
                        Process npmInstallProcess = npmInstallProcessBuilder.start();
                        npmInstallProcess.waitFor();

                        // Determine the operating system
                        String os = System.getProperty("os.name").toLowerCase();
                        String[] command;
                        if (os.contains("win")) {
                            command = new String[]{"cmd.exe", "/c", "npm start"};
                        } else {
                            command = new String[]{"/bin/bash", "-c", "npm start"};
                        }

                        // Run the Electron app
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.directory(electronAppDir);
                        processBuilder.redirectErrorStream(true);
                        Process electronProcess = processBuilder.start();

                        // Read and print the output from the process
                        BufferedReader reader = new BufferedReader(new InputStreamReader(electronProcess.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        controlsPanel.add(copilotButton);

        // Language ComboBox
        languageComboBox = new JComboBox<>();
        loadLanguages();
        languageComboBox.addActionListener(e -> updateLanguageSettings());
        controlsPanel.add(new JLabel(LanguageLoader.getText("Language") + ":"));
        controlsPanel.add(languageComboBox);

        // Status Bar
        statusBar = new JLabel(LanguageLoader.getText("statusBar"));
        add(statusBar, BorderLayout.SOUTH);

        // Zoom Slider
        JSlider zoomSlider = new JSlider(50, 200, 100); // Min 50%, Max 200%, Initial 100%
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(e -> {
            int zoomPercent = zoomSlider.getValue();
            float zoomFactor = zoomPercent / 100.0f;
            textArea.setFont(textArea.getFont().deriveFont(defaultTextSize * zoomFactor));
        });
        controlsPanel.add(new JLabel(LanguageLoader.getText("zoom") + ":"));
        controlsPanel.add(zoomSlider);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu(LanguageLoader.getText("settings"));

        // Show Line Numbers setting
        JCheckBoxMenuItem lineNumbersItem = new JCheckBoxMenuItem(LanguageLoader.getText("showLineNumbers"), showLineNumbers);
        lineNumbersItem.addActionListener(e -> {
            showLineNumbers = lineNumbersItem.isSelected();
            scrollPane.setLineNumbersEnabled(showLineNumbers);
        });
        settingsMenu.add(lineNumbersItem);

        // Dark Mode setting
        JCheckBoxMenuItem darkModeItem = new JCheckBoxMenuItem(LanguageLoader.getText("darkMode"), darkMode);
        darkModeItem.addActionListener(e -> {
            darkMode = darkModeItem.isSelected();
            applyDarkMode();
        });
        settingsMenu.add(darkModeItem);

        // Show Copilot setting
        JCheckBoxMenuItem showCopilotItem = new JCheckBoxMenuItem(LanguageLoader.getText("showCopilot"), showCopilot);
        showCopilotItem.addActionListener(e -> {
            showCopilot = showCopilotItem.isSelected();
            copilotButton.setVisible(showCopilot);
        });
        settingsMenu.add(showCopilotItem);

        // New File Menu Item
        JMenuItem newFileMenuItem = new JMenuItem(LanguageLoader.getText("newFile"));
        newFileMenuItem.addActionListener(new NewFileAction());
        settingsMenu.add(newFileMenuItem);

        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        applyDarkMode();
        setupAutoCompletion();
    }

    private void loadLanguages() {
        ServiceLoader<Language> loader = ServiceLoader.load(Language.class);
        for (Language lang : loader) {
            languages.add(lang);
            languageComboBox.addItem(lang.getName());
        }
    }

    private void setupAutoCompletion() {
        CompletionProvider provider = createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(textArea);
    }

    private CompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        String selectedLanguage = (String) languageComboBox.getSelectedItem();
        for (Language lang : languages) {
            if (lang.getName().equals(selectedLanguage)) {
                return lang.getCompletionProvider();
            }
        }
        return provider;
    }

    private void updateStatusBar() {
        String text = textArea.getText();
        int wordCount = text.split("\\s+").length;
        wordCount = wordCount == 1 && text.isEmpty() ? 0 : wordCount;
        int lineCount = textArea.getLineCount();
        String statusText = String.format("Words: %d, Lines: %d", wordCount, lineCount);
        statusBar.setText(statusText);
    }

    private void applyDarkMode() {
        if (darkMode) {
            getContentPane().setBackground(new Color(30, 30, 30)); // Darker dark gray
            textArea.setBackground(new Color(50, 50, 50)); // Lighter dark gray
            textArea.setForeground(Color.WHITE);
        } else {
            getContentPane().setBackground(Color.LIGHT_GRAY);
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
        }
    }

    private void updateLanguageSettings() {
        String selectedLanguage = (String) languageComboBox.getSelectedItem();
        for (Language lang : languages) {
            if (lang.getName().equals(selectedLanguage)) {
                textArea.setSyntaxEditingStyle(lang.getSyntaxStyle());
                textArea.setForeground(Color.BLUE); // Set a default color, you can customize it per language
                break;
            }
        }
    }

    private class SaveAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            String selectedLanguage = (String) languageComboBox.getSelectedItem();
            String fileExtension = getFileExtension(selectedLanguage);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(selectedLanguage + " Files", fileExtension.substring(1)));
            int option = fileChooser.showSaveDialog(SimpleTextEditor.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(fileExtension)) {
                    filePath += fileExtension; // Add the appropriate extension if not provided
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    textArea.write(writer);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SimpleTextEditor.this, LanguageLoader.getText("errorSavingFile"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private String getFileExtension(String language) {
            for (Language lang : languages) {
                if (lang.getName().equals(language)) {
                    return lang.getFileExtension();
                }
            }
            return "";
        }
    }

    private class OpenAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Python Files", "py"));
            int option = fileChooser.showOpenDialog(SimpleTextEditor.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    textArea.read(new java.io.FileReader(filePath), null);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SimpleTextEditor.this, LanguageLoader.getText("errorOpeningFile"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class NewFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.setText("");
        }
    }

    private class RunAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String code = textArea.getText();
            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String selectedLanguage = (String) languageComboBox.getSelectedItem();
                        for (Language lang : languages) {
                            if (lang.getName().equals(selectedLanguage)) {
                                String os = System.getProperty("os.name").toLowerCase();
                                File tempFile = File.createTempFile("temp", lang.getFileExtension());
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                                    writer.write(code);
                                }
                                String[] command = lang.getRunCommand(os, tempFile);
                                ProcessBuilder processBuilder = new ProcessBuilder(command);
                                processBuilder.redirectErrorStream(true);
                                Process process = processBuilder.start();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }
    }

    public static void updateLanguage(String newLanguage) {
        language = newLanguage;
        LanguageLoader.loadLanguage(language);
        // Update UI components with the new language
        // Update text to the selected language
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LanguageLoader.loadLanguage(language);
            PitoneMenu menu = new PitoneMenu();
            menu.setVisible(true);
        });
    }
}