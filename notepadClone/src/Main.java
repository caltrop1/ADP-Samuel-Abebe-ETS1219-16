import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;


public class Main extends Application {

    private TextArea textArea;
    private CodeArea codeArea;

    private File currentFile;
    private boolean isPythonFile = false;

    private static final Set<String> PYTHON_KEYWORDS = Set.of(
            "False", "None", "True", "and", "as",
            "assert", "async", "await", "break", "class",
            "continue", "def", "del", "elif", "else",
            "except", "finally", "for", "from", "global",
            "if", "import", "in", "is", "lambda",
            "nonlocal", "not", "or", "pass", "raise",
            "return", "try", "while", "with", "yield"
    );

    private static final Set<String> PYTHON_BUILTINS = Set.of(
            "print", "len", "range", "type", "int", "str", "float", "bool",
            "list", "dict", "set", "tuple", "input", "open", "super",
            "enumerate", "zip", "map", "filter", "sorted", "reversed",
            "isinstance", "abs", "round",
            "min", "max", "sum", "any", "all", "repr", "id", "dir"
    );
    // regex patterns were checked using regex101.com
    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile(
            "(?<COMMENT>#[^\n]*)" + // the text inside <> will act like the name of this regex and allows the use of matcher to find this regex later
                    "|(?<STRING>\"\"\"[\\s\\S]*?\"\"\"" +
                    "|'''[\\s\\S]*?'''" +
                    "|\"(?:\\\\.|[^\"\\\\\n])*\"" +
                    "|'(?:\\\\.|[^'\\\\\n])*')" +
                    "|(?<DECORATOR>@\\w+)" +
                    "|(?<NUMBER>\\b\\d+\\.?\\d*([eE][+-]?\\d+)?\\b" +
                    "|0x[0-9a-fA-F]+\\b)" +
                    "|(?<KEYWORD>\\b(" + String.join("|", PYTHON_KEYWORDS) + ")\\b)" +
                    "|(?<BUILTIN>\\b(" + String.join("|", PYTHON_BUILTINS) + ")\\b)"
    );
    // since matcher goes from left to right and stop when it finds a match, the order of the regexes matters

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        textArea = new TextArea();
        textArea.setWrapText(true);

        codeArea = new CodeArea();
        codeArea.setWrapText(true);
        codeArea.setVisible(false);

        codeArea.textProperty().addListener((_,_,_) -> {
            highlightPython();
        });
        codeArea.getStylesheets().add(
                getClass().getResource("/editor.css").toExternalForm()
        );

        StackPane editorStack = new StackPane(textArea, codeArea);

        Button openButton = new Button("Open");
        Button saveButton = new Button("Save");

        openButton.setOnAction(e -> openFile(stage));
        saveButton.setOnAction(e -> saveFile(stage));

        HBox toolbar = new HBox(10, openButton, saveButton);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(editorStack);

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.setTitle("Notepad");
        stage.show();
    }

    private void openFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text and Python files", "*.txt", "*.py")
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            loadFile(file);
        }
    }

    private void loadFile(File file) {
        try {
            String text = Files.readString(file.toPath());

            currentFile = file;
            isPythonFile = file.getName().toLowerCase().endsWith(".py");

            if (isPythonFile) {
                textArea.setVisible(false);
                codeArea.setVisible(true);
                codeArea.replaceText(text);
                highlightPython();
            } else {
                codeArea.setVisible(false);
                textArea.setVisible(true);
                textArea.setText(text);
            }

        } catch (IOException e) {
            System.out.println("Failed to open file: " + e.getMessage());
        }
    }

    private void saveFile(Stage stage) {
        if (currentFile == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save File");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text and Python files", "*.txt", "*.py")
            );

            File file = chooser.showSaveDialog(stage);
            if (file == null) return;

            currentFile = file;
        }

        writeFile(currentFile);
    }

    private void writeFile(File file) {
        try {
            String content = isPythonFile
                    ? codeArea.getText()
                    : textArea.getText();

            Files.write(file.toPath(), content.getBytes());

            currentFile = file;
            isPythonFile = file.getName().toLowerCase().endsWith(".py");

        } catch (IOException e) {
            System.out.println("Failed to save file: " + e.getMessage());
        }
    }

    private void highlightPython() {
        String text = codeArea.getText();
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        Matcher matcher = HIGHLIGHT_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            builder.add(Collections.emptyList(), matcher.start() - lastEnd);

            String styleClass;
            if      (matcher.group("COMMENT")   != null) styleClass = "comment";
            else if (matcher.group("STRING")    != null) styleClass = "string";
            else if (matcher.group("DECORATOR") != null) styleClass = "decorator";
            else if (matcher.group("NUMBER")    != null) styleClass = "number";
            else if (matcher.group("KEYWORD")   != null) styleClass = "keyword";
            else                                          styleClass = "builtin";

            builder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }

        builder.add(Collections.emptyList(), text.length() - lastEnd);

        codeArea.setStyleSpans(0, builder.create());
    }
}
