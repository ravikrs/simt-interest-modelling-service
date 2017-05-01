package de.rwth.i9.cimt.ke.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.algorithm.kpextraction.rake.Rake;
import de.rwth.i9.cimt.ke.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImpl;

@Service("rakeKPExtraction")
public class RAKEKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(RAKEKPExtraction.class);

	@Autowired
	OpenNLPImpl openNLPImpl;

	public List<Keyword> extractKeyword(String text, int numKeywords) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		List<Keyword> totalKeywords = new ArrayList<Keyword>();
		int keywordCount = 0;
		totalKeywords = Rake.extractKeyword(text, openNLPImpl);
		for (Keyword keyword : totalKeywords) {
			keywords.add(keyword);
			if (++keywordCount > numKeywords)
				break;
		}
		log.info("RAKE KeyphraseExtraction");
		return keywords;
	}

}
