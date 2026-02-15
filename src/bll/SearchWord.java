package bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dto.Documents;
import dto.Pages;
import pl.EditorPO;

public class SearchWord {
	public static List<String> searchKeyword(String keyword, List<Documents> docs) {
		final Logger LOGGER = LogManager.getLogger(EditorPO.class);
		List<String> getFiles = new ArrayList<>();
		
		// Handle null or empty keyword
		if (keyword == null || keyword.trim().isEmpty()) {
			return getFiles; // Return empty list instead of throwing exception
		}
		
		// Handle short keywords
		if (keyword.length() < 3) {
			return getFiles; // Return empty list instead of throwing exception
		}
		
		// Handle null documents list
		if (docs == null) {
			return getFiles;
		}

		for (Documents doc : docs) {
			if (doc.getPages() == null) {
				continue;
			}
			
			for (Pages page : doc.getPages()) {
				String pageContent = page.getPageContent();
				if (pageContent != null && pageContent.toLowerCase().contains(keyword.toLowerCase())) {
					// Just return the document name for successful matches
					getFiles.add(doc.getName());
					break; // Only add document once even if multiple pages match
				}
			}
		}
		return getFiles;
	}

}
