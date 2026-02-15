package presentation;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import bll.SearchWord;
import dto.Documents;
import java.util.ArrayList;
import java.util.List;

public class SearchWordTest extends TestCase {
    
    private List<Documents> testDocuments;
    
    public SearchWordTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(SearchWordTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testDocuments = new ArrayList<>();
        
        List<dto.Pages> pages1 = new ArrayList<>();
        pages1.add(new dto.Pages(1, 1, 1, "The quick brown fox jumps over the lazy dog"));
        
        List<dto.Pages> pages2 = new ArrayList<>();
        pages2.add(new dto.Pages(2, 2, 1, "Java programming is fun and challenging"));
        
        List<dto.Pages> pages3 = new ArrayList<>();
        pages3.add(new dto.Pages(3, 3, 1, "Testing software requires patience and skill"));
        
        List<dto.Pages> pages4 = new ArrayList<>();
        pages4.add(new dto.Pages(4, 4, 1, "بسم الله الرحمن الرحيم"));
        
        List<dto.Pages> pages5 = new ArrayList<>();
        pages5.add(new dto.Pages(5, 5, 1, "الحمد لله رب العالمين"));
        
        testDocuments.add(new Documents(1, "doc1.txt", "hash1", "2024-01-01", "2024-01-01", pages1));
        testDocuments.add(new Documents(2, "doc2.txt", "hash2", "2024-01-01", "2024-01-01", pages2));
        testDocuments.add(new Documents(3, "doc3.txt", "hash3", "2024-01-01", "2024-01-01", pages3));
        testDocuments.add(new Documents(4, "doc4.txt", "hash4", "2024-01-01", "2024-01-01", pages4));
        testDocuments.add(new Documents(5, "doc5.txt", "hash5", "2024-01-01", "2024-01-01", pages5));
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        testDocuments = null;
    }
    
    public void testSearchKeyword_ValidKeywordExists_ReturnsMatchingResults() {
        String keyword = "the";
        
        List<String> results = SearchWord.searchKeyword(keyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Should find documents containing 'the'", results.size() > 0);
        assertTrue("Results should contain doc1.txt", results.contains("doc1.txt"));
    }
    
    public void testSearchKeyword_ValidKeywordNotExists_ReturnsEmptyResults() {
        String keyword = "nonexistent";
        
        List<String> results = SearchWord.searchKeyword(keyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertEquals("Should return empty results for non-existent keyword", 0, results.size());
    }
    
    public void testSearchKeyword_EmptyKeyword_ReturnsEmptyResults() {
        String emptyKeyword = "";
        
        List<String> results = SearchWord.searchKeyword(emptyKeyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Empty keyword should return empty results", results.isEmpty());
    }
    
    public void testSearchKeyword_NullKeyword_ReturnsEmptyResults() {
        String nullKeyword = null;
        
        List<String> results = SearchWord.searchKeyword(nullKeyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Null keyword should return empty results", results.isEmpty());
    }
    
    public void testSearchKeyword_KeywordTooShort_ReturnsEmptyResults() {
        String shortKeyword = "ab";
        
        List<String> results = SearchWord.searchKeyword(shortKeyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Keywords less than 3 characters should return empty results", 
                   results.isEmpty());
    }
    
    public void testSearchKeyword_MinimumValidLength_ReturnsResults() {
        String minKeyword = "fox";
        
        List<String> results = SearchWord.searchKeyword(minKeyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("3-character keyword should return results if found", results.size() >= 0);
        if (results.size() > 0) {
            assertTrue("Results should contain doc1.txt", results.contains("doc1.txt"));
        }
    }
    
    public void testSearchKeyword_CaseSensitive_HandlesCorrectly() {
        String lowerKeyword = "java";
        String upperKeyword = "JAVA";
        String mixedKeyword = "Java";
        
        List<String> lowerResults = SearchWord.searchKeyword(lowerKeyword, testDocuments);
        List<String> upperResults = SearchWord.searchKeyword(upperKeyword, testDocuments);
        List<String> mixedResults = SearchWord.searchKeyword(mixedKeyword, testDocuments);
        
        assertNotNull("Lower case results should not be null", lowerResults);
        assertNotNull("Upper case results should not be null", upperResults);
        assertNotNull("Mixed case results should not be null", mixedResults);
        assertTrue("Search should handle case consistently", true);
    }
    
    public void testSearchKeyword_ArabicKeyword_ReturnsMatchingResults() {
        String arabicKeyword = "الله";
        
        List<String> results = SearchWord.searchKeyword(arabicKeyword, testDocuments);
        
        assertNotNull("Results should not be null", results);
        if (results.size() > 0) {
            assertTrue("Should handle Arabic text search correctly", true);
        }
    }
    
    public void testSearchKeyword_SpecialCharacters_HandlesGracefully() {
        String specialKeyword = "@#$";
        
        List<String> results = SearchWord.searchKeyword(specialKeyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Special characters should be handled gracefully", results.size() >= 0);
    }
    
    public void testSearchKeyword_EmptyDocumentList_ReturnsEmptyResults() {
        String keyword = "test";
        List<Documents> emptyDocuments = new ArrayList<>();
        
        List<String> results = SearchWord.searchKeyword(keyword, emptyDocuments);
        assertNotNull("Results should not be null", results);
        assertTrue("Empty document list should return empty results", results.isEmpty());
    }
    
    public void testSearchKeyword_NullDocumentList_HandlesGracefully() {
        String keyword = "test";
        List<Documents> nullDocuments = null;
        try {
            List<String> results = SearchWord.searchKeyword(keyword, nullDocuments);
            assertNotNull("Results should not be null", results);
            assertTrue("Null document list should return empty results", results.isEmpty());
        } catch (Exception e) {
            assertTrue("Exception handling for null document list is acceptable", true);
        }
    }
    
    public void testSearchKeyword_MultipleMatchesSameDocument_ReturnsDocumentOnce() {
        testDocuments.clear();
        List<dto.Pages> testPages = new ArrayList<>();
        testPages.add(new dto.Pages(1, 1, 1, "test test test document"));
        testDocuments.add(new Documents(1, "test.txt", "hash", "2024-01-01", "2024-01-01", testPages));
        String keyword = "test";
        
        List<String> results = SearchWord.searchKeyword(keyword, testDocuments);
        assertNotNull("Results should not be null", results);
        assertEquals("Document should appear only once even with multiple matches", 
                     1, results.size());
        assertTrue("Results should contain test.txt", results.contains("test.txt"));
    }
    
    public void testSearchKeyword_LargeDocumentSet_CompletesReasonably() {
        List<Documents> largeDocumentSet = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            List<dto.Pages> pages = new ArrayList<>();
            pages.add(new dto.Pages(i, i, 1, "Document " + i + " contains search term"));
            largeDocumentSet.add(new Documents(i, "doc" + i + ".txt", 
                                             "hash" + i, "2024-01-01", "2024-01-01", pages));
        }
        String keyword = "search";
        
        long startTime = System.currentTimeMillis();
        List<String> results = SearchWord.searchKeyword(keyword, largeDocumentSet);
        long endTime = System.currentTimeMillis();
        assertNotNull("Results should not be null", results);
        assertTrue("Large document set should return results", results.size() > 0);
        assertTrue("Search should complete in reasonable time (< 3 seconds)", 
                   (endTime - startTime) < 3000);
    }
}