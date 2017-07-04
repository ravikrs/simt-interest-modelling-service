package de.rwth.i9.simt.im.service.topic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.simt.im.service.RAKEKPExtraction;
import de.rwth.i9.simt.ke.lib.model.Keyword;
import de.rwth.i9.simt.nlp.opennlp.OpenNLPImplSpring;

@Service("communityClusterKPExtraction")
public class CommunityClusterKPExtraction {

	private static final Logger log = LoggerFactory.getLogger(RAKEKPExtraction.class);

	@Autowired
	OpenNLPImplSpring openNLPImplSpring;

	public List<Keyword> extractKeyword(String text, int numKeywords) {
		log.info("KeyCluster KeyphraseExtraction");
		return new ArrayList<Keyword>();
	}

}
