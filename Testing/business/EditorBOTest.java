package business;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import bll.EditorBO;
import dal.IFacadeDAO;
import dto.Documents;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class EditorBOTest extends TestCase {
    
    private EditorBO editorBO;
    private MockFacadeDAO mockDAO;
    
    public EditorBOTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(EditorBOTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        mockDAO = new MockFacadeDAO();
        editorBO = new EditorBO(mockDAO);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        editorBO = null;
        mockDAO = null;
    }
    
    
    public void testCreateFile_ValidContent_ReturnsTrue() {
        String fileName = "test.txt";
        String content = "Test content";
        mockDAO.setCreateFileResult(true);
        
        boolean result = editorBO.createFile(fileName, content);
        
        assertTrue("File creation should succeed with valid content", result);
        assertEquals("DAO should be called with correct filename", fileName, mockDAO.getLastFileName());
        assertEquals("DAO should be called with correct content", content, mockDAO.getLastContent());
    }
    
    public void testCreateFile_EmptyContent_ReturnsTrue() {
        String fileName = "empty.txt";
        String content = "";
        mockDAO.setCreateFileResult(true);
        
        boolean result = editorBO.createFile(fileName, content);
        
        assertTrue("File creation should succeed with empty content", result);
    }
    
    
    public void testCreateFile_NullContent_ReturnsFalse() {
        String fileName = "null.txt";
        String content = null;
        mockDAO.setCreateFileResult(false);
        
        boolean result = editorBO.createFile(fileName, content);
        
        assertFalse("File creation should fail with null content", result);
    }
    
    public void testImportTextFiles_ValidTxtFile_ReturnsTrue() {
        File tempFile = createTempFile("test.txt", "Test content for import");
        mockDAO.setCreateFileResult(true);
        
        boolean result = editorBO.importTextFiles(tempFile, "test.txt");
        
        assertTrue("Import should succeed for valid .txt file", result);
        assertTrue("Content should be read correctly", 
                   mockDAO.getLastContent().contains("Test content for import"));
    }
    
    public void testImportTextFiles_UnsupportedFormat_ReturnsFalse() {
        File tempFile = createTempFile("test.docx", "Document content");
        
        boolean result = editorBO.importTextFiles(tempFile, "test.docx");
        
        assertFalse("Import should fail for unsupported .docx format", result);
    }
    
    public void testGetFileExtension_ValidFileName_ReturnsCorrectExtension() {
        assertEquals("txt", editorBO.getFileExtension("document.txt"));
        assertEquals("pdf", editorBO.getFileExtension("file.pdf"));
        assertEquals("", editorBO.getFileExtension("noextension"));
        assertEquals("docx", editorBO.getFileExtension("complex.name.docx"));
    }
    
    public void testGetFile_ValidId_ReturnsDocument() {
        List<dto.Pages> pages = new ArrayList<>();
        pages.add(new dto.Pages(1, 1, 1, "content"));
        Documents testDoc = new Documents(1, "test.txt", "hash", "2024-01-01", "2024-01-01", pages);
        List<Documents> docs = new ArrayList<>();
        docs.add(testDoc);
        mockDAO.setDocuments(docs);
        
        Documents result = editorBO.getFile(1);
        
        assertNotNull("Should return document for valid ID", result);
        assertEquals("Should return correct document", testDoc.getId(), result.getId());
        assertEquals("Should return correct filename", testDoc.getName(), result.getName());
    }
    
    public void testGetFile_InvalidId_ReturnsNull() {
        List<Documents> docs = new ArrayList<>();
        List<dto.Pages> pages = new ArrayList<>();
        pages.add(new dto.Pages(1, 1, 1, "content"));
        docs.add(new Documents(1, "test.txt", "hash", "2024-01-01", "2024-01-01", pages));
        mockDAO.setDocuments(docs);
        
        Documents result = editorBO.getFile(999);
        
        assertNull("Should return null for invalid ID", result);
    }
    
    public void testGetAllFiles_MultipleFiles_ReturnsAllDocuments() {
        List<Documents> expectedDocs = new ArrayList<>();
        
        List<dto.Pages> pages1 = new ArrayList<>();
        pages1.add(new dto.Pages(1, 1, 1, "content1"));
        expectedDocs.add(new Documents(1, "file1.txt", "hash1", "2024-01-01", "2024-01-01", pages1));
        
        List<dto.Pages> pages2 = new ArrayList<>();
        pages2.add(new dto.Pages(2, 2, 1, "content2"));
        expectedDocs.add(new Documents(2, "file2.txt", "hash2", "2024-01-01", "2024-01-01", pages2));
        
        mockDAO.setDocuments(expectedDocs);
        
        List<Documents> result = editorBO.getAllFiles();
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should return all documents", expectedDocs.size(), result.size());
        assertEquals("Should return correct documents", expectedDocs, result);
    }
    
    public void testUpdateFile_ValidData_ReturnsTrue() {
        int fileId = 1;
        String fileName = "updated.txt";
        int pageNumber = 1;
        String content = "Updated content";
        mockDAO.setUpdateFileResult(true);
        
        boolean result = editorBO.updateFile(fileId, fileName, pageNumber, content);
        
        assertTrue("File update should succeed with valid data", result);
        assertEquals("DAO should be called with correct file ID", fileId, mockDAO.getLastFileId());
        assertEquals("DAO should be called with correct content", content, mockDAO.getLastContent());
    }
    
    public void testDeleteFile_ValidId_ReturnsTrue() {
        int fileId = 1;
        mockDAO.setDeleteFileResult(true);
        
        boolean result = editorBO.deleteFile(fileId);
        
        assertTrue("File deletion should succeed with valid ID", result);
        assertEquals("DAO should be called with correct file ID", fileId, mockDAO.getLastFileId());
    }
    
    private File createTempFile(String fileName, String content) {
        try {
            File tempFile = File.createTempFile("test", ".txt");
            java.io.FileWriter writer = new java.io.FileWriter(tempFile);
            writer.write(content);
            writer.close();
            tempFile.deleteOnExit();
            return tempFile;
        } catch (Exception e) {
            fail("Failed to create temp file: " + e.getMessage());
            return null;
        }
    }
    
    private class MockFacadeDAO implements IFacadeDAO {
        private boolean createFileResult = true;
        private boolean updateFileResult = true;
        private boolean deleteFileResult = true;
        private List<Documents> documents = new ArrayList<>();
        
        private String lastFileName;
        private String lastContent;
        private int lastFileId;
        
        public void setCreateFileResult(boolean result) { this.createFileResult = result; }
        public void setUpdateFileResult(boolean result) { this.updateFileResult = result; }
        public void setDeleteFileResult(boolean result) { this.deleteFileResult = result; }
        public void setDocuments(List<Documents> docs) { this.documents = docs; }
        
        public String getLastFileName() { return lastFileName; }
        public String getLastContent() { return lastContent; }
        public int getLastFileId() { return lastFileId; }
        
        public boolean createFileInDB(String nameOfFile, String content) {
            this.lastFileName = nameOfFile;
            this.lastContent = content;
            return createFileResult;
        }
        
        public boolean updateFileInDB(int id, String fileName, int pageNumber, String content) {
            this.lastFileId = id;
            this.lastFileName = fileName;
            this.lastContent = content;
            return updateFileResult;
        }
        
        public boolean deleteFileInDB(int id) {
            this.lastFileId = id;
            return deleteFileResult;
        }
        
        public List<Documents> getFilesFromDB() {
            return documents;
        }
        
        public String transliterateInDB(int pageId, String arabicText) {
            return "transliterated: " + arabicText;
        }
        
        public Map<String, String> lemmatizeWords(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "lemma");
            return result;
        }
        
        public Map<String, List<String>> extractPOS(String text) {
            Map<String, List<String>> result = new HashMap<>();
            List<String> posTags = new ArrayList<>();
            posTags.add("NOUN");
            result.put("test", posTags);
            return result;
        }
        
        public Map<String, String> extractRoots(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "root");
            return result;
        }
        
        public double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
            return 0.5;
        }
        
        public Map<String, Double> performPMI(String content) {
            Map<String, Double> result = new HashMap<>();
            result.put("test", 0.3);
            return result;
        }
        
        public Map<String, Double> performPKL(String content) {
            Map<String, Double> result = new HashMap<>();
            result.put("test", 0.7);
            return result;
        }
        
        public Map<String, String> stemWords(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "stem");
            return result;
        }
        
        public Map<String, String> segmentWords(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "seg-ment");
            return result;
        }
    }
}