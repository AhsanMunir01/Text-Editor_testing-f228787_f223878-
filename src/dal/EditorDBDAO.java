package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dto.Documents;
import dto.Pages;
import pl.EditorPO;

public class EditorDBDAO implements IEditorDBDAO {
	private static final Logger LOGGER = LogManager.getLogger(EditorPO.class);
	Connection conn = null;

	public EditorDBDAO() {
		this.conn = DatabaseConnection.getInstance().getConnection();

	}

	@Override
	public synchronized boolean createFileInDB(String nameOfFile, String content) {
		String hash = null;
		List<Pages> pages = null;

		String insertQuery = "INSERT INTO files (fileName, fileHash) VALUES (?, ?)";
		String pageQuery = "INSERT INTO pages (fileId, pageNumber, pageContent) VALUES (?, ?, ?)";
		String transliterateQuery = "INSERT INTO transliteratedpages (pageId, transliteratedText) VALUES (?, ?)";
		;
		String posQuery = "INSERT INTO pos (pageId, word, pos) VALUES (?, ?, ?)";
		String lemmaQuery = "INSERT INTO lemmatization (pageId, word, lemma) VALUES (?, ?, ?)";
		String rootQuery = "INSERT INTO rootextraction (pageId, word, root) VALUES (?, ?, ?)";
		String stemQuery = "INSERT INTO stemmation (pageId, word, stem) VALUES (?, ?, ?)";
		String segmentQuery = "INSERT INTO wordsegementation (pageId, word, segment) VALUES (?, ?, ?)";
		String tfidfQuery = "INSERT INTO tfidf (fileId, tfidfScore) VALUES (?, ?)";
		String pklQuery = "INSERT INTO pkl (pageId, word, pklScore) VALUES (?, ?, ?)";
		String pmiQuery = "INSERT INTO pmi (pageId, word, pmiScore) VALUES (?, ?, ?)";
		Map<String, String> analyticsMap = new HashMap<>();
		Map<String, Double> scoreMap = new HashMap<>();

//		PreparedStatement fileStmt = null;
//		PreparedStatement transliteratetStmt = null;
//		PreparedStatement posStmt = null;
//		PreparedStatement lemmaStmt = null;
//		PreparedStatement rootStmt = null;
//		PreparedStatement segmentStmt = null;
//		PreparedStatement stemStmt = null;
//		PreparedStatement pklStmt = null;
//		PreparedStatement pmiStmt = null;
//		PreparedStatement tfidfStmt = null;
		try {

			hash = HashCalculator.calculateHash(content);
			pages = PaginationDAO.paginate(content);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}

		try (PreparedStatement fileStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement pageStmt = conn.prepareStatement(pageQuery, PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement transliteratetStmt = conn.prepareStatement(transliterateQuery);
				PreparedStatement posStmt = conn.prepareStatement(posQuery);
				PreparedStatement lemmaStmt = conn.prepareStatement(lemmaQuery);
				PreparedStatement rootStmt = conn.prepareStatement(rootQuery);
				PreparedStatement segmentStmt = conn.prepareStatement(segmentQuery);
				PreparedStatement stemStmt = conn.prepareStatement(stemQuery);
				PreparedStatement pklStmt = conn.prepareStatement(pklQuery);
				PreparedStatement pmiStmt = conn.prepareStatement(pmiQuery);
				PreparedStatement tfidfStmt = conn.prepareStatement(tfidfQuery)) {
			conn = DatabaseConnection.getInstance().getConnection();
			double tfidf = performTFIDF(getAllExistingFilesContent(conn), content);
			conn.setAutoCommit(false);

			// Insert into files table
//			fileStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			fileStmt.setString(1, nameOfFile);
			fileStmt.setString(2, hash);
			fileStmt.executeUpdate();

			ResultSet fileRS = fileStmt.getGeneratedKeys();
			fileRS.next();
			int fileID = fileRS.getInt(1);

			for (Pages page : pages) {
				// Insert into pages table
//				pageStmt = conn.prepareStatement(pageQuery, PreparedStatement.RETURN_GENERATED_KEYS);
				pageStmt.setInt(1, fileID);
				pageStmt.setInt(2, page.getPageNumber());
				pageStmt.setString(3, page.getPageContent());
				pageStmt.executeUpdate();

				ResultSet pageRS = pageStmt.getGeneratedKeys();
				pageRS.next();
				int pageId = pageRS.getInt(1);

				// Transliteration
				String transliteratedText = Transliteration.transliterate(page.getPageContent());
//				transliteratetStmt = conn.prepareStatement(transliterateQuery);
				transliteratetStmt.setInt(1, pageId);
				transliteratetStmt.setString(2, transliteratedText);
				transliteratetStmt.executeUpdate();

				// POS Tagging
				Map<String, List<String>> posTagsMap = POSTagger.extractPOS(page.getPageContent());

//				posStmt = conn.prepareStatement(posQuery);

				for (Map.Entry<String, List<String>> entry : posTagsMap.entrySet()) {
					String word = entry.getKey();
					List<String> posTags = entry.getValue();
					String posTagString = String.join("|", posTags);

					posStmt.setInt(1, pageId);
					posStmt.setString(2, word);
					posStmt.setString(3, posTagString);
					posStmt.addBatch();
				}
				posStmt.executeBatch();

				analyticsMap = Lemmatization.lemmatizeWords(page.getPageContent());

//				lemmaStmt = conn.prepareStatement(lemmaQuery);

				for (Map.Entry<String, String> entry : analyticsMap.entrySet()) {
					String word = entry.getKey();
					String lemma = entry.getValue();
					String lemmaString = String.join("|", lemma);

					lemmaStmt.setInt(1, pageId);
					lemmaStmt.setString(2, word);
					lemmaStmt.setString(3, lemmaString);
					lemmaStmt.addBatch();
				}
				lemmaStmt.executeBatch();
				analyticsMap = RootExtraction.extractRoots(page.getPageContent());

//				rootStmt = conn.prepareStatement(rootQuery);

				for (Map.Entry<String, String> entry : analyticsMap.entrySet()) {
					String word = entry.getKey();
					String root = entry.getValue();
					String rootString = String.join("|", root);

					rootStmt.setInt(1, pageId);
					rootStmt.setString(2, word);
					rootStmt.setString(3, rootString);
					rootStmt.addBatch();
				}
				rootStmt.executeBatch();

				analyticsMap = WordSegmentation.extractSegments(page.getPageContent());
//				segmentStmt = conn.prepareStatement(segmentQuery);

				for (Map.Entry<String, String> entry : analyticsMap.entrySet()) {
					String word = entry.getKey();
					String segment = entry.getValue();
					String segmentString = String.join("|", segment);

					segmentStmt.setInt(1, pageId);
					segmentStmt.setString(2, word);
					segmentStmt.setString(3, segmentString);
					segmentStmt.addBatch();
				}
				segmentStmt.executeBatch();

				analyticsMap = Stemmation.stemWords(page.getPageContent());
//				stemStmt = conn.prepareStatement(stemQuery);

				for (Map.Entry<String, String> entry : analyticsMap.entrySet()) {
					String word = entry.getKey();
					String stem = entry.getValue();
					String stemString = String.join("|", stem);

					stemStmt.setInt(1, pageId);
					stemStmt.setString(2, word);
					stemStmt.setString(3, stemString);
					stemStmt.addBatch();
				}
				stemStmt.executeBatch();

				scoreMap = performPKL(page.getPageContent());
//				pklStmt = conn.prepareStatement(pklQuery);

				for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {
					String word = entry.getKey();
					Double pkl = entry.getValue();

					pklStmt.setInt(1, pageId);
					pklStmt.setString(2, word);
					pklStmt.setDouble(3, pkl);
					pklStmt.addBatch();
				}
				pklStmt.executeBatch();

				scoreMap = performPMI(page.getPageContent());
//				pmiStmt = conn.prepareStatement(pmiQuery);

				for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {
					String word = entry.getKey();
					Double pmi = entry.getValue();

					pmiStmt.setInt(1, pageId);
					pmiStmt.setString(2, word);
					pmiStmt.setDouble(3, pmi);
					pmiStmt.addBatch();
				}
				pmiStmt.executeBatch();

			}

//			tfidfStmt = conn.prepareStatement(tfidfQuery);
			if (Double.isNaN(tfidf)) tfidf = 0.0;
			tfidfStmt.setInt(1, fileID);
			tfidfStmt.setDouble(2, tfidf);
			tfidfStmt.executeUpdate();

			conn.commit();
			return true;

		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				LOGGER.error(e1.getMessage());
			}
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}

		return false;
	}

