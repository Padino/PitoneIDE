package lang;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class Python implements Language {
    @Override
    public String getName() {
        return "Python";
    }

    @Override
    public String getFileExtension() {
        return ".py";
    }

    @Override
    public String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_PYTHON;
    }



    @Override
    public String[] getRunCommand(String os, File tempFile) {
        checkPythonInstallation();
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", "start", "cmd.exe", "/k", "python " + tempFile.getAbsolutePath()};
        } else if (os.contains("mac")) {
            return new String[]{"/bin/bash", "-c", "osascript -e 'tell application \"Terminal\" to do script \"python3 " + tempFile.getAbsolutePath() + "\"'"};
        } else {
            return new String[]{"/bin/bash", "-c", "x-terminal-emulator -e python3 " + tempFile.getAbsolutePath()};
        }
    }

    private void checkPythonInstallation() {
        try {
            Process process = new ProcessBuilder("python3", "--version").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                showPythonNotInstalledWarning();
            }
        } catch (IOException | InterruptedException e) {
            showPythonNotInstalledWarning();
        }
    }

    private void showPythonNotInstalledWarning() {
        String os = System.getProperty("os.name").toLowerCase();
        String downloadLink = "https://www.python.org/downloads/";
        String message;
        String title;

        if (Locale.getDefault().getLanguage().equals("it")) {
            message = "<html>Python non è installato sul tuo sistema.<br>Per favore scarica e installa Python da: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Python non installato";
        } else {
            message = "<html>Python is not installed on your system.<br>Please download and install Python from: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "Python Not Installed";
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