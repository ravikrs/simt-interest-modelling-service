package de.rwth.i9.cimt.ke.service.topic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.unsupervised.topicclustering.KeyCluster;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.service.RAKEKPExtraction;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;

@Service("keyClusterKPExtraction")
public class KeyClusterKPExtraction {

	private static final Logger log = LoggerFactory.getLogger(RAKEKPExtraction.class);

	@Autowired
	OpenNLPImplSpring openNLPImplSpring;

	public List<Keyword> extractKeyword(String text, int numKeywords) {
		log.info("KeyCluster KeyphraseExtraction");
		List<Keyword> keywords = new ArrayList<>();
		List<Keyword> kw = KeyCluster.performKeyClusterKE(text, openNLPImplSpring);
		kw.sort(Keyword.KeywordComparatorDesc);
		for (Keyword keyword : kw) {
			if (keywords.size() >= numKeywords) {
				break;
			}
			keywords.add(keyword);
		}
		return keywords;
	}

}
