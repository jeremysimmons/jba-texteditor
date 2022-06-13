package editor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Actions {
    public static final String OPEN_FILE = "Open File";
    public static final String SAVE_FILE = "Save File";
    public static final String START_SEARCH = "Start Search";
    public static final String PREVIOUS_MATCH = "Previous Match";
    public static final String NEXT_MATCH = "Next Match";
    public static final String TOGGLE_REGEX = "Toggle Regex";
}
class Match {
    private int startIndex;
    private int endIndex;

    public Match(int startIndex, int endIndex) {

        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}

public class TextEditor extends JFrame {
    private JCheckBoxMenuItem useRegexMenuItem;
    JTextArea textArea;
    private ToolbarControl toolbarControl;
    private JFileChooser chooser;
    private java.util.List<Match> matches = new ArrayList<Match>();
    private int matchIndex;

    public TextEditor() {
        setTitle("Text Editor");
        var cwd = System.getProperty("user.dir");
        chooser = new JFileChooser(cwd);
        chooser.setName("FileChooser");
        chooser.setVisible(false);
        this.add(chooser);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        textArea = new JTextArea(200, 200);
        textArea.setName("TextArea");
        JScrollPane scrollableTextArea = new JScrollPane(textArea);
        scrollableTextArea.setName("ScrollPane");
        scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        BorderFactory.setMargin(scrollableTextArea, 10, 10, 10, 10);
        add(scrollableTextArea, BorderLayout.CENTER);
        toolbarControl = new ToolbarControl();
        toolbarControl.setListener((ActionEvent actionEvent) -> {
            if (actionEvent == null) return;
            var command = actionEvent.getActionCommand();
            if (actionEvent instanceof ToggleRegexActionEvent) {
                var toggle = (ToggleRegexActionEvent) actionEvent;
                switch (command) {
                    case Actions.TOGGLE_REGEX:
                        boolean selected = toggle.getUseRegularExpressions();
                        OnUseRegexClicked(selected);
                        break;
                }
            } else if (actionEvent instanceof SearchActionEvent) {
                var searchAction = (SearchActionEvent) actionEvent;
                switch (command) {
                    case Actions.START_SEARCH:
                        startSearch(searchAction.getSearchTerm(), searchAction.getUseRegularExpressions());
                        break;
                    case Actions.NEXT_MATCH:
                        nextMatch();
                        break;
                    case Actions.PREVIOUS_MATCH:
                        previousMatch(searchAction.getSearchTerm(), searchAction.getUseRegularExpressions());
                        break;
                }
            } else {
                switch (command) {
                    case Actions.OPEN_FILE:
                        this.openFileButtonAction();
                        break;
                    case Actions.SAVE_FILE:
                        this.saveFileButtonAction();
                        break;
                }
            }
        });

        BorderFactory.setMargin(toolbarControl, 5, 0, 0, 5);
        add(toolbarControl, BorderLayout.NORTH);


        var fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        var loadMenuItem = new JMenuItem("Open");
        loadMenuItem.setName("MenuOpen");
        loadMenuItem.addActionListener(actionEvent -> OnMenuLoadClicked());
        var saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        saveMenuItem.addActionListener(actionEvent -> OnMenuSaveClicked());
        var exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        exitMenuItem.addActionListener(actionEvent -> OnMenuExitClicked());
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(exitMenuItem);

        var searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        var startSearchMenuItem = new JMenuItem("Start Search");
        startSearchMenuItem.setName("MenuStartSearch");
        startSearchMenuItem.addActionListener(actionEvent -> OnMenuStartSearchClicked());
        var previousMatchMenuItem = new JMenuItem("Previous Match");
        previousMatchMenuItem.setName("MenuPreviousMatch");
        previousMatchMenuItem.addActionListener(actionEvent -> OnMenuPreviousMatchClicked());
        var nextMatchMenuItem = new JMenuItem("Next Match");
        nextMatchMenuItem.setName("MenuNextMatch");
        nextMatchMenuItem.addActionListener(actionEvent -> OnMenuNextMatchClicked());
        useRegexMenuItem = new JCheckBoxMenuItem("Use regex");
        useRegexMenuItem.setName("MenuUseRegExp");
        useRegexMenuItem.addActionListener(actionEvent -> OnUseRegexClicked(useRegexMenuItem.isSelected()));

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(previousMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(useRegexMenuItem);

        var mainMenu = new JMenuBar();
        mainMenu.add(fileMenu);
        mainMenu.add(searchMenu);
        setJMenuBar(mainMenu);
        setVisible(true);
    }

    private void OnMenuLoadClicked() {
        openFileButtonAction();
    }

    private void OnMenuSaveClicked() {
        saveFileButtonAction();
    }

    private void OnMenuExitClicked() {
        dispose();
        System.exit(0);
    }

    private void OnMenuStartSearchClicked() {
        startSearch(toolbarControl.getSearchText(), toolbarControl.getUseRegularExpressions());
    }

    private void OnMenuPreviousMatchClicked() {
        previousMatch(toolbarControl.getSearchText(), toolbarControl.getUseRegularExpressions());
    }

    private void OnMenuNextMatchClicked() {
        nextMatch();
    }

    private void OnUseRegexClicked(boolean selected) {
        toolbarControl.setUseRegularExpressions(selected);
        useRegexMenuItem.setSelected(selected);
    }

    public void openFileButtonAction() {
        chooser.setVisible(true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            loadTextFromFile(chooser.getSelectedFile());
        }
        chooser.setVisible(false);
    }

    void loadTextFromFile(File file) {
        Path path = Paths.get(file.getPath());
        try {
            String text = new String(Files.readAllBytes(path));
            textArea.setText(text);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void saveFileButtonAction() {
        saveTextToFile();
    }

    void saveTextToFile() {
        chooser.setVisible(true);
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
            var file = chooser.getSelectedFile();
            Path path = Paths.get(file.getPath());
            try {
                Files.write(path, textArea.getText().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        chooser.setVisible(false);
    }

    private void startSearch(String searchTerm, boolean useRegularExpressions) {
        matches.clear();
        var text = textArea.getText();
        if(useRegularExpressions) {
            Pattern stringPattern = Pattern.compile(searchTerm);
            Matcher m = stringPattern.matcher(text);
            while(m.find()) {
                matches.add(new Match(m.start(), m.end()));
            }
        } else {
            int index = 0;
            while((index = text.indexOf(searchTerm, index)) != -1)
            {
                matches.add(new Match(index, index + searchTerm.length()));
                index = index + searchTerm.length();
            }
        }
        matchIndex = 0;
        displayCurrentMatch();
    }

    private void displayCurrentMatch() {
        if(matches.isEmpty()) return;
        if(matches.size() > matchIndex) {
            var match = matches.get(matchIndex);
            textArea.setCaretPosition(match.getStartIndex());
            textArea.select(match.getStartIndex(), match.getEndIndex());
            textArea.grabFocus();
        }
    }

    private void nextMatch() {
        matchIndex++;
        if(matchIndex >= matches.size()) {
            matchIndex = 0;
        }
        displayCurrentMatch();
    }

    private void previousMatch(String searchTerm, boolean useRegularExpressions) {
        matchIndex--;
        if(!matches.isEmpty() && matchIndex < 0) {
            matchIndex = matches.size() - 1;
        }
        displayCurrentMatch();
    }

}

class BorderFactory {
    public static void setMargin(JComponent component, int top, int right, int bottom, int left) {
        Border border = component.getBorder();
        Border marginBorder = new EmptyBorder(new Insets(top, left, bottom, right));
        component.setBorder(border == null ? marginBorder : new CompoundBorder(marginBorder, border));
    }
}

class ToggleRegexActionEvent extends ActionEvent {
    private boolean useRegularExpressions;

    public ToggleRegexActionEvent(Object source, int id, String command, boolean useRegularExpressions) {
        super(source, id, command);
        this.useRegularExpressions = useRegularExpressions;
    }

    public boolean getUseRegularExpressions() {
        return useRegularExpressions;
    }
}

class SearchActionEvent extends ActionEvent {

    private String searchTerm;
    private boolean useRegularExpressions;

    public SearchActionEvent(Object source, int id, String command, String searchTerm, boolean useRegularExpressions) {
        super(source, id, command);
        this.searchTerm = searchTerm;
        this.useRegularExpressions = useRegularExpressions;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public boolean getUseRegularExpressions() {
        return useRegularExpressions;
    }
}

class ToolbarControl extends JPanel {

    private final JCheckBox useRegularExpCheckbox;
    JTextField searchField;

    private ActionListener listener;

    ToolbarControl() {

        setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setColumns(30);

        useRegularExpCheckbox = new JCheckBox();
        useRegularExpCheckbox.setName("UseRegExCheckbox");
        useRegularExpCheckbox.setText("Use Regular Expressions");

        JButton openButton = new JButton();
        openButton.setName("OpenButton");
        openButton.setIcon(new ImageIcon("/Users/admin/Downloads/toolbarButtonGraphics/general/Open24.gif"));
        openButton.addActionListener(actionEvent -> {
            if (listener == null) return;
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.OPEN_FILE));
        });

        JButton saveButton = new JButton();
        saveButton.setName("SaveButton");
        saveButton.setIcon(new ImageIcon("/Users/admin/Downloads/toolbarButtonGraphics/general/Save24.gif"));
        saveButton.addActionListener(actionEvent -> {
            if (listener == null) return;
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.SAVE_FILE));
        });


        JButton startSearchButton = new JButton();
        startSearchButton.setName("StartSearchButton");
        startSearchButton.setIcon(new ImageIcon("/Users/admin/Downloads/toolbarButtonGraphics/general/Search24.gif"));
        startSearchButton.addActionListener(actionEvent -> {
            if (listener == null) return;
            listener.actionPerformed(new SearchActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.START_SEARCH, searchField.getText(), useRegularExpCheckbox.isSelected()));
        });

        JButton previousMatchButton = new JButton();
        previousMatchButton.setName("PreviousMatchButton");
        previousMatchButton.setIcon(new ImageIcon("/Users/admin/Downloads/toolbarButtonGraphics/navigation/Back24.gif"));
        previousMatchButton.addActionListener(actionEvent -> {
            if (listener == null) return;
            listener.actionPerformed(new SearchActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.PREVIOUS_MATCH, searchField.getText(), useRegularExpCheckbox.isSelected()));
        });

        JButton nextMatchButton = new JButton();
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.setIcon(new ImageIcon("/Users/admin/Downloads/toolbarButtonGraphics/navigation/Forward24.gif"));
        nextMatchButton.addActionListener(actionEvent -> {
            if (listener == null) return;
            listener.actionPerformed(new SearchActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.NEXT_MATCH, searchField.getText(), useRegularExpCheckbox.isSelected()));
        });

        useRegularExpCheckbox.addActionListener(actionEvent -> {
            listener.actionPerformed(new ToggleRegexActionEvent(this, ActionEvent.ACTION_PERFORMED, Actions.TOGGLE_REGEX, useRegularExpCheckbox.isSelected()));
        });

        this.add(saveButton);
        this.add(openButton);
        this.add(searchField);

        this.add(startSearchButton);
        this.add(previousMatchButton);
        this.add(nextMatchButton);
        this.add(useRegularExpCheckbox);
    }

    public ActionListener getListener() {
        return listener;
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    public void setUseRegularExpressions(boolean use) {
        this.useRegularExpCheckbox.setSelected(use);
    }

    public boolean getUseRegularExpressions() {
        return this.useRegularExpCheckbox.isSelected();
    }

    public String getSearchText() {
        return this.searchField.getText();
    }
}
