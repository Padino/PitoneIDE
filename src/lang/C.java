package lang;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class C implements Language {
    @Override
    public String getName() {
        return "C";
    }

    @Override
    public String getFileExtension() {
        return ".c";
    }

    @Override
    public String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_C;
    }



    @Override
    public String[] getRunCommand(String os, File tempFile) {
        checkCInstallation();
        String cFileName = tempFile.getAbsolutePath().replace(".c", "");
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", "start", "cmd.exe", "/k", "gcc " + tempFile.getAbsolutePath() + " -o " + cFileName + " && " + cFileName};
        } else if (os.contains("mac")) {
            return new String[]{"/bin/bash", "-c", "osascript -e 'tell application \"Terminal\" to do script \"gcc " + tempFile.getAbsolutePath() + " -o " + cFileName + " && " + cFileName + "\"'"};
        } else {
            return new String[]{"/bin/bash", "-c", "x-terminal-emulator -e gcc " + tempFile.getAbsolutePath() + " -o " + cFileName + " && " + cFileName};
        }
    }

    private void checkCInstallation() {
        try {
            Process process = new ProcessBuilder("gcc", "--version").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                showCNotInstalledWarning();
            }
        } catch (IOException | InterruptedException e) {
            showCNotInstalledWarning();
        }
    }

    private void showCNotInstalledWarning() {
        String os = System.getProperty("os.name").toLowerCase();
        String downloadLink = "https://gcc.gnu.org/install/";
        String message;
        String title;

        if (Locale.getDefault().getLanguage().equals("it")) {
            message = "<html>GCC non è installato sul tuo sistema.<br>Per favore scarica e installa GCC da: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "GCC non installato";
        } else {
            message = "<html>GCC is not installed on your system.<br>Please download and install GCC from: <a href='" + downloadLink + "'>" + downloadLink + "</a></html>";
            title = "GCC Not Installed";
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