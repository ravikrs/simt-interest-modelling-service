package de.rwth.i9.cimt.ke.service.semantic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

@Service("wikipediaBasedIM")
public class WikipediaBasedIM {
	private static Logger log = LoggerFactory.getLogger(WikipediaBasedIM.class);

	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;

}