	@Override
	public boolean updateFileInDB(int fileId, String fileName, int pageNumber, String content) {

		PreparedStatement fileStmt = null;
		PreparedStatement pageStmt = null;
		// PreparedStatement transliterateStmt = null;
		PreparedStatement posStmt = null;
		PreparedStatement lemmaStmt = null;
		PreparedStatement rootStmt = null;
		PreparedStatement segmentStmt = null;
		PreparedStatement stemStmt = null;
		PreparedStatement pklStmt = null;
		PreparedStatement pmiStmt = null;
		PreparedStatement tfidfStmt = null;

		try {

			conn.setAutoCommit(false);

			// Update file information
			String fileQuery = "UPDATE files SET fileName = ?, lastModified = CURRENT_TIMESTAMP() WHERE fileId = ?";
			fileStmt = conn.prepareStatement(fileQuery);
			fileStmt.setString(1, fileName);
			fileStmt.setInt(2, fileId);
			fileStmt.executeUpdate();

			// Update page content
			String pageQuery = "UPDATE pages SET pageContent = ? WHERE fileId = ? AND pageNumber = ?";
			pageStmt = conn.prepareStatement(pageQuery);
			pageStmt.setString(1, content);
			pageStmt.setInt(2, fileId);
			pageStmt.setInt(3, pageNumber);
			pageStmt.executeUpdate();

			// Get the pageId of the updated page
			String pageIdQuery = "SELECT pageId FROM pages WHERE fileId = ? AND pageNumber = ?";
			PreparedStatement pageIdStmt = conn.prepareStatement(pageIdQuery);
			pageIdStmt.setInt(1, fileId);
			pageIdStmt.setInt(2, pageNumber);
			ResultSet pageIdRS = pageIdStmt.executeQuery();
			if (!pageIdRS.next()) {
				throw new SQLException("Page not found for the given fileId and pageNumber");
			}
			int pageId = pageIdRS.getInt("pageId");

//	        // Update transliteration
//	        String transliteratedText = Transliteration.transliterate(content);
//	        String transliterateQuery = "UPDATE transliteratedpages SET transliteratedText = ? WHERE pageId = ?";
//	        transliterateStmt = conn.prepareStatement(transliterateQuery);
//	        transliterateStmt.setString(1, transliteratedText);
//	        transliterateStmt.setInt(2, pageId);
//	        transliterateStmt.executeUpdate();

			// Update POS tagging
			Map<String, List<String>> posTagsMap = POSTagger.extractPOS(content);
			String deletePosQuery = "DELETE FROM pos WHERE pageId = ?";
			posStmt = conn.prepareStatement(deletePosQuery);
			posStmt.setInt(1, pageId);
			posStmt.executeUpdate();

			String insertPosQuery = "INSERT INTO pos (pageId, word, pos) VALUES (?, ?, ?)";
			posStmt = conn.prepareStatement(insertPosQuery);
			Map<String, Double> scoreMap = performPMI(content);

			for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {
			    String word = entry.getKey();
			    Double value = entry.getValue();
			    if (value == null || value.isNaN()) {
			        value = 0.0;
			    }

			    pmiStmt.setInt(1, pageId);
			    pmiStmt.setString(2, word);
			    pmiStmt.setDouble(3, value);
			    pmiStmt.addBatch();
			}

			posStmt.executeBatch();

			// Update lemmatization
			Map<String, String> lemmaMap = Lemmatization.lemmatizeWords(content);
			String deleteLemmaQuery = "DELETE FROM lemmatization WHERE pageId = ?";
			lemmaStmt = conn.prepareStatement(deleteLemmaQuery);
			lemmaStmt.setInt(1, pageId);
			lemmaStmt.executeUpdate();

			String insertLemmaQuery = "INSERT INTO lemmatization (pageId, word, lemma) VALUES (?, ?, ?)";
			lemmaStmt = conn.prepareStatement(insertLemmaQuery);
			for (Map.Entry<String, String> entry : lemmaMap.entrySet()) {
				lemmaStmt.setInt(1, pageId);
				lemmaStmt.setString(2, entry.getKey());
				lemmaStmt.setString(3, entry.getValue());
				lemmaStmt.addBatch();
			}
			lemmaStmt.executeBatch();

			// Update root extraction
			Map<String, String> rootMap = RootExtraction.extractRoots(content);
			String deleteRootQuery = "DELETE FROM rootextraction WHERE pageId = ?";
			rootStmt = conn.prepareStatement(deleteRootQuery);
			rootStmt.setInt(1, pageId);
			rootStmt.executeUpdate();

			String insertRootQuery = "INSERT INTO rootextraction (pageId, word, root) VALUES (?, ?, ?)";
			rootStmt = conn.prepareStatement(insertRootQuery);
			for (Map.Entry<String, String> entry : rootMap.entrySet()) {
				rootStmt.setInt(1, pageId);
				rootStmt.setString(2, entry.getKey());
				rootStmt.setString(3, entry.getValue());
				rootStmt.addBatch();
			}
			rootStmt.executeBatch();

			// Update word segmentation
			Map<String, String> segmentMap = WordSegmentation.extractSegments(content);
			String deleteSegmentQuery = "DELETE FROM wordsegementation WHERE pageId = ?";
			segmentStmt = conn.prepareStatement(deleteSegmentQuery);
			segmentStmt.setInt(1, pageId);
			segmentStmt.executeUpdate();

			String insertSegmentQuery = "INSERT INTO wordsegementation (pageId, word, segment) VALUES (?, ?, ?)";
			segmentStmt = conn.prepareStatement(insertSegmentQuery);
			for (Map.Entry<String, String> entry : segmentMap.entrySet()) {
				segmentStmt.setInt(1, pageId);
				segmentStmt.setString(2, entry.getKey());
				segmentStmt.setString(3, entry.getValue());
				segmentStmt.addBatch();
			}
			segmentStmt.executeBatch();

			// Update stemming
			Map<String, String> stemMap = Stemmation.stemWords(content);
			String deleteStemQuery = "DELETE FROM stemmation WHERE pageId = ?";
			stemStmt = conn.prepareStatement(deleteStemQuery);
			stemStmt.setInt(1, pageId);
			stemStmt.executeUpdate();

			String insertStemQuery = "INSERT INTO stemmation (pageId, word, stem) VALUES (?, ?, ?)";
			stemStmt = conn.prepareStatement(insertStemQuery);
			for (Map.Entry<String, String> entry : stemMap.entrySet()) {
				stemStmt.setInt(1, pageId);
				stemStmt.setString(2, entry.getKey());
				stemStmt.setString(3, entry.getValue());
				stemStmt.addBatch();
			}
			stemStmt.executeBatch();

			// Update PKL
			Map<String, Double> pklMap = performPKL(content);
			String deletePklQuery = "DELETE FROM pkl WHERE pageId = ?";
			pklStmt = conn.prepareStatement(deletePklQuery);
			pklStmt.setInt(1, pageId);
			pklStmt.executeUpdate();

			String insertPklQuery = "INSERT INTO pkl (pageId, word, pklScore) VALUES (?, ?, ?)";
			pklStmt = conn.prepareStatement(insertPklQuery);
			for (Map.Entry<String, Double> entry : pklMap.entrySet()) {
				pklStmt.setInt(1, pageId);
				pklStmt.setString(2, entry.getKey());
				pklStmt.setDouble(3, entry.getValue());
				pklStmt.addBatch();
			}
			pklStmt.executeBatch();

			// Update PMI
			Map<String, Double> pmiMap = performPMI(content);
			String deletePmiQuery = "DELETE FROM pmi WHERE pageId = ?";
			pmiStmt = conn.prepareStatement(deletePmiQuery);
			pmiStmt.setInt(1, pageId);
			pmiStmt.executeUpdate();

			String insertPmiQuery = "INSERT INTO pmi (pageId, word, pmiScore) VALUES (?, ?, ?)";
			pmiStmt = conn.prepareStatement(insertPmiQuery);
			for (Map.Entry<String, Double> entry : pmiMap.entrySet()) {
				pmiStmt.setInt(1, pageId);
				pmiStmt.setString(2, entry.getKey());
				pmiStmt.setDouble(3, entry.getValue());
				pmiStmt.addBatch();
			}
			pmiStmt.executeBatch();

			// Update TF-IDF
			double tfidf = performTFIDF(getAllExistingFilesContent(conn), content);
			String tfidfQuery = "UPDATE tfidf SET tfidfScore = ? WHERE fileId = ?";
			tfidfStmt = conn.prepareStatement(tfidfQuery);
			if (Double.isNaN(tfidf)) tfidf = 0.0;
			tfidfStmt.setDouble(1, tfidf);
			tfidfStmt.setInt(2, fileId);
			tfidfStmt.executeUpdate();

			conn.commit();
			return true;
		} catch (Exception e) {
			try {

				conn.rollback();

			} catch (SQLException rollbackEx) {
				rollbackEx.printStackTrace();
				LOGGER.error(rollbackEx.getMessage());
			}
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteFileInDB(int id) {
		String query = "DELETE FROM FILES WHERE fileId = ?";
		try (PreparedStatement fileStmt = conn.prepareStatement(query)) {

			fileStmt.setInt(1, id);
			int rowsAffected = fileStmt.executeUpdate();

			return rowsAffected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return false;
		}
	}
//	public boolean deleteFileInDB(int id) {
//		String query = "DELETE FROM FILES WHERE fileId = ?";
//
//		PreparedStatement fileStmt = null;
//
//		try {
//
//			fileStmt = conn.prepareStatement(query);
//
//			fileStmt.setInt(1, id);
//			fileStmt.executeUpdate();
//
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	@Override
	public List<Documents> getFilesFromDB() {
		List<Documents> documents = new ArrayList<>();

		PreparedStatement stmt = null;
		String query = null;
		ResultSet rs;

		try {

			conn.setAutoCommit(false);
			query = "SELECT fileId, fileName, filehash, dateCreated, lastModified FROM files";
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("fileId");
				String name = rs.getString("fileName");
				String hash = rs.getString("fileHash");
				String lastModified = rs.getString("lastModified");
				String dateCreated = rs.getString("dateCreated");

				String query1 = "SELECT pageId, fileId, pageNumber, pageContent FROM pages where fileId = ?";
				PreparedStatement stmt1 = conn.prepareStatement(query1);
				stmt1.setInt(1, id);
				ResultSet rs1 = stmt1.executeQuery();
				List<Pages> pages = new ArrayList<Pages>();

				while (rs1.next()) {
					pages.add(new Pages(rs1.getInt("pageId"), rs1.getInt("fileId"), rs1.getInt("pageNumber"),
							rs1.getString("pageContent")));
				}

				documents.add(new Documents(id, name, hash, lastModified, dateCreated, pages));
			}
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}
		return documents;
	}

	@Override
	public String transliterateInDB(int pageId, String arabicText) {
		String content;
		String deleteQuery = "DELETE FROM transliteratedpages WHERE pageId = ?";
		String insertQuery = "INSERT INTO transliteratedpages (pageId, transliteratedText) VALUES (?, ?)";

		try {
			// Ensure the transliteration result is valid
			content = Transliteration.transliterate(arabicText);

			// Begin transaction
			conn.setAutoCommit(false);

			// Delete existing entries
			try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
				deleteStmt.setInt(1, pageId);
				deleteStmt.executeUpdate();
			}

			// Insert new transliterated content
			try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
				insertStmt.setInt(1, pageId);
				insertStmt.setString(2, content);
				insertStmt.executeUpdate();
			}

			// Commit transaction
			conn.commit();
			return content;

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			try {

				conn.rollback();

			} catch (SQLException rollbackEx) {
				rollbackEx.printStackTrace();
				LOGGER.error(rollbackEx.getMessage());
			}
			return null;
		}
	}

	private List<String> getAllExistingFilesContent(Connection conn) throws SQLException {
		List<String> allFilesContent = new ArrayList<>();
		String query = "SELECT f.fileId, GROUP_CONCAT(p.pageContent ORDER BY p.pageNumber SEPARATOR '') AS fileContent "
				+ "FROM files f " + "JOIN pages p ON f.fileId = p.fileId " + "GROUP BY f.fileId";

		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				allFilesContent.add(rs.getString("fileContent"));
			}
		}
		return allFilesContent;
	}

	@Override
	public synchronized Map<String, String> lemmatizeWords(String text) {
		// TODO Auto-generated method stub
		return Lemmatization.lemmatizeWords(PreProcessText.preprocessText(text));
	}

	@Override
	public synchronized Map<String, List<String>> extractPOS(String text) {
		// TODO Auto-generated method stub
		return POSTagger.extractPOS(PreProcessText.preprocessText(text));
	}

	@Override
	public synchronized Map<String, String> extractRoots(String text) {
		// TODO Auto-generated method stub
		return RootExtraction.extractRoots(PreProcessText.preprocessText(text));
	}

	@Override
	public synchronized double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
		TFIDFCalculator tfidf = new TFIDFCalculator();
		for (String unSelectedDocContent : unSelectedDocsContent) {
			tfidf.addDocumentToCorpus(unSelectedDocContent);
		}
		return tfidf.calculateDocumentTfIdf(selectedDocContent);
	}

	@Override
	public synchronized Map<String, Double> performPMI(String content) {
		// TODO Auto-generated method stub
		PMICalculator pmi = new PMICalculator(content);
		Map<String, Double> pmiScores = pmi.calculatePMIForAllBigrams();

		return pmiScores;
	}

	@Override
	public synchronized Map<String, Double> performPKL(String content) {
		// TODO Auto-generated method stub
		PKLCalculator pkl = new PKLCalculator(content);
		Map<String, Double> pklScores = pkl.calculatePKLForAllWords();
		return pklScores;
	}

	@Override
	public synchronized Map<String, String> stemWords(String text) {
		// TODO Auto-generated method stub
		return Stemmation.stemWords(PreProcessText.preprocessText(text));
	}

	@Override
	public synchronized Map<String, String> segmentWords(String text) {
		// TODO Auto-generated method stub
		return WordSegmentation.extractSegments(PreProcessText.preprocessText(text));
	}

}