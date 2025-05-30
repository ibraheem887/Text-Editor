package presentation_layer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LemmatizationResultWindow extends JFrame {
    private CustomTable resultTable;
    private DefaultTableModel model;
    private int currentPage = 0;
    private int rowsPerPage = 10; // Set how many rows you want per page
    private int totalRows;
    private List<String[]> results;

    public LemmatizationResultWindow(List<String[]> results) {
        setTitle("Lemmatization Results");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create table with DefaultTableModel
        String[] columns = {"Original Word", "Lemmatized Word"};
        model = new DefaultTableModel(columns, 0);
        resultTable = new CustomTable();
        resultTable.setModel(model);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        this.results = results;
        totalRows = results.size();

        // Load data into the table
        loadLemmatizationData();

        // Pagination controls
        JPanel paginationPanel = new JPanel();
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");

        // Adding action listeners for pagination
        prevButton.addActionListener(e -> showPreviousPage());
        nextButton.addActionListener(e -> showNextPage());

        paginationPanel.add(prevButton);
        paginationPanel.add(nextButton);

        add(paginationPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadLemmatizationData() {
        model.setRowCount(0); // Clear previous data

        // Adding results for the current page
        int startIdx = currentPage * rowsPerPage;
        int endIdx = Math.min(startIdx + rowsPerPage, totalRows);

        for (int i = startIdx; i < endIdx; i++) {
            String[] rowData = results.get(i);
            // File title (first column in the array)
            String originalText = rowData[1]; // Original content (second column in the array)
            String lemmatizedText = rowData[2]; // Lemmatized content (third column in the array)

            // Split sentences into words
            String[] originalWords = originalText.split("\\s+");
            String[] lemmatizedWords = lemmatizedText.split("\\s+");

            // Get the maximum length to ensure all words are displayed
            int maxLength = Math.max(originalWords.length, lemmatizedWords.length);

            for (int j = 0; j < maxLength; j++) {
                String originalWord = (j < originalWords.length) ? originalWords[j] : ""; // Fill with empty if no more words
                String lemmatizedWord = (j < lemmatizedWords.length) ? lemmatizedWords[j] : ""; // Fill with empty if no more words
                model.addRow(new Object[]{originalWord, lemmatizedWord});
            }
        }
    }

    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadLemmatizationData(); // Reload with updated page
        }
    }

    private void showNextPage() {
        if ((currentPage + 1) * rowsPerPage < totalRows) {
            currentPage++;
            loadLemmatizationData(); // Reload with updated page
        }
    }

    // Mock method to get results (replace this with actual data source)
    private List<String[]> loadResultsFromDataSource() {
        // This should fetch the data from your DAO or data source
        // For now, we'll return the results passed to the constructor
        return results;
    }

    // CustomTable class for your JTable (you can extend it if needed for custom behavior)
    private class CustomTable extends JTable {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make cells non-editable
        }
    }

    
}
