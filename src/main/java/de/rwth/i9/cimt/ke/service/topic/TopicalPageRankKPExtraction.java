package de.rwth.i9.cimt.ke.service.topic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.algorithm.kpextraction.unsupervised.graphranking.TextRank;
import de.rwth.i9.cimt.ke.algorithm.kpextraction.unsupervised.topicclustering.TopicalPageRank;
import de.rwth.i9.cimt.ke.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImpl;

@Service("topicalPageRankKPExtraction")
public class TopicalPageRankKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(TopicalPageRankKPExtraction.class);
	@Autowired
	OpenNLPImpl openNLPImpl;

	public List<Keyword> extractKeywordTR(String textbody, int numKeywords) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		int iter = 0;
		for (Keyword keyword : TextRank.performTextRankKE(textbody, openNLPImpl)) {
			if (iter == numKeywords) {
				break;
			}
			keywords.add(keyword);
		}
		log.info("TopicalPageRank KeyphraseExtraction");
		return keywords;
	}

	public List<Keyword> extractKeywordTPR(String textbody, int numKeywords) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		int iter = 0;
		for (Keyword keyword : TopicalPageRank.performTopicalPageRankKE(textbody, openNLPImpl)) {
			if (iter == numKeywords) {
				break;
			}
			keywords.add(keyword);
		}
		return keywords;
	}

}
