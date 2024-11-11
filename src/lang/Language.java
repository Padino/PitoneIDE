package lang;

import org.fife.ui.autocomplete.CompletionProvider;

import java.io.File;

public interface Language {
    String getName();
    String getFileExtension();
    String getSyntaxStyle();
    CompletionProvider getCompletionProvider();
    String[] getRunCommand(String os, File tempFile);
}