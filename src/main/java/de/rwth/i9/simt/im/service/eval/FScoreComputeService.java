package de.rwth.i9.simt.im.service.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.rwth.i9.simt.im.model.eval.Publication;
import de.rwth.i9.simt.im.model.eval.PublicationKeywords;
import de.rwth.i9.simt.im.repository.eval.PublicationKeywordsRepository;
import de.rwth.i9.simt.im.repository.eval.PublicationRepository;
import de.rwth.i9.simt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.simt.ke.lib.util.WordCount;

@Service("fScoreComputeService")
public class FScoreComputeService {
	private static final Logger log = LoggerFactory.getLogger(FScoreComputeService.class);

	@Autowired
	PublicationKeywordsRepository publicationKeywordsRepository;
	@Autowired
	PublicationRepository publicationRepository;

	int[] numKeywordsArray = new int[] { 5, 10, 15, 20 };

	public void computeFScore() {
		int pageSize = 10;
		List<PublicationKeywords> pubKws = null;
		long count = publicationRepository.count();
		KeyphraseExtractionAlgorithm[] algs = new KeyphraseExtractionAlgorithm[] {
				KeyphraseExtractionAlgorithm.KEY_CLUSTER, KeyphraseExtractionAlgorithm.RAKE,
				KeyphraseExtractionAlgorithm.TEXT_RANK, KeyphraseExtractionAlgorithm.TEXT_RANK_WORDNET,
				KeyphraseExtractionAlgorithm.TOPIC_RANK, KeyphraseExtractionAlgorithm.TOPICAL_PAGE_RANK };
		for (int i = 0; i * pageSize < count; i++) {
			Page<Publication> publications = publicationRepository.findAll(new PageRequest(i, pageSize));
			for (Publication pub : publications) {
				String keywords = pub.getDefaultKeywords();
				if (keywords == null || keywords.isEmpty())
					continue;
				for (KeyphraseExtractionAlgorithm alg : algs) {
					pubKws = publicationKeywordsRepository
							.findByPublicationIdAndKeAlgorithmAndIsWikipediaBased(pub.getId(), alg.toString(), false);
					for (int numKwds : numKeywordsArray) {
						for (PublicationKeywords pubKw : pubKws) {
							exportToCSV(pub.getId(), alg.toString(), pub.getDefaultKeywords(), pubKw.getKeywordTokens(),
									false, numKwds);
						}
						pubKws = publicationKeywordsRepository.findByPublicationIdAndKeAlgorithmAndIsWikipediaBased(
								pub.getId(), alg.toString(), true);
						for (PublicationKeywords pubKw : pubKws) {
							exportToCSV(pub.getId(), alg.toString(), pub.getDefaultKeywords(), pubKw.getKeywordTokens(),
									true, numKwds);
						}
					}

				}

			}
		}

	}

	private void exportToCSV(int pubId, String algorithmName, String defaultKeywordString,
			String extractedKeywordString, boolean isWikipediaBased, int numKeywords) {
		Set<String> defaultKeywords = new HashSet<>();
		Set<String> extractedKeywords = new HashSet<>();
		String fileName = algorithmName + numKeywords;
		FileWriter fw = null;
		BufferedWriter bw = null;
		if (isWikipediaBased) {
			fileName += "WB";
		}
		for (String kw : defaultKeywordString.split(",")) {
			if (kw.isEmpty())
				continue;
			defaultKeywords.add(kw.trim().toLowerCase());
		}

		int iter = 0;
		for (WordCount wc : WordCount.parseIntoList(extractedKeywordString)) {
			if (++iter > numKeywords) {
				break;
			}
			extractedKeywords.add(wc.getX().trim().toLowerCase());
		}
		if (extractedKeywords.isEmpty()) {
			return;
		}
		double precision = getPrecisionScore(defaultKeywords, extractedKeywords);
		double recall = getRecallScore(defaultKeywords, extractedKeywords);
		double fScore = 0.0;
		if (precision != 0.0 && recall != 0.0) {
			fScore = (2 * precision * recall) / (precision + recall);
		}
		fileName += ".csv";
		try {
			fw = new FileWriter(fileName, true);
			bw = new BufferedWriter(fw);
			bw.write(pubId + ";" + String.join(",", defaultKeywords) + ";" + String.join(",", extractedKeywords) + ";"
					+ precision + ";" + recall + ";" + fScore + "\n");
			bw.flush();
			fw.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private double getPrecisionScore(Set<String> defaultKeywords, Set<String> extractedKeywords) {
		Set<String> intersection = new HashSet<>(defaultKeywords);
		intersection.retainAll(extractedKeywords);
		return (double) intersection.size() * 100 / extractedKeywords.size();

	}

	private double getRecallScore(Set<String> defaultKeywords, Set<String> extractedKeywords) {
		Set<String> intersection = new HashSet<>(extractedKeywords);
		intersection.retainAll(defaultKeywords);
		return (double) intersection.size() * 100 / defaultKeywords.size();
	}

	public void computeAverageFScore() throws FileNotFoundException, IOException {
		List<String> results = new ArrayList<>();
		List<String> results1 = new ArrayList<>();

		String inputDirectory = "C:\\rks\\Thesis\\Workspace\\cimt-keyphrase-extraction\\eval";
		File dir = new File(inputDirectory);
		String[] extensions = new String[] { "csv" };
		Collection<File> files = FileUtils.listFiles(dir, extensions, false);
		for (File file : files) {
			log.info("###########################");
			log.info("Starting Keyword processing: " + file.getName());
			String result = "";
			String result1 = "";

			List<String> lines = IOUtils.readLines(new FileReader(file));
			String title = FilenameUtils.removeExtension(file.getName()).trim();
			double precision = 0.0, recall = 0.0, fScore = 0.0;
			double precision1 = 0.0, recall1 = 0.0, fScore1 = 0.0;
			int count1 = 0, count = 0;
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				String[] splittedLines = line.split(";");
				int pubId = Integer.valueOf(splittedLines[0]);
				if (pubId <= 185) {
					precision += Double.valueOf(splittedLines[3]);
					recall += Double.valueOf(splittedLines[4]);
					fScore += Double.valueOf(splittedLines[5]);
					count++;

				} else {
					precision1 += Double.valueOf(splittedLines[3]);
					recall1 += Double.valueOf(splittedLines[4]);
					fScore1 += Double.valueOf(splittedLines[5]);
					count1++;
				}
			}
			result = title + ";" + precision / count + ";" + recall / count + ";" + fScore / count;
			results.add(result);
			result1 = title + ";" + precision1 / count1 + ";" + recall1 / count1 + ";" + fScore1 / count1;
			results1.add(result1);
		}
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter("ResultsRwth.csv", true);
			bw = new BufferedWriter(fw);
			for (String res : results) {
				bw.write(res + "\n");
			}
			bw.flush();
			fw.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fw = new FileWriter("ResultsInspec.csv", true);
			bw = new BufferedWriter(fw);
			for (String res : results1) {
				bw.write(res + "\n");
			}
			bw.flush();
			fw.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
