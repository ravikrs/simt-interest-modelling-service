package de.rwth.i9.cimt.ke.service.graph;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.textrank.LanguageEnglish;
import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.textrank.TextRankWordnet;
import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.unsupervised.graphranking.TextRank;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;

@Service("textRankKPExtraction")
public class TextRankKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(TextRankKPExtraction.class);
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;
	@Autowired
	LanguageEnglish languageEnglish;
	private @Value("${cimt.home}") String cimtHome;

	public List<Keyword> extractKeywordTextRank(String text, int numKeyword) {
		List<Keyword> keywords = new ArrayList<>();
		// our implementation
		List<Keyword> totalkeywords = TextRank.performTextRankKE(text, openNLPImplSpring);
		// List<Keyword> totalkeywords =
		// TextRankWordnet.extractKeywordTextRankWordnet(text, openNLPImpl,
		// languageEnglish,
		// cimtHome + "LexSemResources/WordNet3.0", false);
		totalkeywords.sort(Keyword.KeywordComparatorDesc);
		for (Keyword keyword : totalkeywords) {
			if (keywords.size() >= numKeyword) {
				break;
			}
			keywords.add(keyword);
		}
		log.info("TextRank KeyphraseExtraction");
		return keywords;
	}

	public List<Keyword> extractKeywordTextRankWordnet(String text, int numKeyword) {
		List<Keyword> keywords = new ArrayList<>();
		List<Keyword> totalkeywords = TextRankWordnet.extractKeywordTextRankWordnet(text, openNLPImplSpring, languageEnglish,
				cimtHome + "/LexSemResources/WordNet3.0", true);
		totalkeywords.sort(Keyword.KeywordComparatorDesc);
		for (Keyword keyword : totalkeywords) {
			if (keywords.size() >= numKeyword) {
				break;
			}
			keywords.add(keyword);
		}
		return keywords;
	}

}
