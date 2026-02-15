package dal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PreProcessText {
	private static final Set<Character> DIACRITICS = new HashSet<>(
			Arrays.asList('َ', 'ً', 'ُ', 'ٌ', 'ِ', 'ٍ', 'ْ', 'ّ'));

	public static String removeHarakat(String text) {
		StringBuilder result = new StringBuilder();
		for (char ch : text.toCharArray()) {
			if (!DIACRITICS.contains(ch)) {
				result.append(ch);
			}
		}
		return result.toString();
	}

	public static String removeNonArabicCharacters(String text) {
		return text.replaceAll("[^\\p{IsArabic}\\s]", "");
	}

	public static String preprocessText(String text) {
		if (text == null || text.trim().isEmpty()) {
			return text;
		}
		
		// Check if text contains Arabic characters
		boolean hasArabic = text.matches(".*[\\p{IsArabic}].*");
		
		if (hasArabic) {
			// For Arabic text, apply Arabic-specific preprocessing
			text = removeHarakat(text);
			text = removeNonArabicCharacters(text);
		} else {
			// For non-Arabic text (like English), just clean and normalize
			text = text.replaceAll("[^a-zA-Z0-9\\s]", ""); // Remove special characters but keep alphanumeric
			text = text.replaceAll("\\s+", " "); // Normalize whitespace
		}
		
		return text.toLowerCase().trim();
	}
}
