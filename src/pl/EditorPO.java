package pl;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bll.IEditorBO;
import dto.Documents;
import dto.Pages;

public class EditorPO extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EditorPO.class);
	private IEditorBO businessObj;
	private DefaultTableModel tableModel;
	private JPanel mainPanel, editPanel, transliterationPanel;
	private JTable fileTable;
	private JTextArea contentTextArea, transliteratedTextArea;
	private JButton nextButton, previousButton;
	private JLabel pageCountLabel;
	private JLabel savingStatusLabel;
	private JLabel wordCountLabel;
	private JLabel importProgressLabel;
	private JLabel avgWordLengthLabel;
	private JLabel totalLineCountLabel;
	private Documents doc;
	private List<Pages> pages;
	private int currentPage = 1;
	private int totalPageCount = 0;
	private int selectedRow = 0;
	// private int unselectedRows = 0;
	// private int totalRows = 0;
	private Thread importThread;
	private Thread autoSaveThread;
	private boolean autoSaveRunning = false;
	private Thread tfidfThread;
	private int selectedDocFileId;
	private Documents selectedDoc;
	private double tfidfScore = 0;
	private Thread pklThread;
	private Map<String, Double> pklResults = new HashMap<>();
	private Thread pmiThread;
	private Map<String, Double> pmiResults = new HashMap<>();
	private Thread posThread;
	private Map<String, List<String>> posMap = new HashMap<>();
	private Thread rootThread;
	private Map<String, String> rootMap = new HashMap<>();
	private Thread lemmaThread;
	private Map<String, String> lemmaMap = new HashMap<>();
	private Thread stemThread;
	private Map<String, String> stemMap = new HashMap<>();
	private Thread wordSegementThread;
	private Map<String, String> segmentMap = new HashMap<>();
	private Thread wordCountThread;
	private Thread avgWordLengthThread;
	private Thread totalLineCountThread;
	private boolean wordCountRunning = true;
	private boolean avgWordLengthRunning = true;
	private boolean totalLineCountRunning = true;
	Font buttonFont = new Font("Arial", Font.BOLD, 12);

	public EditorPO(IEditorBO businessObj) {
		this.businessObj = businessObj;

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("Real Text Editor");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new CardLayout());

		// Initialize and set up panels
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.LIGHT_GRAY);
		setupMainMenuPanel();
		editPanel = new JPanel(new BorderLayout());
		editPanel.setBackground(Color.LIGHT_GRAY);
		setupEditPanel();
		transliterationPanel = new JPanel(new BorderLayout());
		transliterationPanel.setBackground(Color.LIGHT_GRAY);
		setupTransliterationPanel();

		add(mainPanel, "MainMenu");
		add(editPanel, "EditDocument");
		add(transliterationPanel, "TransliterationView");

		setVisible(true);
	}

	private void setupMainMenuPanel() {

		tableModel = new DefaultTableModel(new Object[] { "File ID", "File Name", "Last Modified", "Date Created" },
				0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		fileTable = new JTable(tableModel);
		fileTable.getTableHeader().setReorderingAllowed(false);
		fileTable.getColumnModel().getColumn(0).setMinWidth(0);
		fileTable.getColumnModel().getColumn(0).setMaxWidth(0);
		fileTable.getTableHeader().setReorderingAllowed(false);
		JScrollPane scroller = new JScrollPane(fileTable);
		JButton importFileButton = new JButton("Upload Files");
		JButton createFileButton = new JButton("Create New File");
		JButton deleteFileButton = new JButton("Delete File(s)");
		JButton viewFilesButton = new JButton("View Files");
		JButton tfidfButton = new JButton("TF-IDF");
		tfidfButton.setEnabled(false);
		JTextField searchfield = new JTextField(20);
		JButton searchbutton = new JButton("Search");
		importProgressLabel = new JLabel();
		importProgressLabel.setText("");
		importProgressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(importProgressLabel, BorderLayout.SOUTH);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

		importFileButton.setFont(buttonFont);
		createFileButton.setFont(buttonFont);
		deleteFileButton.setFont(buttonFont);
		viewFilesButton.setFont(buttonFont);
		tfidfButton.setFont(buttonFont);
		searchbutton.setFont(buttonFont);
		importProgressLabel.setFont(buttonFont);

		importFileButton.setBackground(Color.WHITE);
		importFileButton.setForeground(Color.BLACK);
		createFileButton.setBackground(Color.WHITE);
		createFileButton.setForeground(Color.BLACK);
		deleteFileButton.setBackground(Color.WHITE);
		deleteFileButton.setForeground(Color.BLACK);
		viewFilesButton.setBackground(Color.WHITE);
		viewFilesButton.setForeground(Color.BLACK);
		tfidfButton.setBackground(Color.WHITE);
		tfidfButton.setForeground(Color.BLACK);

		for (Component button : buttonPanel.getComponents()) {
			if (button instanceof JButton) {
				((JButton) button).setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			}
		}

		buttonPanel.add(importFileButton);
		buttonPanel.add(createFileButton);
		buttonPanel.add(deleteFileButton);
		buttonPanel.add(viewFilesButton);
		buttonPanel.add(tfidfButton);
		buttonPanel.add(searchfield);
		buttonPanel.add(searchbutton);

		mainPanel.add(buttonPanel, BorderLayout.NORTH);
		mainPanel.add(scroller, BorderLayout.CENTER);

		fileTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					selectedRow = fileTable.getSelectedRow();
					if (selectedRow != -1) {
						int fileId = (int) tableModel.getValueAt(selectedRow, 0);
						openEditPanel(fileId);
					}
				} else if (event.getClickCount() == 1) {
					tfidfThread = new Thread(new Runnable() {

						@Override
						public void run() {
							selectedDocFileId = (int) tableModel.getValueAt(selectedRow, 0);
							selectedDoc = null;
							selectedDoc = businessObj.getFile(selectedDocFileId);
							pages = selectedDoc.getPages();
							String selectedDocContent = null;
							for (int i = 0; i < pages.size(); i++) {
								selectedDocContent = pages.get(i).getPageContent();
							}

							List<String> unselectedDocsContent = new ArrayList<String>();
							for (int row = 0; row < fileTable.getRowCount(); row++) {
								int unselectedDocFileId = (int) tableModel.getValueAt(row, 0);
								if (unselectedDocFileId != selectedDocFileId) {
									Documents unselectedDoc = null;
									unselectedDoc = businessObj.getFile(unselectedDocFileId);
									pages = unselectedDoc.getPages();
									String unselectedDocContent = null;
									for (int i = 0; i < pages.size(); i++) {
										unselectedDocContent = pages.get(i).getPageContent();
									}
									unselectedDocsContent.add(unselectedDocContent);
								}
							}
							tfidfScore = businessObj.performTFIDF(unselectedDocsContent, selectedDocContent);
						}

					});
					tfidfThread.start();
					tfidfButton.setEnabled(true);
					// totalRows = fileTable.getRowCount();
					selectedRow = fileTable.getSelectedRow();
					// unselectedRows = totalRows - selectedRow;
				}
			}
		});

		importFileButton.addActionListener(e -> {
			importThread = new Thread(new Runnable() {

				@Override
				public void run() {
					FileImporter fileImporter = new FileImporter(businessObj);
					importProgressLabel.setText("Importing files, please wait...");
					importProgressLabel.setVisible(true);
					fileImporter.importFiles(e);
					refreshFileList();
					importProgressLabel.setText("Import complete!");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.error(e1.getMessage());
					}
					importProgressLabel.setVisible(false);
				}

			});
			importThread.start();
		});
		createFileButton.addActionListener(e -> {
			createFile(e);
		});
		deleteFileButton.addActionListener(e -> {
			deleteSelectedFiles(e);
		});
		viewFilesButton.addActionListener(e -> {
			refreshFileList();
		});
		searchbutton.addActionListener(e -> {
			String keyword = searchfield.getText();
			try {
				List<String> resultFiles = businessObj.searchKeyword(keyword);
				if (resultFiles.isEmpty()) {
					JOptionPane.showMessageDialog(this, "No files found while searching.");
					logger.info("No files found while searching.");
				} else {
					new SearchFrame(resultFiles);
				}
			} catch (IllegalArgumentException exception) {
				JOptionPane.showMessageDialog(this, exception.getMessage());
				logger.error(exception.getMessage());
			}
		});

		tfidfButton.addActionListener(e -> {
			JOptionPane.showMessageDialog(null, "TF-IDF Score for '" + selectedDoc.getName() + "' is: " + tfidfScore);
			logger.info("TF-IDF Score for '" + selectedDoc.getName() + "' is: " + tfidfScore);
		});
	}

	private void setupEditPanel() {
		contentTextArea = new JTextArea(10, 40);
		contentTextArea.setLineWrap(true);
		contentTextArea.setWrapStyleWord(true);
		contentTextArea.setEditable(true);
		contentTextArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JScrollPane contentScroller = new JScrollPane(contentTextArea);
		contentScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JButton saveFileButton = new JButton("Save File");
		JButton backButton = new JButton("Back to Menu");
		JButton transliterateButton = new JButton("Transliterate Content");
		JButton lemmatizeButton = new JButton("Lemmatize Content");
		JButton extractPOSButton = new JButton("Extract POS");
		JButton extractRootsButton = new JButton("Extract Roots");
		JButton pmiButton = new JButton("Calculate PMI");
		JButton pklButton = new JButton("Calculate PKL");
		JButton stemmingButton = new JButton("Stem Content");
		JButton segmentationButton = new JButton("Segment Content");

		saveFileButton.setFont(buttonFont);
		backButton.setFont(buttonFont);
		transliterateButton.setFont(buttonFont);
		lemmatizeButton.setFont(buttonFont);
		extractPOSButton.setFont(buttonFont);
		extractRootsButton.setFont(buttonFont);
		pmiButton.setFont(buttonFont);
		pklButton.setFont(buttonFont);
		stemmingButton.setFont(buttonFont);
		segmentationButton.setFont(buttonFont);

		nextButton = new JButton("Next Page");
		previousButton = new JButton("Previous Page");
		nextButton.setEnabled(false);
		previousButton.setEnabled(false);

		pageCountLabel = new JLabel("Page 0 of 0");
		savingStatusLabel = new JLabel("Auto-Saving...");
		wordCountLabel = new JLabel("Words: 0");
		avgWordLengthLabel = new JLabel("(Avg Word Length: 0)");
		totalLineCountLabel = new JLabel("Lines: 0");
		savingStatusLabel.setFont(buttonFont);
		totalLineCountLabel.setFont(buttonFont);
		wordCountLabel.setFont(buttonFont);
		avgWordLengthLabel.setFont(buttonFont);

		JPanel editButtonPanel = new JPanel(new FlowLayout());

		contentTextArea.setBackground(Color.WHITE);
		contentTextArea.setForeground(Color.BLACK);
		Font textFont = new Font("Arial", Font.BOLD, 30);
		contentTextArea.setFont(textFont);
		contentTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		saveFileButton.setBackground(Color.WHITE);
		saveFileButton.setForeground(Color.BLACK);
		backButton.setBackground(Color.WHITE);
		backButton.setForeground(Color.BLACK);
		transliterateButton.setBackground(Color.WHITE);
		transliterateButton.setForeground(Color.BLACK);
		lemmatizeButton.setBackground(Color.WHITE);
		lemmatizeButton.setForeground(Color.BLACK);
		extractRootsButton.setBackground(Color.WHITE);
		extractRootsButton.setForeground(Color.BLACK);
		extractPOSButton.setBackground(Color.WHITE);
		extractPOSButton.setForeground(Color.BLACK);
		pmiButton.setBackground(Color.WHITE);
		pmiButton.setForeground(Color.BLACK);
		pklButton.setBackground(Color.WHITE);
		pklButton.setForeground(Color.BLACK);
		stemmingButton.setBackground(Color.WHITE);
		stemmingButton.setForeground(Color.BLACK);
		segmentationButton.setBackground(Color.WHITE);
		segmentationButton.setForeground(Color.BLACK);

		for (Component button : editButtonPanel.getComponents()) {
			if (button instanceof JButton) {
				((JButton) button).setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			}
		}

		editButtonPanel.add(totalLineCountLabel);
		editButtonPanel.add(wordCountLabel);
		editButtonPanel.add(avgWordLengthLabel);
		editButtonPanel.add(previousButton);
		editButtonPanel.add(pageCountLabel);
		editButtonPanel.add(nextButton);
		editButtonPanel.add(savingStatusLabel);
		editButtonPanel.add(saveFileButton);
		editButtonPanel.add(backButton);
		editButtonPanel.add(transliterateButton);
		editButtonPanel.add(segmentationButton);
		editButtonPanel.add(stemmingButton);
		editButtonPanel.add(lemmatizeButton);
		editButtonPanel.add(extractPOSButton);
		editButtonPanel.add(extractRootsButton);
		editButtonPanel.add(pmiButton);
		editButtonPanel.add(pklButton);

		JPanel resultPanel = new JPanel(new BorderLayout());
		JLabel resultLabel = new JLabel("Results:");
		resultPanel.add(resultLabel, BorderLayout.NORTH);

		DefaultTableModel resultTableModel = new DefaultTableModel(new Object[] { "Word", "Result" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable resultTable = new JTable(resultTableModel);

		resultTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane resultScroller = new JScrollPane(resultTable);
		resultPanel.add(resultScroller, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentScroller, resultPanel);

		splitPane.setResizeWeight(0.7);
		splitPane.setDividerLocation(0.7);
		splitPane.setEnabled(false);
		splitPane.setDividerSize(0);

		editPanel.add(splitPane, BorderLayout.CENTER);
		editPanel.add(editButtonPanel, BorderLayout.SOUTH);

		nextButton.addActionListener(e -> nextPage());
		previousButton.addActionListener(e -> previousPage());
		saveFileButton.addActionListener(e -> {
			saveFile();
		});
		backButton.addActionListener(e -> {
			autoSaveRunning = false;
			if (autoSaveThread != null && autoSaveThread.isAlive()) {
				autoSaveThread.interrupt();
			}

			totalLineCountRunning = false;
			if (totalLineCountThread != null && totalLineCountThread.isAlive()) {
				totalLineCountThread.interrupt();
			}

			wordCountRunning = false;
			if (wordCountThread != null && wordCountThread.isAlive()) {
				wordCountThread.interrupt();
				avgWordLengthThread.interrupt();
			}

			avgWordLengthRunning = false;
			if (avgWordLengthThread != null && avgWordLengthThread.isAlive()) {
				avgWordLengthThread.interrupt();
			}
			CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
			cardLayout.show(getContentPane(), "MainMenu");
			refreshFileList();
		});
		transliterateButton.addActionListener(e -> {
			transliterateContent();
		});

		lemmatizeButton.addActionListener(e -> displayWordResults(() -> {
			return lemmaMap;
		}, resultTableModel));

		stemmingButton.addActionListener(e -> displayWordResults(() -> {
			return stemMap;
		}, resultTableModel));

		extractPOSButton.addActionListener(e -> displayWordResults(() -> {
			return posMap;
		}, resultTableModel));

		extractRootsButton.addActionListener(e -> displayWordResults(() -> {
			return rootMap;
		}, resultTableModel));

		segmentationButton.addActionListener(e -> displayWordResults(() -> {
			return segmentMap;
		}, resultTableModel));

		pmiButton.addActionListener(e -> {
			displayAnalyticResults(pmiResults, resultTableModel);
		});

		pklButton.addActionListener(e -> {
			displayAnalyticResults(pklResults, resultTableModel);
		});
	}

	private void displayAnalyticResults(Map<String, Double> analyticsScore, DefaultTableModel resultTableModel) {
		String content = contentTextArea.getText();
		resultTableModel.setRowCount(0);

		if (content != null && !content.trim().isEmpty()) {
			try {
				for (Map.Entry<String, Double> entry : analyticsScore.entrySet()) {
					resultTableModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error processing analytic score: " + e.getMessage());
				logger.error("Error processing analytic score: " + e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this, "Content is empty. Please enter text to process.");
			logger.error("Content is empty. Please enter text to process.");
		}

		// editPanel.revalidate();
		// editPanel.repaint();
	}

	private void displayWordResults(Supplier<Object> action, DefaultTableModel resultTableModel) {
		String content = contentTextArea.getText();
		if (content != null && !content.trim().isEmpty()) {
			try {
				// Execute the action (either lemmatization, stemming, etc.)
				Object result = action.get();

				// Clear existing rows
				resultTableModel.setRowCount(0);

				if (result instanceof Map) {
					Map<?, ?> resultMap = (Map<?, ?>) result;

					if (resultMap.keySet().iterator().next() instanceof String) {
						for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
							resultTableModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
						}
					}

					else if (resultMap.keySet().iterator().next() instanceof String
							&& resultMap.values().iterator().next() instanceof List) {
						for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
							resultTableModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
						}
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error processing word analytics: " + e.getMessage());
				logger.error("Error processing word analytics: " + e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this, "Content is empty. Please enter text to process.");
			logger.error("Content is empty. Please enter text to process.");
		}
	}

	// private void lemmatizeContent() {
	// String content = contentTextArea.getText();
	// if (content != null && !content.trim().isEmpty()) {
	// List<String> lemmatizedWords = businessObj.lemmatizeWords(content);
	// String result = String.join(", ", lemmatizedWords);
	// JOptionPane.showMessageDialog(this, "Lemmatized Words: " + result);
	// } else {
	// JOptionPane.showMessageDialog(null, "Content is empty. Please enter text to
	// lemmatize.");
	// }
	// }
	//
	// private void extractPOSContent() {
	// String content = contentTextArea.getText();
	// if (content != null && !content.trim().isEmpty()) {
	// List<String> posList = businessObj.extractPOS(content);
	// String result = String.join(", ", posList);
	// JOptionPane.showMessageDialog(this, "Extracted POS: " + result);
	// } else {
	// JOptionPane.showMessageDialog(null, "Content is empty. Please enter text to
	// extract POS.");
	// }
	// }
	//
	// private void extractRootsContent() {
	// String content = contentTextArea.getText();
	// if (content != null && !content.trim().isEmpty()) {
	// List<String> rootsList = businessObj.extractRoots(content);
	// String result = String.join(", ", rootsList);
	// JOptionPane.showMessageDialog(this, "Extracted Roots: " + result);
	// } else {
	// JOptionPane.showMessageDialog(null, "Content is empty. Please enter text to
	// extract roots.");
	// }
	// }

	private void setupTransliterationPanel() {
		transliteratedTextArea = new JTextArea();
		transliteratedTextArea.setLineWrap(true);
		transliteratedTextArea.setWrapStyleWord(true);
		transliteratedTextArea.setEditable(false);
		Font textFont = new Font("Arial", Font.BOLD, 20);
		transliteratedTextArea.setFont(textFont);

		JScrollPane transliterationScroller = new JScrollPane(transliteratedTextArea);
		transliterationScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JButton backToEditButton = new JButton("Back to Edit");
		backToEditButton.setFont(buttonFont);

		backToEditButton.setBackground(Color.WHITE);
		backToEditButton.setForeground(Color.BLACK);

		backToEditButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		backToEditButton.addActionListener(e -> {
			CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
			cardLayout.show(getContentPane(), "EditDocument");
		});

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(backToEditButton);

		transliterationPanel.add(transliterationScroller, BorderLayout.CENTER);
		transliterationPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private double calculateAvgWordLength(String content) {
		if (content == null || content.isEmpty()) {
			return 0;
		}

		String[] words = content.split("\\s+");
		int totalLength = 0;
		int wordCount = 0;

		for (String word : words) {
			if (!word.isEmpty()) {
				totalLength += word.length();
				wordCount++;
			}
		}

		return wordCount == 0 ? 0 : (double) totalLength / wordCount;
	}

	private int calculateWordCount(String text) {
		if (text == null || text.trim().isEmpty()) {
			return 0;
		}
		return text.trim().split("\\s+").length;
	}

	private int calculateLineCount(String content) {
		if (content == null || content.isEmpty()) {
			return 0;
		}
		String[] lines = content.split("\r?\n");
		return lines.length;
	}

	private void openEditPanel(int fileId) {
		currentPage = 1;
		doc = businessObj.getFile(fileId);
		pages = doc.getPages();
		totalPageCount = pages.size();

		loadPage(currentPage);

		if (autoSaveThread != null && autoSaveThread.isAlive()) {
			autoSaveRunning = false;
			try {
				autoSaveThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		autoSaveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				autoSaveRunning = true;
				while (autoSaveRunning) {
					try {
						autoSaveFile();
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});

		totalLineCountThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (totalLineCountRunning) {
					try {
						Thread.sleep(500);
						String content = contentTextArea.getText();
						int lineCount = calculateLineCount(content);

						SwingUtilities.invokeLater(() -> {
							totalLineCountLabel.setText("Lines: " + lineCount);
						});
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		});

		wordCountThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (wordCountRunning) {
					try {
						Thread.sleep(500);
						String content = contentTextArea.getText();
						int wordCount = calculateWordCount(content);
						SwingUtilities.invokeLater(() -> {
							wordCountLabel.setText("Words: " + wordCount);
						});
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		});

		avgWordLengthThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (avgWordLengthRunning) {
					try {
						Thread.sleep(500);
						String content = contentTextArea.getText();
						double avgWordLength = calculateAvgWordLength(content);
						SwingUtilities.invokeLater(() -> {
							avgWordLengthLabel.setText("(Avg Word Length: " + (int) avgWordLength + ")");
						});
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		});

		pklThread = new Thread(new Runnable() {

			@Override
			public void run() {
				pklResults = null;
				pklResults = businessObj.performPKL(contentTextArea.getText());
			}
		});

		pmiThread = new Thread(new Runnable() {

			@Override
			public void run() {
				pmiResults = null;
				pmiResults = businessObj.performPMI(contentTextArea.getText());
			}
		});

		posThread = new Thread(new Runnable() {

			@Override
			public void run() {
				posMap = businessObj.extractPOS(contentTextArea.getText());
			}
		});
		lemmaThread = new Thread(new Runnable() {

			@Override
			public void run() {
				lemmaMap = businessObj.lemmatizeWords(contentTextArea.getText());
			}
		});

		rootThread = new Thread(new Runnable() {

			@Override
			public void run() {
				rootMap = businessObj.extractRoots(contentTextArea.getText());
			}
		});

		stemThread = new Thread(new Runnable() {

			@Override
			public void run() {
				stemMap = businessObj.stemWords(contentTextArea.getText());
			}
		});

		wordSegementThread = new Thread(new Runnable() {

			@Override
			public void run() {
				segmentMap = businessObj.segmentWords(contentTextArea.getText());
			}
		});

		autoSaveThread.start();
		totalLineCountThread.start();
		wordCountThread.start();
		avgWordLengthThread.start();
		pklThread.start();
		pmiThread.start();
		posThread.start();
		lemmaThread.start();
		rootThread.start();
		stemThread.start();
		wordSegementThread.start();

		CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
		cardLayout.show(getContentPane(), "EditDocument");
	}

	private void createFile(ActionEvent e) {
		String fileName = JOptionPane.showInputDialog("Enter file name:");
		String fileContent = JOptionPane.showInputDialog("Enter file content:");
		if (fileName != null) {
			boolean created = businessObj.createFile(fileName, fileContent);
			JOptionPane.showMessageDialog(null, created ? "File created successfully!" : "File creation failed!");
			logger.info(created ? "File created successfully!" : "File creation failed!");
			refreshFileList();
		}
	}

	private void deleteSelectedFiles(ActionEvent e) {
		if (confirmAction("Do you want to delete the selected file?")) {
			int selectedRow = fileTable.getSelectedRow();
			if (selectedRow != -1) {
				int fileId = (int) tableModel.getValueAt(selectedRow, 0);
				boolean deleted = businessObj.deleteFile(fileId);
				JOptionPane.showMessageDialog(null,
						deleted ? "File deleted successfully!" : "Failed to delete the selected file.");
				logger.info(deleted ? "File deleted successfully!" : "Failed to delete the selected file.");
				refreshFileList();
			} else {
				JOptionPane.showMessageDialog(null, "Please select a file to delete.");
				logger.warn("Please select a file to delete.");
			}
		}
	}

	private void saveFile() {
		int selectedRow = fileTable.getSelectedRow();
		if (selectedRow != -1) {
			int fileId = (int) tableModel.getValueAt(selectedRow, 0);
			String fileName = (String) tableModel.getValueAt(selectedRow, 1);
			String content = contentTextArea.getText();

			if (content == null || content.trim().isEmpty()) {
				content = "";
			}

			boolean updated = businessObj.updateFile(fileId, fileName, currentPage, content);
			JOptionPane.showMessageDialog(null,
					updated ? "File updated successfully!" : "File update failed. Duplicate file may exist.");
			logger.info(updated ? "File updated successfully!" : "File update failed. Duplicate file may exist.");
			refreshFilePage(fileId, currentPage);
		} else {
			JOptionPane.showMessageDialog(null, "Please select a file to save.");
		}
	}

	private void autoSaveFile() throws RemoteException, InterruptedException {
		int selectedRow = fileTable.getSelectedRow();
		if (selectedRow != -1) {
			String content = contentTextArea.getText();

			if (!businessObj.shouldAutoSave(content)) {
				savingStatusLabel.setVisible(false);
				return;
			}

			int fileId = (int) tableModel.getValueAt(selectedRow, 0);
			String fileName = (String) tableModel.getValueAt(selectedRow, 1);

			if (content == null || content.trim().isEmpty()) {
				content = "";
			}

			boolean updated = businessObj.updateFile(fileId, fileName, currentPage, content);
			if (updated) {
				savingStatusLabel.setVisible(true);
				Thread.sleep(5000);
				savingStatusLabel.setVisible(false);
			}
		} else {
			logger.error("Unable to Auto-Save File: no file selected");
		}
	}

	private void nextPage() {
		if (currentPage < totalPageCount) {
			currentPage++;
			loadPage(currentPage);
		}
	}

	private void previousPage() {
		if (currentPage > 1) {
			currentPage--;
			loadPage(currentPage);
		}
	}

	private void loadPage(int page) {
		String pageContent = "";
		for (int i = 0; i < pages.size(); i++) {
			if (page == pages.get(i).getPageNumber()) {
				pageContent = pages.get(i).getPageContent();
			}
		}
		contentTextArea.setText(pageContent);

		pageCountLabel.setText("Page " + (page) + " of " + totalPageCount);

		nextButton.setEnabled(page < totalPageCount);
		previousButton.setEnabled(page > 1);
	}

	private boolean confirmAction(String message) {
		int option = JOptionPane.showConfirmDialog(null, message, "Confirm Action", JOptionPane.YES_NO_OPTION);
		logger.info(message, "Confirm Action", JOptionPane.YES_NO_OPTION);
		return option == JOptionPane.YES_OPTION;
	}

	private void transliterateContent() {
		String content = contentTextArea.getText();
		int pageId = pages.get(currentPage - 1).getPageId();
		if (content != null && !content.trim().isEmpty()) {
			String transliteratedContent = businessObj.transliterate(pageId, content);
			transliteratedTextArea.setText(transliteratedContent);

			CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
			cardLayout.show(getContentPane(), "TransliterationView");
		} else {
			JOptionPane.showMessageDialog(null, "Content is empty. Please enter text to transliterate.");
			logger.warn("Content is empty. Please enter text to transliterate.");
		}
	}

	private void refreshFilePage(int fileId, int currPage) {
		openEditPanel(fileId);
		loadPage(currPage);
	}

	private void refreshFileList() {
		List<Documents> docs = businessObj.getAllFiles();
		tableModel.setRowCount(0);

		for (Documents doc : docs) {
			Object[] rowData = { doc.getId(), doc.getName(), doc.getLastModified(), doc.getDateCreated() };
			tableModel.addRow(rowData);
		}
	}

}