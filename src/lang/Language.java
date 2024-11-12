package lang;


import java.io.File;

public interface Language {
    String getName();
    String getFileExtension();
    String getSyntaxStyle();
    String[] getRunCommand(String os, File tempFile);
}