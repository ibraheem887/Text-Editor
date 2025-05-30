package presentation_layer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import Common.ITextAnalysisFacade;

public class TextEditorPage extends JFrame {
	private static final Logger logger = (Logger) LogManager.getLogger(TextEditorPage.class);

	private static final long serialVersionUID = 1L;
	private static final int LINES_PER_PAGE = 15;

	private JTextArea textArea;
	private String title;
	private JButton enableEditButton;
	private JButton importButton;
	private JButton exportButton;
	private JButton nextPageButton;
	private JButton prevPageButton;
	private JLabel pageLabel;
	private ITextAnalysisFacade taf;
	private String content;
	private int currentPage = 0;
	private String currentFileTitle;

	private HomePage homePage;

	public TextEditorPage(ITextAnalysisFacade taf, String title, String content, HomePage homePage,
			String currentFileTitle) {

		this.title = title;
		this.content = content;
		this.homePage = homePage;
		this.currentFileTitle = currentFileTitle;
		this.taf = taf;

		setTitle("Text Editor - " + title);
		setSize(1300, 1000);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLookAndFeel();

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(new Color(245, 245, 245));

		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(new Color(92, 184, 92));
		JLabel headerLabel = new JLabel("Text Editor - " + title);
		headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
		headerLabel.setForeground(Color.WHITE);
		headerPanel.add(headerLabel);
		mainPanel.add(headerPanel, BorderLayout.NORTH);

		textArea = new JTextArea(content);
		textArea.setFont(new Font("Arial", Font.PLAIN, 16));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBackground(Color.WHITE);
		textArea.setForeground(Color.DARK_GRAY);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel controlPanel = createControlPanel();
		controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(200, 200, 200)));

		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(controlPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);

		JPanel paginationPanel = createPaginationPanel();
		paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the pagination
																					// panel
		add(paginationPanel, BorderLayout.SOUTH);

		showPage(currentPage);
	}

	private void lemmatizeText() throws RemoteException {

		String text = textArea.getText(); // Get the content from the text area
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Text is empty. Please enter some text to lemmatize.", "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Text is empty. Please enter some text to lemmatize.");
			return;
		}

		// Ensure currentFileTitle is set
		if (currentFileTitle == null || currentFileTitle.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No file selected for lemmatization.", "Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("No file selected for lemmatization.");
			return;
		}

		// Get the file ID for the current file
		int fileId = taf.getFileIdByTitle(currentFileTitle); // Get the file ID by title

		if (fileId == -1) {
			JOptionPane.showMessageDialog(this, "Error retrieving file ID.", "Error", JOptionPane.ERROR_MESSAGE);
			logger.error("Error retrieving file ID.");
			return;
		}

		// Check if the lemmatization result already exists for this file

		// Lemmatize the sentence
		String lemmatizedText = taf.lemmatizeSentence(text);

		// Save the lemmatization result in the database
		taf.saveLemmatizationResult(currentFileTitle, text, lemmatizedText);

		// Retrieve the lemmatization results for the specific file
		List<String[]> lemmatizationResults = taf.getLemmatizationResults(fileId);

		// Open the window to display the results
		new LemmatizationResultWindow(lemmatizationResults); // Display results in a new window

		JOptionPane.showMessageDialog(this, "Lemmatization result saved successfully.", "Success",
				JOptionPane.INFORMATION_MESSAGE);
		logger.info("Lemmatization result saved successfully.\", \"Success");
	}

	private JButton createStyledButton(String text, Color background, Color foreground) {
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				if (getModel().isPressed()) {
					g.setColor(getBackground().darker());
				} else if (getModel().isRollover()) {
					g.setColor(getBackground().brighter());
				} else {
					g.setColor(getBackground());
				}
				super.paintComponent(g);
			}
		};

		button.setBackground(background);
		button.setForeground(foreground);
		button.setFont(new Font("Arial", Font.BOLD, 10));
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(70, 20));
		button.setMargin(new Insets(5, 10, 5, 5));

		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
				BorderFactory.createEmptyBorder(3, 10, 3, 10)));

		button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
			@Override
			public void installDefaults(AbstractButton b) {
				super.installDefaults(b);
				b.setRolloverEnabled(true);
			}
		});

		// Apply shadow effect when button is pressed
		button.setOpaque(true);
		button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		return button;
	}

	// In your control panel creation, modify the button color settings:
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		controlPanel.setBackground(new Color(245, 245, 245)); // Light gray background
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding for better appearance

		// Home button with custom color
		JButton homeButton = createStyledButton("Home", new Color(217, 83, 79), Color.WHITE);
		homeButton.setPreferredSize(new Dimension(70, 20));
		homeButton.addActionListener(e -> backToHome());
		controlPanel.add(homeButton);

		// Import button with custom color
		importButton = createStyledButton("Import", new Color(0, 123, 255), Color.WHITE);
		importButton.setPreferredSize(new Dimension(70, 20));
		importButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					importFile();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());

		controlPanel.add(importButton);

		exportButton = createStyledButton("Export", new Color(0, 173, 235), Color.WHITE);
		exportButton.setPreferredSize(new Dimension(70, 20));
		exportButton.addActionListener(e -> new Thread(new Runnable() {
			public void run() {
				try {
					exportFile();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());
		controlPanel.add(exportButton);

		// Enable Editing button with custom color
		enableEditButton = createStyledButton("Enable Editing", new Color(92, 184, 92), Color.WHITE);
		enableEditButton.setPreferredSize(new Dimension(70, 20));
		enableEditButton.addActionListener(e -> enableEditing());
		controlPanel.add(enableEditButton);

		JButton lemmatizeButton = createStyledButton("Lemmatize", new Color(0, 204, 255), Color.WHITE);
		lemmatizeButton.setPreferredSize(new Dimension(70, 20));
		lemmatizeButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					lemmatizeText();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());

		controlPanel.add(lemmatizeButton);

		// Inside the createControlPanel() method or wherever you create other buttons

		JButton tfIdfButton = createStyledButton("TF-IDF Analysis", new Color(0, 173, 235), Color.WHITE);
		tfIdfButton.setPreferredSize(new Dimension(70, 20));
		tfIdfButton.addActionListener(e -> {
			try {
				performTfIdfAnalysis(textArea.getText());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage());
				
			}
		}); // Calls the TextAnalyzer method
		controlPanel.add(tfIdfButton);

		// PMI Analysis Button
		JButton pmiButton = createStyledButton("PMI Analysis", new Color(0, 173, 235), Color.WHITE);
		pmiButton.setPreferredSize(new Dimension(70, 20));
		pmiButton.addActionListener(e -> {
			try {
				performPmiAnalysis(textArea.getText());
			} catch (RemoteException e1) {
				logger.error(e1.getMessage());
				
			}
		}); // Calls the TextAnalyzer method
		controlPanel.add(pmiButton);

		// PKL Analysis Button
		JButton pklButton = createStyledButton("PKL Analysis", new Color(0, 173, 235), Color.WHITE);
		pklButton.setPreferredSize(new Dimension(70, 20));
		pklButton.addActionListener(e -> {
			try {
				performPklAnalysis(textArea.getText());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage());
			}
		}); // Calls the TextAnalyzer method
		controlPanel.add(pklButton);

		// Font Style combo box
		String[] fontStyles = { "Plain", "Bold", "Italic" };
		JComboBox<String> fontStyleBox = new JComboBox<>(fontStyles);
		fontStyleBox.setFont(new Font("Arial", Font.PLAIN, 14));
		fontStyleBox.setBackground(new Color(255, 255, 255)); // White background
		fontStyleBox.setForeground(new Color(51, 51, 51)); // Dark gray font color
		fontStyleBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Light gray border
		fontStyleBox.setPreferredSize(new Dimension(70, 20)); // Size adjustment for a balanced look
		fontStyleBox.addActionListener(new FontStyleListener());
		controlPanel.add(new JLabel("Font Style:"));
		controlPanel.add(fontStyleBox);

		// Font Size combo box
		Integer[] fontSizes = { 12, 14, 16, 18, 20, 24, 28, 32, 36, 40 };
		JComboBox<Integer> fontSizeBox = new JComboBox<>(fontSizes);
		fontSizeBox.setFont(new Font("Arial", Font.PLAIN, 14));
		fontSizeBox.setBackground(new Color(255, 255, 255)); // White background
		fontSizeBox.setForeground(new Color(51, 51, 51)); // Dark gray font color
		fontSizeBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Light gray border
		fontSizeBox.setPreferredSize(new Dimension(70, 20)); // Size adjustment for a balanced look
		fontSizeBox.addActionListener(new FontSizeListener());
		controlPanel.add(new JLabel("Font Size:"));
		controlPanel.add(fontSizeBox);

		JButton transliterateButton = createStyledButton("Transliterate", new Color(255, 193, 7), Color.WHITE);
		transliterateButton.setPreferredSize(new Dimension(70, 20));
		transliterateButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					transliterateText();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());

		controlPanel.add(transliterateButton);

		// Add this code in the createControlPanel method
		JButton posTagButton = createStyledButton("POS Tagging", new Color(108, 117, 125), Color.WHITE);
		posTagButton.setPreferredSize(new Dimension(70, 20));
		// Assuming this is within your GUI class where posTagButton is defined
		posTagButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					performPOSTagging();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());
		controlPanel.add(posTagButton);

		JButton arabicWordSegmentationButton = createStyledButton("Analyze Arabic Text", new Color(108, 117, 125),
				Color.WHITE);
		arabicWordSegmentationButton.setPreferredSize(new Dimension(70, 20));
		arabicWordSegmentationButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					performWordCountAnalysis();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());
		controlPanel.add(arabicWordSegmentationButton);

		// Assuming controlPanel is an instance variable

		// Save button with custom color
		JButton saveButton = createStyledButton("Save", new Color(92, 184, 92), Color.WHITE);
		saveButton.setPreferredSize(new Dimension(70, 20));
		saveButton.addActionListener(e ->

		new Thread(new Runnable() {
			public void run() {
				try {
					saveFile();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}).start());
		controlPanel.add(saveButton);

		return controlPanel;
	}

	private JPanel createPaginationPanel() {
		JPanel paginationPanel = new JPanel();
		paginationPanel.setBackground(new Color(245, 245, 245)); // Light gray background
		paginationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center the buttons

		prevPageButton = createStyledButton("Previous", new Color(91, 192, 222), Color.WHITE);
		prevPageButton.addActionListener(e -> showPage(currentPage - 1));
		prevPageButton.setEnabled(false); // Initially disabled
		paginationPanel.add(prevPageButton);

		nextPageButton = createStyledButton("Next", new Color(91, 192, 222), Color.WHITE);
		nextPageButton.addActionListener(e -> showPage(currentPage + 1));
		nextPageButton.setEnabled(content.split("\n").length > LINES_PER_PAGE); // Enable only if content exceeds one
																				// page
		paginationPanel.add(nextPageButton);

		// Page label to display current page and total pages
		pageLabel = new JLabel();
		pageLabel.setFont(new Font("Arial", Font.BOLD, 12));
		pageLabel.setForeground(Color.DARK_GRAY);
		paginationPanel.add(pageLabel);

		return paginationPanel;
	}

	private void transliterateText() throws RemoteException {
		String arabicContent = textArea.getText();
		String romanContent = taf.transliterate(arabicContent);

		// Display the transliterated content
		textArea.setText(romanContent);

	}

	public void performTfIdfAnalysis(String arabicText) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				try {
					taf.calculateTfIdf(arabicText);
				} catch (RemoteException e) {
					logger.error(e.getMessage());
				}
			}
		}).start();
	}

	// Method to perform PMI analysis
	public void performPmiAnalysis(String arabicText) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				try {
					taf.calculatePmi(arabicText); // Call TextAnalyzer for PMI analysis
				} catch (RemoteException e) {
					logger.error(e.getMessage());
				}
			}
		}).start();
	}

	// Method to perform PKL analysis
	public void performPklAnalysis(String arabicText) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				try {
					taf.calculatePkl(arabicText); // Call TextAnalyzer for PKL analysis
				} catch (RemoteException e) {
					logger.error(e.getMessage());
				}
			}
		}).start();
	}

	private void enableEditing() {
		textArea.setEditable(true);
		enableEditButton.setEnabled(false);
		importButton.setEnabled(true);
		JOptionPane.showMessageDialog(this, "You can now edit the text.", "Editing Enabled",
				JOptionPane.INFORMATION_MESSAGE);
		logger.info("You can now edit the text.Editing Enabled");
	}

	private void importFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a Text File to Import");
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				String fileContent = new String(Files.readAllBytes(selectedFile.toPath()));
				textArea.setText(fileContent);
				content = fileContent;
				currentPage = 0;
				showPage(currentPage);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error reading file.", "File Error", JOptionPane.ERROR_MESSAGE);
				logger.error("Error reading file.");
			}
		}
	}

	private void saveFile() throws RemoteException {

		String content = textArea.getText();
		taf.updateFile(title, content); // Use FileFacade to update file
		JOptionPane.showMessageDialog(this, "File saved successfully!");
	}

	private void showPage(int page) {
		if (page < 0) {
			return;
		}

		// Split content into lines for pagination
		String[] lines = content.split("\n");
		int start = page * LINES_PER_PAGE;
		int end = Math.min(start + LINES_PER_PAGE, lines.length);

		StringBuilder pageContent = new StringBuilder();
		for (int i = start; i < end; i++) {
			pageContent.append(lines[i]).append("\n");
		}

		textArea.setText(pageContent.toString());
		currentPage = page;

		// Update buttons and page label
		prevPageButton.setEnabled(currentPage > 0);
		nextPageButton.setEnabled(currentPage * LINES_PER_PAGE < lines.length);
		pageLabel.setText(
				"Page " + (currentPage + 1) + " of " + (int) Math.ceil((double) lines.length / LINES_PER_PAGE));
	}

	private void backToHome() {
		homePage.setVisible(true);
		setVisible(false);
	}

	private class FontStyleListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String selectedStyle = (String) ((JComboBox<?>) e.getSource()).getSelectedItem();
			int style = Font.PLAIN;

			switch (selectedStyle) {
			case "Bold":
				style = Font.BOLD;
				break;
			case "Italic":
				style = Font.ITALIC;
				break;
			}

			textArea.setFont(new Font("Arial", style, textArea.getFont().getSize()));
		}
	}

	// Method to show lemmatization results in a new dialog with table
	public void performPOSTagging() throws RemoteException {
		JFrame parentFrame = this;
		String text = textArea.getText().trim();
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(parentFrame, "Please enter Arabic text for analysis.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Please enter Arabic text for analysis.");
			return;
		}

		// Check if file exists

		if (!taf.fileExists(title)) {
			JOptionPane.showMessageDialog(parentFrame, "File with title '" + title + "' not found.", "File Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("File with title '" + title + "' not found.");
			return;
		}

		// Perform segmentation
		List<String> segmentedWords = taf.segmentArabicText(text);
		List<String[]> wordDetails = taf.tagWords(segmentedWords);

		// Save POS tagging results in the database
		taf.savePOSTaggingResults(title, wordDetails);

		// Display results in a JTable
		displayPOSResults(wordDetails, parentFrame);
	}

	private void displayPOSResults(List<String[]> taggedWords, JFrame parentFrame) {
		String[][] tableData = new String[taggedWords.size()][2];
		for (int i = 0; i < taggedWords.size(); i++) {
			tableData[i] = taggedWords.get(i);
		}

		String[] columnNames = { "Segmented Word", "Part of Speech" };
		DefaultTableModel tableModel = new DefaultTableModel(tableData, columnNames);
		JTable table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 100));
		table.setFillsViewportHeight(true);

		JScrollPane scrollPane = new JScrollPane(table);
		JFrame frame = new JFrame("POS Tagged Words");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(parentFrame);
		frame.setVisible(true);
	}

	private void performWordCountAnalysis() {
		// Get the original text from the text area
		String text = textArea.getText();

		// Split text into words using whitespace as a delimiter
		String[] words = text.split("\\s+");

		// Count occurrences of each word (case-sensitive)
		Map<String, Long> wordCountMap = Arrays.stream(words)
				.collect(Collectors.groupingBy(word -> word, Collectors.counting()));

		// Prepare the results as a string
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("=== Word Analysis ===\n");
		resultBuilder.append("Total Words: ").append(words.length).append("\n");
		resultBuilder.append("Unique Words: ").append(wordCountMap.size()).append("\n\n");
		resultBuilder.append("Word Details:\n");

		wordCountMap.forEach((word, count) -> {
			resultBuilder.append("Word: ").append(word).append(" | Count: ").append(count).append("\n");
		});

		// Display the results in a popup
		JOptionPane.showMessageDialog(null, resultBuilder.toString(), "Word Count Analysis",
				JOptionPane.INFORMATION_MESSAGE);
		logger.info(" Displaying Word Count Analysis.......");
	}

	private void exportFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a Location to Export the File");
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();

			// Ensure the file has a .md extension
			if (!selectedFile.getName().endsWith(".md")) {
				selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + ".md");
			}

			try {
				// Convert the content to Markdown format
				String markdownContent = convertToMarkdown(content);
				Files.write(selectedFile.toPath(), markdownContent.getBytes());
				JOptionPane.showMessageDialog(this, "File saved as Markdown successfully.", "Save Success",
						JOptionPane.INFORMATION_MESSAGE);
				logger.info("File saved as Markdown successfully.");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error saving file.", "Save Error", JOptionPane.ERROR_MESSAGE);
				logger.error("Error saving file.");
			}
		}
	}

	private String convertToMarkdown(String content) {
		StringBuilder markdown = new StringBuilder();

		// Example: Add headings and text formatting
		String[] lines = content.split("\n");
		for (String line : lines) {
			// Basic example: If the line starts with '#' treat it as a heading (optional)
			if (line.startsWith("###")) {
				markdown.append("### ").append(line.substring(3).trim()).append("\n");
			} else if (line.startsWith("##")) {
				markdown.append("## ").append(line.substring(2).trim()).append("\n");
			} else if (line.startsWith("#")) {
				markdown.append("# ").append(line.substring(1).trim()).append("\n");
			} else {
				markdown.append(line).append("\n"); // Regular text
			}
		}
		return markdown.toString();
	}

	private class FontSizeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedSize = (Integer) ((JComboBox<?>) e.getSource()).getSelectedItem();
			textArea.setFont(new Font("Arial", textArea.getFont().getStyle(), selectedSize));
		}
	}

	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
