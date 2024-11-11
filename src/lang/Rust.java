package lang;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class Rust implements Language {
    private String selectedLanguage;

    public Rust() {
        this.selectedLanguage = Locale.getDefault().getLanguage(); // Default to system language
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    @Override
    public String getName() {
        return "Rust";
    }

    @Override
    public String getFileExtension() {
        return ".rs";
    }

    @Override
    public String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_RUST;
    }

    @Override
    public CompletionProvider getCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.addCompletion(new BasicCompletion(provider, "fn"));
        provider.addCompletion(new BasicCompletion(provider, "let"));
        provider.addCompletion(new BasicCompletion(provider, "mut"));
        provider.addCompletion(new BasicCompletion(provider, "if"));
        provider.addCompletion(new BasicCompletion(provider, "else"));
        provider.addCompletion(new BasicCompletion(provider, "while"));
        provider.addCompletion(new BasicCompletion(provider, "for"));
        provider.addCompletion(new BasicCompletion(provider, "return"));
        return provider;
    }

    @Override
    public String[] getRunCommand(String os, File tempFile) {
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        checkRustInstallation();
        String rustFileName = tempFile.getAbsolutePath().replace(".rs", "");
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", "start", "cmd.exe", "/k", "rustc " + tempFile.getAbsolutePath() + " && " + rustFileName};
        } else if (os.contains("mac")) {
            return new String[]{"/bin/bash", "-c", "osascript -e 'tell application \"Terminal\" to do script \"rustc " + tempFile.getAbsolutePath() + " && " + rustFileName + "\"'"};
        } else {
            return new String[]{"/bin/bash", "-c", "x-terminal-emulator -e rustc " + tempFile.getAbsolutePath() + " && " + rustFileName};
        }
    }

    private void checkRustInstallation() {
        try {
            Process process = new ProcessBuilder("rustc", "--version").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            process.waitFor();
            if (output == null || !output.contains("rustc")) {
                showRustNotInstalledWarning();
            }
        } catch (IOException | InterruptedException e) {
            showRustNotInstalledWarning();
        }
    }

    private void showRustNotInstalledWarning() {
        String os = System.getProperty("os.name").toLowerCase();
        String downloadLink = "https://www.rust-lang.org/tools/install";
        String message;
        String title;

        if (selectedLanguage.equals("it")) {
            message = "<html>Rust non è installato sul tuo sistema.<br>Per favore scarica e installa Rust da: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Rust non installato";
        } else {
            message = "<html>Rust is not installed on your system.<br>Please download and install Rust from: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Rust Not Installed";
        }

        JLabel label = new JLabel(message);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new URI(downloadLink));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        JOptionPane.showMessageDialog(null, label, title, JOptionPane.WARNING_MESSAGE);
    }
}