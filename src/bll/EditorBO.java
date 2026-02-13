package bll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dal.IFacadeDAO;
import dto.Documents;
import dto.Pages;
import pl.EditorPO;

public class EditorBO implements IEditorBO {
	private static final Logger LOGGER = LogManager.getLogger(EditorPO.class);

	private IFacadeDAO db;

	public EditorBO(IFacadeDAO db) {
		this.db = db;
	}

	@Override
	public boolean createFile(String nameOfFile, String content) {
		try {
			return db.createFileInDB(nameOfFile, content);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean updateFile(int id, String fileName, int pageNumber, String content) {
		try {
			return db.updateFileInDB(id, fileName, pageNumber, content);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteFile(int id) {
		try {
			return db.deleteFileInDB(id);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean importTextFiles(File file, String fileName) {
		StringBuilder fileContent = new StringBuilder();
		String fileExtension = getFileExtension(fileName);
		BufferedReader reader;
		try {
			// Support multiple file types including docx, pdf, and common text files
			if (fileExtension.equalsIgnoreCase("txt") || fileExtension.equalsIgnoreCase("md5")) {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					fileContent.append(line).append("\n");
				}
				reader.close();
				return db.createFileInDB(fileName, fileContent.toString());
			} else if (fileExtension.equalsIgnoreCase("docx") || fileExtension.equalsIgnoreCase("doc")) {
				// For now, show user a helpful message for unsupported formats
				LOGGER.warn("Unsupported file format: " + fileExtension + ". Please convert to .txt format.");
				return false;
			} else if (fileExtension.equalsIgnoreCase("pdf")) {
				// For now, show user a helpful message for unsupported formats  
				LOGGER.warn("Unsupported file format: " + fileExtension + ". Please convert to .txt format.");
				return false;
			} else {
				// Try to read as plain text for other extensions
				reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					fileContent.append(line).append("\n");
				}
				reader.close();
				return db.createFileInDB(fileName, fileContent.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public Documents getFile(int id) {
		List<Documents> docs = getAllFiles();
		for (int i = 0; i < docs.size(); i++) {
			if (id == docs.get(i).getId()) {
				return docs.get(i);
			}
		}
		return null;
	}

	@Override
	public String getFileExtension(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf('.');
		return (lastIndexOfDot == -1) ? "" : fileName.substring(lastIndexOfDot + 1);
	}

	@Override
	public List<Documents> getAllFiles() {
		return db.getFilesFromDB();
	}

	@Override
	public String transliterate(int pageId, String arabicText) {
		return db.transliterateInDB(pageId, arabicText);
	}

	@Override
	public List<String> searchKeyword(String keyword) {

		return SearchWord.searchKeyword(keyword, getAllFiles());
	}

	@Override
	public Map<String, String> lemmatizeWords(String text) {
		// TODO Auto-generated method stub
		return db.lemmatizeWords(text);
	}

	@Override
	public Map<String, List<String>> extractPOS(String text) {
		// TODO Auto-generated method stub
		return db.extractPOS(text);
	}

	@Override
	public Map<String, String> extractRoots(String text) {
		// TODO Auto-generated method stub
		return db.extractRoots(text);
	}

	@Override
	public double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
		return db.performTFIDF(unSelectedDocsContent, selectedDocContent);
	}

	@Override
	public Map<String, Double> performPMI(String content) {

		return db.performPMI(content);
	}

	@Override
	public Map<String, Double> performPKL(String content) {
		return db.performPKL(content);
	}

	@Override
	public Map<String, String> stemWords(String text) {
		// TODO Auto-generated method stub
		return db.stemWords(text);
	}

	@Override
	public Map<String, String> segmentWords(String text) {
		// TODO Auto-generated method stub
		return db.segmentWords(text);
	}

}
