package lang;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class Java implements Language {
    @Override
    public String getName() {
        return "Java";
    }

    @Override
    public String getFileExtension() {
        return ".java";
    }

    @Override
    public String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_JAVA;
    }

    @Override
    public CompletionProvider getCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.addCompletion(new BasicCompletion(provider, "public"));
        provider.addCompletion(new BasicCompletion(provider, "class"));
        provider.addCompletion(new BasicCompletion(provider, "void"));
        provider.addCompletion(new BasicCompletion(provider, "main"));
        return provider;
    }

    @Override
    public String[] getRunCommand(String os, File tempFile) {
        checkJavaInstallation();
        String javaFileName = tempFile.getAbsolutePath().replace(".java", "");
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", "start", "cmd.exe", "/k", "javac " + tempFile.getAbsolutePath() + " && java " + javaFileName};
        } else if (os.contains("mac")) {
            return new String[]{"/bin/bash", "-c", "osascript -e 'tell application \"Terminal\" to do script \"javac " + tempFile.getAbsolutePath() + " && java " + javaFileName + "\"'"};
        } else {
            return new String[]{"/bin/bash", "-c", "x-terminal-emulator -e javac " + tempFile.getAbsolutePath() + " && java " + javaFileName};
        }
    }

    private void checkJavaInstallation() {
        try {
            Process process = new ProcessBuilder("java", "-version").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                showJavaNotInstalledWarning();
            }
        } catch (IOException | InterruptedException e) {
            showJavaNotInstalledWarning();
        }
    }

    private void showJavaNotInstalledWarning() {
        String os = System.getProperty("os.name").toLowerCase();
        String downloadLink = "https://www.java.com/en/download/";
        String message;
        String title;

        if (Locale.getDefault().getLanguage().equals("it")) {
            message = "<html>Java non è installato sul tuo sistema.<br>Per favore scarica e installa Java da: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Java non installato";
        } else {
            message = "<html>Java is not installed on your system.<br>Please download and install Java from: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Java Not Installed";
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