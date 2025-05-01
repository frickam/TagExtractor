import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class TagExtractor extends JFrame {
    private JTextField fileTextField;
    private JTextField stopWordsTextField;
    private JTextArea resultTextArea;
    private Map<String, Integer> wordFrequencyMap;

    public TagExtractor() {
        setTitle("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        // File selection panel
        JPanel filePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        filePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fileTextField = new JTextField();
        stopWordsTextField = new JTextField();
        fileTextField.setEditable(false);
        stopWordsTextField.setEditable(false);
        JButton selectFileButton = new JButton("Select Text File");
        JButton selectStopWordsButton = new JButton("Select Stop Words File");
        JButton extractTagsButton = new JButton("Extract Tags");

        selectFileButton.addActionListener(e -> selectFile(fileTextField));
        selectStopWordsButton.addActionListener(e -> selectFile(stopWordsTextField));
        extractTagsButton.addActionListener(e -> extractTags());

        filePanel.add(createLabeledPanel("Text File:", fileTextField, selectFileButton));
        filePanel.add(createLabeledPanel("Stop Words File:", stopWordsTextField, selectStopWordsButton));
        filePanel.add(extractTagsButton);

        // Result display area
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        // Save button
        JButton saveButton = new JButton("Save Results");
        saveButton.addActionListener(e -> saveResults());

        add(filePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    private JPanel createLabeledPanel(String labelText, JTextField textField, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void selectFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void extractTags() {
        String filePath = fileTextField.getText();
        String stopWordsPath = stopWordsTextField.getText();

        if (filePath.isEmpty() || stopWordsPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both the text file and the stop words file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            Set<String> stopWords = loadStopWords(stopWordsPath);
            wordFrequencyMap = new HashMap<>();

            for (String line : lines) {
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }

            displayResults(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<String> loadStopWords(String stopWordsPath) throws IOException {
        List<String> stopWordsList = Files.readAllLines(Paths.get(stopWordsPath));
        return new HashSet<>(stopWordsList);
    }

    private void displayResults(String fileName) {
        resultTextArea.setText("Tags extracted from file: " + fileName + "\n\n");
        wordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> resultTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n"));
    }

    private void saveResults() {
        if (wordFrequencyMap == null || wordFrequencyMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to save. Extract tags first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
                for (Map.Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue());
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(this, "Results saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving results: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractor tagExtractor = new TagExtractor();
            tagExtractor.setVisible(true);
        });
    }
}