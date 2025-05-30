package presentation_layer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import Common.ITextAnalysisFacade;


public class HomePage extends JFrame {
	private static final Logger logger = (Logger) LogManager.getLogger(HomePage.class);
    private static final long serialVersionUID = 1L;
   
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private String currentFileTitle = "";  // This will store the title of the selected file
    private ITextAnalysisFacade taf;
    public HomePage(ITextAnalysisFacade taf) throws Exception {
    	
    	this.taf=taf;
        setTitle("Text Editor - Home");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLookAndFeel();

        JPanel headerPanel = createHeaderPanel();
        JPanel mainPanel = createMainPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(44, 62, 80));
        JLabel headerLabel = new JLabel("Text Editor");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(236, 240, 241));

        tableModel = new DefaultTableModel(new Object[]{"Title", "Created At", "Updated At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fileTable = new JTable(tableModel);
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        fileTable.setRowHeight(35);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customizeTableAppearance();

        updateFileList();

        // Row Selection Listener to update currentFileTitle
        fileTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = fileTable.getSelectedRow();
            if (selectedRow != -1) {
                // Assuming the title is in the first column (index 0)
                currentFileTitle = (String) tableModel.getValueAt(selectedRow, 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), "Available Files"));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel searchPanel = createSearchPanel();
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(236, 240, 241));

        searchField = new JTextField(15);
        searchField.setText("Search");
        searchField.setForeground(new Color(150, 150, 150));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.addFocusListener(new SearchFieldFocusListener());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterFileList(searchField.getText());
            }
        });
        searchPanel.add(searchField);

        return searchPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(236, 240, 241));

       

        JButton createButton = createStyledButton("Create New File", new Color(52, 152, 219), Color.WHITE);
        createButton.addActionListener(e -> createNewFile());

        JButton openButton = createStyledButton("Open File", new Color(46, 204, 113), Color.WHITE);
        openButton.addActionListener(e -> openFile());

        JButton deleteButton = createStyledButton("Delete File", new Color(231, 76, 60), Color.WHITE);
        deleteButton.addActionListener(e -> deleteFile());

        buttonPanel.add(createButton);
        buttonPanel.add(openButton);
        buttonPanel.add(deleteButton);

        return buttonPanel;
    }

    private class SearchFieldFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            if (searchField.getText().equals("Search")) {
                searchField.setText("");
                searchField.setForeground(Color.DARK_GRAY);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (searchField.getText().isEmpty()) {
                searchField.setText("Search");
                searchField.setForeground(new Color(150, 150, 150));
            }
        }
    }

    private void customizeTableAppearance() {
        fileTable.setShowGrid(false);
        fileTable.setIntercellSpacing(new Dimension(0, 0));

        fileTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = -5160218071148309674L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JTableHeader header = fileTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBackground(new Color(44, 62, 80));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        fileTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(150);
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
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        button.setMargin(new Insets(10, 20, 10, 20));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installDefaults(AbstractButton b) {
                super.installDefaults(b);
                b.setRolloverEnabled(true);
            }
        });

        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        return button;
    }

    private void updateFileList() {
        tableModel.setRowCount(0);
        List<String[]> files = null;
		try {
			files = taf.getAllFilesWithDates();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (String[] file : files) {
            tableModel.addRow(file);
        }
    }

    private void filterFileList(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        fileTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    private void createNewFile() {
        String title = JOptionPane.showInputDialog(this, "Enter the file title:");
        if (title != null && !title.trim().isEmpty()) {
        	try {
				taf.createFile(title, "");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            updateFileList();
            showMessage("File created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            logger.info("File created successfully!\", \"Success");
        } else {
            showMessage("Title cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            logger.error("Title cannot be empty!");
        }
    }

    private void openFile() {
        if (!currentFileTitle.isEmpty()) {
            String existingContent = null;
			try {
				existingContent = taf.getFileContent(currentFileTitle);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            new TextEditorPage(taf, currentFileTitle, existingContent, this,currentFileTitle).setVisible(true);
        } else {
            showMessage("Please select a file to open.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            logger.warn("Please select a file to open.");
        }
    }

    private void deleteFile() {
        if (!currentFileTitle.isEmpty()) {
            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this file?", "Delete File", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
            	try {
					taf.deleteFile(currentFileTitle);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                updateFileList();
                showMessage("File deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                logger.info("File deleted successfully!\", \"Success");
                
            }
        } else {
            showMessage("Please select a file to delete.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            logger.warn("Please select a file to delete.");
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
