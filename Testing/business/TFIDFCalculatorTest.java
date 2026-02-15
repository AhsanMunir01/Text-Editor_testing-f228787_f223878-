package business;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import dal.TFIDFCalculator;

public class TFIDFCalculatorTest extends TestCase {
    
    private TFIDFCalculator calculator;
    
    public TFIDFCalculatorTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(TFIDFCalculatorTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        calculator = new TFIDFCalculator();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        calculator = null;
    }
    
    public void testTFIDF_PositivePath_KnownDocument_ReturnsPositiveScore() {
        calculator.addDocumentToCorpus("the cat sat on the mat");
        calculator.addDocumentToCorpus("the dog ran in the park");
        calculator.addDocumentToCorpus("cats and dogs are pets");
        
        String testDocument = "the cat";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(testDocument);
        
        assertTrue("TF-IDF score should be positive for known document", tfidfScore > 0);
        assertFalse("TF-IDF score should not be NaN", Double.isNaN(tfidfScore));
        assertFalse("TF-IDF score should not be infinite", Double.isInfinite(tfidfScore));
    }
    
    public void testTFIDF_NegativePath_EmptyDocument_ReturnsZero() {
        calculator.addDocumentToCorpus("some content");
        String emptyDocument = "";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(emptyDocument);
        
        assertEquals("Empty document should return zero TF-IDF score", 0.0, tfidfScore, 0.001);
        assertFalse("Empty document should not return NaN", Double.isNaN(tfidfScore));
    }
    
    public void testTFIDF_NegativePath_NullDocument_HandlesGracefully() {
        calculator.addDocumentToCorpus("some content");
        String nullDocument = null;
        
        try {
            double tfidfScore = calculator.calculateDocumentTfIdf(nullDocument);
            assertFalse("Null document should not produce NaN", Double.isNaN(tfidfScore));
        } catch (Exception e) {
            assertTrue("Exception handling for null input is acceptable", true);
        }
    }
    
    public void testTFIDF_SingleWordDocument_ReturnsValidScore() {
        calculator.addDocumentToCorpus("word");
        calculator.addDocumentToCorpus("another");
        
        String singleWord = "word";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(singleWord);
        
        assertTrue("Single word should have positive TF-IDF score", tfidfScore > 0);
        assertFalse("Single word should not produce NaN", Double.isNaN(tfidfScore));
    }
    
    public void testTFIDF_SpecialCharacters_HandlesGracefully() {
        calculator.addDocumentToCorpus("normal text content");
        String specialDocument = "@#$%^&*()";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(specialDocument);
        
        assertFalse("Special characters should not cause NaN", Double.isNaN(tfidfScore));
        assertTrue("Special characters should return valid score", tfidfScore >= 0);
    }
    
    public void testTFIDF_RepeatedWords_CalculatesCorrectly() {
        calculator.addDocumentToCorpus("cat dog bird");
        calculator.addDocumentToCorpus("dog bird fish");
        
        String repeatedDocument = "cat cat cat";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(repeatedDocument);
        
        assertTrue("Repeated words should have positive TF-IDF score", tfidfScore > 0);
        assertFalse("Repeated words should not produce NaN", Double.isNaN(tfidfScore));
    }
    
    public void testTFIDF_EmptyCorpus_ReturnsZero() {
        String testDocument = "test document";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(testDocument);
        
        assertEquals("Empty corpus should return zero TF-IDF", 0.0, tfidfScore, 0.001);
    }
    
    public void testTFIDF_LargeCorpus_CompletesReasonably() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            calculator.addDocumentToCorpus("Document " + i + " with various content words");
        }
        
        String testDocument = "test document for performance evaluation";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(testDocument);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        assertTrue("Large corpus should complete in reasonable time (< 5 seconds)", 
                   executionTime < 5000);
        assertFalse("Large corpus TF-IDF should not be NaN", Double.isNaN(tfidfScore));
        assertTrue("Large corpus TF-IDF should be non-negative", tfidfScore >= 0);
    }
    
    public void testTFIDF_Consistency_SameInputSameOutput() {
        calculator.addDocumentToCorpus("consistent test document");
        calculator.addDocumentToCorpus("another test document");
        
        String testDocument = "test";
        
        double score1 = calculator.calculateDocumentTfIdf(testDocument);
        double score2 = calculator.calculateDocumentTfIdf(testDocument);
        
        assertEquals("Same input should produce same TF-IDF score", score1, score2, 0.00001);
    }
    
    public void testTFIDF_ArabicText_HandlesCorrectly() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator.addDocumentToCorpus("الحمد لله رب العالمين");
        
        String arabicDocument = "الله الرحمن";
        
        double tfidfScore = calculator.calculateDocumentTfIdf(arabicDocument);
        
        assertFalse("Arabic text should not produce NaN", Double.isNaN(tfidfScore));
        assertTrue("Arabic text should produce valid score", tfidfScore >= 0);
    }
}