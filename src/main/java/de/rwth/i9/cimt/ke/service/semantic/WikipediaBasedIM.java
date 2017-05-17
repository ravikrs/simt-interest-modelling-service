package de.rwth.i9.cimt.ke.service.semantic;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.unsupervised.topicclustering.TopicalPageRank;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

@Service("wikipediaBasedIM")
public class WikipediaBasedIM {
	private static Logger log = LoggerFactory.getLogger(WikipediaBasedIM.class);

	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;
	private @Value("${cimt.home}") String cimtHome;

	public List<Keyword> getWikiBasedKeyphrase(String textContent) {
		List<Keyword> keywords = TopicalPageRank.performTopicalPageRankKE(textContent, openNLPImplSpring, cimtHome);

		return keywords;
	}

}
