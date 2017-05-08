package de.rwth.i9.cimt.ke.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.cimt.ke.model.Keyword;
import de.rwth.i9.cimt.ke.service.graph.TextRankKPExtraction;
import de.rwth.i9.cimt.ke.service.graph.TopicRankKPExtraction;
import de.rwth.i9.cimt.ke.service.topic.CommunityClusterKPExtraction;
import de.rwth.i9.cimt.ke.service.topic.KeyClusterKPExtraction;
import de.rwth.i9.cimt.ke.service.topic.TopicalPageRankKPExtraction;

@Service("kpExtraction")
public class KPExtraction {
	private static final Logger log = LoggerFactory.getLogger(KPExtraction.class);
	@Autowired
	TextRankKPExtraction textRankKPExtraction;
	@Autowired
	TopicRankKPExtraction topicRankKPExtraction;
	@Autowired
	JATEKPExtraction jateKPExtraction;
	@Autowired
	RAKEKPExtraction rakeKPExtraction;
	@Autowired
	KeyClusterKPExtraction keyClusterKPExtraction;
	@Autowired
	CommunityClusterKPExtraction communityClusterKPExtraction;
	@Autowired
	TopicalPageRankKPExtraction topicalPageRankKPExtraction;

	public List<Keyword> extractKeyword(String textbody, String algorithmName, int numKeywords) {
		KeyphraseExtractionAlgorithm algorithm = KeyphraseExtractionAlgorithm.fromString(algorithmName);
		List<Keyword> keyphrases = new ArrayList<>();
		switch (algorithm) {
		case KEY_CLUSTER:
			keyphrases = keyClusterKPExtraction.extractKeyword(textbody, numKeywords);
			break;
		case DEFAULT:
			keyphrases = textRankKPExtraction.extractKeywordTextRankWordnet(textbody, numKeywords);
			break;
		case JATE_ATTF:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_CHISQUARE:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_CVALUE:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_GLOSSEX:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_RAKE:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_RIDF:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_TERMEX:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_TTF:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_TFIDF:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case JATE_WEIRDNESS:
			keyphrases = jateKPExtraction.extractKeyword(textbody, algorithm.toString(), numKeywords);
			break;
		case RAKE:
			keyphrases = rakeKPExtraction.extractKeyword(textbody, numKeywords);
			break;
		case TEXT_RANK:
			keyphrases = textRankKPExtraction.extractKeywordTextRank(textbody, numKeywords);
			break;
		case TEXT_RANK_WORDNET:
			keyphrases = textRankKPExtraction.extractKeywordTextRankWordnet(textbody, numKeywords);
			break;
		case TOPIC_RANK:
			keyphrases = topicRankKPExtraction.extractKeywordTopicRank(textbody, numKeywords);
			break;
		case TOPICAL_PAGE_RANK:
			keyphrases = topicalPageRankKPExtraction.extractKeywordTPR(textbody, numKeywords);
			break;
		default:
			break;
		}

		return keyphrases;

	}
}
