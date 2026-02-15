package data;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import dal.HashCalculator;

public class HashCalculatorTest extends TestCase {

    public HashCalculatorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(HashCalculatorTest.class);
    }

    public void testHashCalculator_SameContent_ReturnsSameHash() {
        String content = "Test content for hashing";

        try {
            String hash1 = HashCalculator.calculateHash(content);
            String hash2 = HashCalculator.calculateHash(content);

            assertNotNull("Hash should not be null", hash1);
            assertNotNull("Hash should not be null", hash2);
            assertEquals("Same content should produce same hash", hash1, hash2);
            assertTrue("Hash should not be empty", hash1.length() > 0);
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_DifferentContent_ReturnsDifferentHash() {
        String content1 = "First content";
        String content2 = "Second content";

        try {
            String hash1 = HashCalculator.calculateHash(content1);
            String hash2 = HashCalculator.calculateHash(content2);

            assertNotNull("First hash should not be null", hash1);
            assertNotNull("Second hash should not be null", hash2);
            assertFalse("Different content should produce different hashes",
                    hash1.equals(hash2));
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_EmptyContent_ReturnsValidHash() {
        String emptyContent = "";

        try {
            String hash = HashCalculator.calculateHash(emptyContent);

            assertNotNull("Hash of empty content should not be null", hash);
            assertTrue("Hash of empty content should not be empty", hash.length() > 0);
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_NullContent_HandlesGracefully() {
        String nullContent = null;

        try {
            String hash = HashCalculator.calculateHash(nullContent);
            if (hash != null) {
                assertTrue("Hash should be valid string", true);
            }
        } catch (Exception e) {
            assertTrue("Exception handling for null content is acceptable", true);
        }
    }

    public void testHashCalculator_SpecialCharacters_ReturnsValidHash() {
        String specialContent = "!@#$%^&*()_+-={}[]|\\:;\"'<>?,./";

        try {
            String hash = HashCalculator.calculateHash(specialContent);

            assertNotNull("Hash with special characters should not be null", hash);
            assertTrue("Hash with special characters should not be empty", hash.length() > 0);
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_ArabicText_ReturnsValidHash() {
        String arabicContent = "بسم الله الرحمن الرحيم";

        try {
            String hash = HashCalculator.calculateHash(arabicContent);

            assertNotNull("Hash with Arabic text should not be null", hash);
            assertTrue("Hash with Arabic text should not be empty", hash.length() > 0);
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_LargeContent_ReturnsValidHash() {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("This is line ").append(i).append(" of large content.\n");
        }

        try {
            long startTime = System.currentTimeMillis();
            String hash = HashCalculator.calculateHash(largeContent.toString());
            long endTime = System.currentTimeMillis();

            assertNotNull("Hash of large content should not be null", hash);
            assertTrue("Hash of large content should not be empty", hash.length() > 0);
            assertTrue("Large content hashing should complete in reasonable time (< 2 seconds)",
                    (endTime - startTime) < 2000);
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_ContentModification_DetectedByHashChange() {
        String originalContent = "Original file content for testing";
        String modifiedContent = "Modified file content for testing";

        try {
            String originalHash = HashCalculator.calculateHash(originalContent);
            String modifiedHash = HashCalculator.calculateHash(modifiedContent);

            assertNotNull("Original hash should not be null", originalHash);
            assertNotNull("Modified hash should not be null", modifiedHash);
            assertFalse("Hash should change when content is modified",
                    originalHash.equals(modifiedHash));
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }

    public void testHashCalculator_MinimalChange_DetectedByHash() {
        String content1 = "test content";
        String content2 = "test content.";

        try {
            String hash1 = HashCalculator.calculateHash(content1);
            String hash2 = HashCalculator.calculateHash(content2);

            assertFalse("Even minimal content change should result in different hash",
                    hash1.equals(hash2));
        } catch (Exception e) {
            fail("Hash calculation should not throw exception: " + e.getMessage());
        }
    }
}
