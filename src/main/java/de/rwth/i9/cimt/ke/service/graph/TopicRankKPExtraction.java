package de.rwth.i9.cimt.ke.service.graph;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.algorithm.kpextraction.unsupervised.graphranking.TopicRank;
import de.rwth.i9.cimt.ke.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImpl;

@Service("topicRankKPExtraction")
public class TopicRankKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(TopicRankKPExtraction.class);
	@Autowired
	OpenNLPImpl openNLPImpl;

	public List<Keyword> extractKeywordTopicRank(String text, int numKeyword) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		List<Keyword> totalkeywords = TopicRank.performTopicRankKE(text, openNLPImpl);
		for (Keyword keyword : totalkeywords) {
			if (keywords.size() >= numKeyword) {
				break;
			}
			keywords.add(keyword);
		}
		log.info("TopicRank KeyphraseExtraction");
		return keywords;
	}

}
