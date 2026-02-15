package dal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TFIDFCalculator {

	private List<String> corpus = new ArrayList<>();

	public void addDocumentToCorpus(String document) {
		if (document != null) {
			String preprocessed = PreProcessText.preprocessText(document);
			if (preprocessed != null && !preprocessed.trim().isEmpty()) {
				corpus.add(preprocessed);
			}
		}
	}

	public double calculateDocumentTfIdf(String document) {
		// Handle null or empty document
		if (document == null || document.trim().isEmpty()) {
			return 0.0;
		}
		
		// Handle empty corpus
		if (corpus.isEmpty()) {
			return 0.0;
		}
		
		String preprocessedDoc = PreProcessText.preprocessText(document);
		
		// Handle empty preprocessed document
		if (preprocessedDoc == null || preprocessedDoc.trim().isEmpty()) {
			return 0.0;
		}
		
		String[] words = preprocessedDoc.split("\\s+");
		
		// Handle document with no words after preprocessing
		if (words.length == 0) {
			return 0.0;
		}
		
		List<String> wordList = Arrays.asList(words);

		Map<String, Double> tf = calculateTermFrequency(wordList);
		Map<String, Double> idf = calculateInverseDocumentFrequency();

		double totalTfIdf = 0.0;
		for (String word : tf.keySet()) {
			double tfValue = tf.get(word);
			double idfValue = idf.getOrDefault(word, Math.log(corpus.size() + 1));
			totalTfIdf += tfValue * idfValue;
		}

		return totalTfIdf / wordList.size();
	}

	private Map<String, Double> calculateTermFrequency(List<String> wordList) {
		Map<String, Double> tf = new HashMap<>();
		
		// Handle empty word list
		if (wordList == null || wordList.isEmpty()) {
			return tf;
		}
		
		double totalWords = wordList.size();

		for (String word : wordList) {
			if (word != null && !word.trim().isEmpty()) {
				tf.put(word, tf.getOrDefault(word, 0.0) + 1);
			}
		}

		for (String word : tf.keySet()) {
			tf.put(word, tf.get(word) / totalWords);
		}

		return tf;
	}

	private Map<String, Double> calculateInverseDocumentFrequency() {
		Map<String, Double> idf = new HashMap<>();
		int totalDocs = corpus.size();
		
		// Handle empty corpus
		if (totalDocs == 0) {
			return idf;
		}

		for (String doc : corpus) {
			if (doc != null && !doc.trim().isEmpty()) {
				Set<String> uniqueWords = Arrays.stream(doc.split("\\s+"))
						.filter(word -> word != null && !word.trim().isEmpty())
						.collect(Collectors.toSet());
				for (String word : uniqueWords) {
					idf.put(word, idf.getOrDefault(word, 0.0) + 1);
				}
			}
		}

		for (String word : idf.keySet()) {
			double docFreq = idf.get(word);
			if (docFreq > 0) {
				idf.put(word, Math.log((double) totalDocs / docFreq));
			} else {
				idf.put(word, 0.0);
			}
		}

		return idf;
	}

//    public static void main(String[] args) {
//        TFIDF calculator = new TFIDF();
//        
//        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
//        calculator.addDocumentToCorpus("ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَـٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍۢ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَـٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ");
//        calculator.addDocumentToCorpus("مرحبًا، اسمي (اسمك). ولدت ونشأت في مدينة (مدينتك) الجميلة. أنا طالب/ة في (اسم مدرستك أو جامعتك) وأدرس (تخصصك الدراسي). منذ صغري وأنا أحب (أحد اهتماماتك أو هواياتك). لقد أمضيت الكثير من الوقت في تعلم وممارسة هذه الهواية لأنها تمنحني شعورًا بالراحة والسعادة. "
//                + "بالإضافة إلى الدراسة، أحب قضاء وقت فراغي في القيام بأشياء أخرى مثل (أحد الأنشطة التي تستمتع بها). هذا النشاط ليس فقط ممتعًا بالنسبة لي ولكنه أيضًا يساعدني في تنمية مهاراتي مثل (اذكر المهارات التي طورتها من خلال هذا النشاط). "
//                + "أحب أيضًا القراءة والمطالعة. الكتب تمثل لي نافذة للعالم، وأنا أحب قراءة أنواع مختلفة من الكتب مثل (نوع الكتب التي تقرأها). كل كتاب أقرأه يساعدني على التعرف على أشياء جديدة وتوسيع آفاقي. "
//                + "أسرتي هي جزء مهم من حياتي. نحن أسرة مترابطة وندعم بعضنا البعض دائمًا. لقد تعلمت من عائلتي قيمًا مهمة مثل الاحترام، والتفاني، والعمل الجاد. هؤلاء القيم تساعدني في كل ما أقوم به في حياتي. "
//                + "أطمح في المستقبل إلى (اذكر أحلامك وطموحاتك المستقبلية). أنا أؤمن أن العمل الجاد والتفاني سيجلب لي النجاح في كل ما أسعى إليه. أنا متحمس/ة لتحقيق أحلامي وأهدافي وآمل أن أترك بصمة إيجابية في العالم.");
//
//        String document = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ " +
//        	    "الرَّحْمَٰنُ " +
//        	    "عَلَّمَ الْقُرْآنَ " +
//        	    "خَلَقَ الْإِنسَانَ " +
//        	    "عَلَّمَهُ الْبَيَانَ " +
//        	    "الشَّمْسُ وَالْقَمَرُ بِحُسْبَانٍ " +
//        	    "وَالنَّجْمُ وَالشَّجَرُ يَسْجُدَانِ " +
//        	    "وَالسَّمَاءَ رَفَعَهَا وَوَضَعَ الْمِيزَانَ " +
//        	    "أَلَّا تَطْغَوْا فِي الْمِيزَانِ " +
//        	    "وَأَقِيمُوا الْوَزْنَ بِالْقِسْطِ وَلَا تُخْسِرُوا الْمِيزَانَ " +
//        	    "وَالْأَرْضَ وَضَعَهَا لِلْأَنَامِ " +
//        	    "فِيهَا فَاكِهَةٌ وَالنَّخْلُ ذَاتُ الْأَكْمَامِ " +
//        	    "وَالْحَبُّ ذُو الْعَصْفِ وَالرَّيْحَانُ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "خَلَقَ الْإِنسَانَ مِنْ صَلْصَالٍ كَالْفَخَّارِ " +
//        	    "وَخَلَقَ الْجَانَّ مِنْ مَارِجٍ مِنْ نَارٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "رَبُّ الْمَشْرِقَيْنِ وَرَبُّ الْمَغْرِبَيْنِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "مَرَجَ الْبَحْرَيْنِ يَلْتَقِيَانِ " +
//        	    "بَيْنَهُمَا بَرْزَخٌ لَا يَبْغِيَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "يَخْرُجُ مِنْهُمَا اللُّؤْلُؤُ وَالْمَرْجَانُ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "وَلَهُ الْجَوَارِ الْمُنْشَآتُ فِي الْبَحْرِ كَالْأَعْلَامِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "كُلُّ مَنْ عَلَيْهَا فَانٍ " +
//        	    "وَيَبْقَىٰ وَجْهُ رَبِّكَ ذُو الْجَلَالِ وَالْإِكْرَامِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "يَسْأَلُهُ مَنْ فِي السَّمَاوَاتِ وَالْأَرْضِ كُلَّ يَوْمٍ هُوَ فِي شَأْنٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "سَنَفْرُغُ لَكُمْ أَيُّهَ الثَّقَلَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "يَا مَعْشَرَ الْجِنِّ وَالْإِنسِ إِنِ اسْتَطَعْتُمْ أَنْ تَنْفُذُوا مِنْ أَقْطَارِ السَّمَاوَاتِ وَالْأَرْضِ فَانْفُذُوا لَا تَنْفُذُونَ إِلَّا بِسُلْطَانٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "يُرْسَلُ عَلَيْكُمَا شُوَاظٌ مِنْ نَارٍ وَنُحَاسٌ فَلَا تَنْتَصِرَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "فَإِذَا انْشَقَّتِ السَّمَاءُ فَكَانَتْ وَرْدَةً كَالدِّهَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "فَيَوْمَئِذٍ لَا يُسْأَلُ عَنْ ذَنْبِهِ إِنْسٌ وَلَا جَانٌّ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "يُعْرَفُ الْمُجْرِمُونَ بِسِيمَاهُمْ فَيُؤْخَذُ بِالنَّوَاصِي وَالْأَقْدَامِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "هَذِهِ جَهَنَّمُ الَّتِي يُكَذِّبُ بِهَا الْمُجْرِمُونَ " +
//        	    "يَطُوفُونَ بَيْنَهَا وَبَيْنَ حَمِيمٍ آنٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "وَلِمَنْ خَافَ مَقَامَ رَبِّهِ جَنَّتَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "ذَوَاتَا أَفْنَانٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "فِيهِمَا عَيْنَانِ تَجْرِيَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "فِيهِمَا مِنْ كُلِّ فَاكِهَةٍ زَوْجَانِ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "مُتَّكِئِينَ عَلَىٰ فُرُشٍ بَطَائِنُهَا مِنْ إِسْتَبْرَقٍ وَجَنَى الْجَنَّتَيْنِ دَانٍ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "فِيهِنَّ قَاصِرَاتُ الطَّرْفِ لَمْ يَطْمِثْهُنَّ إِنْسٌ قَبْلَهُمْ وَلَا جَانٌّ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "كَأَنَّهُنَّ الْيَاقُوتُ وَالْمَرْجَانُ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ " +
//        	    "هَلْ جَزَاءُ الْإِحْسَانِ إِلَّا الْإِحْسَانُ " +
//        	    "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ";
//
//
//
//        double documentTfIdf = calculator.calculateDocumentTfIdf(document);
//
//        System.out.println("TF-IDF Score for the document:");
//        System.out.println(documentTfIdf);
//    }
}
