package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	private static Set<String> termSet = new HashSet<>();
	private static Map<Integer, String> docs = new HashMap<>();
	private static Map<Integer, String> queries = new HashMap<>();

	public static List<String> break2Words(String doc) {
		String[] docSplit = doc.trim().split("\\s++");
		return Arrays.asList(docSplit);
	}

	public static int calTf(String term, List<String> documents) {
		int frequency = 0;
		for (String doc : documents) {
			if (doc.equalsIgnoreCase(term)) {
				frequency += 1;
			}
		}
		return frequency;
	}

	// log-frequency
	public static double getTermWeight(double tf) {
		double result = 0;
		if (tf > 0) {
			result = 1 + Math.log10(tf);
		}
		return result;
	}

	public static Map<String, Double> getTf(String documents) {
		Map<String, Double> result = new HashMap<>();
		List<String> docs = break2Words(documents);
		int tf;
		double w_tf = 0.0;
		for (String term : termSet) {
			w_tf = 0;
			tf = calTf(term, docs);
			w_tf = getTermWeight(tf);
			result.put(term, w_tf);
		}
		return result;
	}

	public static Map<String, Integer> getDf() {
		Map<String, Integer> result = new HashMap<>();
		int df = 0;
		for (String term : termSet) {
			df = 0;
			for (Map.Entry<Integer, String> doc : docs.entrySet()) {
				if (doc.getValue().contains(term)) {
					df += 1;
				}
			}
			result.put(term, df);
		}
		return result;
	}

	public static Map<String, Double> getIdf() {
		Map<String, Double> result = new HashMap<>();
		Map<String, Integer> df = getDf();
		double calResult = 0.0;
		int N = docs.size();
		for (Map.Entry<String, Integer> item : df.entrySet()) {
			if (item.getValue() != 0) {
				calResult = Math.log10(N / item.getValue());
			} else {
				calResult = 0;
			}
			result.put(item.getKey(), calResult);
		}
		return result;
	}

	public static Map<Integer, Map<String, Double>> getTf_Idf(Map<Integer, Map<String, Double>> tfSentences, Map<String, Double> idf) {
		Map<Integer, Map<String, Double>> result = new HashMap<>();
		Map<String, Double> idfResult = null;
		double total = 0.0;
		double tf = 0;
		double idfItem = 0;
		for (Map.Entry<Integer, Map<String, Double>> tfSentence : tfSentences.entrySet()) {
			idfResult = new HashMap<>();
			for (Map.Entry<String, Double> item : tfSentence.getValue().entrySet()) {
				tf = item.getValue();
				idfItem = idf.get(item.getKey());
				total = tf * idfItem;
				idfResult.put(item.getKey(), total);
			}
			result.put(tfSentence.getKey(), idfResult);
		}

		return result;
	}

	public static Set<String> retrievAllTerm(List<Map<Integer, String>> documents) {
		Set<String> termSet = new HashSet<>();
		List<String> terms = new ArrayList<>();
		for (Map<Integer, String> d : documents) {
			for (Map.Entry<Integer, String> doc : d.entrySet()) {
				terms = break2Words(doc.getValue());
				termSet.addAll(terms);
			}
		}
		return termSet;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public static Integer findMaxResult(Map<Integer, Double> map) {
		Map.Entry<Integer, Double> maxEntry = null;

		for (Map.Entry<Integer, Double> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		return maxEntry.getKey();
	}

	public static double getNorm(Map<String, Double> tf_idf) {
		double result = 0;
		for (String term : termSet) {
			result += Math.pow(tf_idf.get(term), 2);
		}
		return Math.sqrt(result);
	}

	public static Map<Integer, String> removeStopWord(Map<Integer, String> words) {
		Map<Integer, String> result = new HashMap<>();
		HashSet<String> StopWordsSet = new HashSet<>(Arrays.asList(Utility.stopWords));
		for (Map.Entry<Integer, String> doc : words.entrySet()) {
			String original = doc.getValue();
			ArrayList<String> allWords = Stream.of(original.toLowerCase().trim().split("\\s++"))
					.collect(Collectors.toCollection(ArrayList<String>::new));
			allWords.removeAll(StopWordsSet);
			String results = allWords.stream().collect(Collectors.joining(" "));
			result.put(doc.getKey(), results);
		}
		return result;
	}

	public static Map<Integer, String> removeSpecialCharacter(Map<Integer, String> words) {
		Map<Integer, String> result = new HashMap<>();
		for (Map.Entry<Integer, String> doc : words.entrySet()) {
			String original = doc.getValue();
			String results = original.replaceAll("[^a-zA-Z0-9]", " ");
			result.put(doc.getKey(), results);
		}
		return result;
	}

	public static void main(String[] args) throws Exception {

		List<Map<Integer, String>> docsList = new ArrayList<>();
		docs = Files.readFile(false);
		queries = Files.readFile(true);

		docs = removeSpecialCharacter(docs);
		queries = removeSpecialCharacter(queries);

		docs = removeStopWord(docs);
		queries = removeStopWord(queries);

		docsList.add(docs);

		List<Map<Integer, String>> allDocs = new ArrayList<>();
		allDocs.addAll(docsList);

		termSet = retrievAllTerm(allDocs);
		
		Map<Integer, Map<String, Double>> tfDocs = new HashMap<>();
		for (Map.Entry<Integer, String> doc : docs.entrySet()) {
			tfDocs.put(doc.getKey(), getTf(doc.getValue()));
		}

		Map<Integer, Map<String, Double>> tfQuery = new HashMap<>();
		for (Map.Entry<Integer, String> que : queries.entrySet()) {
			tfQuery.put(que.getKey(), getTf(que.getValue()));
		}

		Map<String, Double> idfResult = getIdf();

		Map<Integer, Map<String, Double>> tf_idfDocs = getTf_Idf(tfDocs, idfResult);
		Map<Integer, Map<String, Double>> tf_idfQuery = getTf_Idf(tfQuery, idfResult);

		Map<Integer, Map<Integer, Double>> queryResult = new HashMap<>();
		Map<Integer, Double> docsResult = new HashMap<>();
		Map<String, Double> tf_idfQ = new HashMap<>();
		Map<String, Double> tf_idfD = new HashMap<>();
		double cosinResult = 0.0;
		double dot = 0;
		double normQ = 0;
		double normD = 0;
		for (Map.Entry<Integer, Map<String, Double>> query : tf_idfQuery.entrySet()) {
			tf_idfQ = query.getValue();
			docsResult = new HashMap<>();
			for (Map.Entry<Integer, Map<String, Double>> doc : tf_idfDocs.entrySet()) {
				tf_idfD = doc.getValue();
				cosinResult = 0.0;
				dot = 0;
				normQ = 0;
				normD = 0;
				for (String term : termSet) {
					dot += tf_idfQ.get(term) * tf_idfD.get(term);
				}
				normQ = getNorm(tf_idfQ);
				normD = getNorm(tf_idfD);
				if (normD != 0 && normQ != 0) {
					cosinResult = dot / normQ * normD;
				} else {
					cosinResult = 0;
				}
				docsResult.put(doc.getKey(), cosinResult);
			}
			docsResult = sortByValue(docsResult);
			queryResult.put(query.getKey(), docsResult);
		}

		Files.writeFile(queryResult);
		System.out.println("queryResult: " + queryResult);
	}

}
