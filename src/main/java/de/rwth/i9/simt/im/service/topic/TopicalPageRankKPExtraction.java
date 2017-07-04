package de.rwth.i9.simt.im.service.topic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.unsupervised.topicclustering.TopicalPageRank;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;

@Service("topicalPageRankKPExtraction")
public class TopicalPageRankKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(TopicalPageRankKPExtraction.class);
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;

	@Autowired
	private Environment env;

	public List<Keyword> extractKeywordTPR(String textbody, int numKeywords) {
		List<Keyword> keywords = new ArrayList<>();
		int iter = 0;
		List<Keyword> allKeywords = TopicalPageRank.performTopicalPageRankKE(textbody, openNLPImplSpring,
				env.getProperty("cimt.home"));
		for (Keyword keyword : allKeywords) {
			if (iter == numKeywords) {
				break;
			}
			keywords.add(keyword);
			iter++;
		}
		return keywords;
	}

}
