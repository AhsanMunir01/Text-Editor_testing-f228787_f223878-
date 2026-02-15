package bll;

import java.io.File;
import java.util.List;
import java.util.Map;

import dto.Documents;

public interface IEditorBO {
	boolean createFile(String nameOfFile, String content);

	boolean updateFile(int id, String fileName, int pageNumber, String content);

	boolean deleteFile(int id);

	boolean importTextFiles(File file, String fileName);

	Documents getFile(int id);

	List<Documents> getAllFiles();

	String getFileExtension(String fileName);

	String transliterate(int pageId, String arabicText);

	List<String> searchKeyword(String keyword);

	Map<String, String> lemmatizeWords(String text);

	Map<String, List<String>> extractPOS(String text);

	Map<String, String> extractRoots(String text);

	double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent);

	Map<String, Double> performPMI(String content);

	Map<String, Double> performPKL(String content);

	Map<String, String> stemWords(String text);

	Map<String, String> segmentWords(String text);

	boolean shouldAutoSave(String content);

}
