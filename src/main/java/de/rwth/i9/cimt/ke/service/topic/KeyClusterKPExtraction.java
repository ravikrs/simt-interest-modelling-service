package de.rwth.i9.cimt.ke.service.topic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.model.Keyword;
import de.rwth.i9.cimt.ke.service.RAKEKPExtraction;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImpl;

@Service("keyClusterKPExtraction")
public class KeyClusterKPExtraction {

	private static final Logger log = LoggerFactory.getLogger(RAKEKPExtraction.class);

	@Autowired
	OpenNLPImpl openNLPImpl;

	public List<Keyword> extractKeyword(String text, int numKeywords) {
		log.info("KeyCluster KeyphraseExtraction");
		return new ArrayList<Keyword>();
	}

}
