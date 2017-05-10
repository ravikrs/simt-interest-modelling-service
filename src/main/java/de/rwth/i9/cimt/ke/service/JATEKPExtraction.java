package de.rwth.i9.cimt.ke.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.algorithm.kpextraction.jate.Jate;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import uk.ac.shef.dcs.jate.JATEException;
import uk.ac.shef.dcs.jate.model.JATETerm;

@Service("jateKPExtraction")
public class JATEKPExtraction {
	private static final Logger log = LoggerFactory.getLogger(JATEKPExtraction.class);
	private @Value("${cimt.home}") String cimtHome;

	public List<Keyword> extractKeyword(String textbody, String algorithmName, int numKeywords) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		int keywordCount = 0;
		try {
			List<JATETerm> terms = new ArrayList<JATETerm>();
			switch (algorithmName) {
			case "JATE_TTF":
				terms = Jate.TTFAlgo(textbody, cimtHome);
				break;

			case "JATE_ATTF":
				terms = Jate.ATTFAlgo(textbody, cimtHome);
				break;

			case "JATE_TFIDF":
				terms = Jate.TFIDFAlgo(textbody, cimtHome);
				break;

			case "JATE_RIDF":
				terms = Jate.RIDFAlgo(textbody, cimtHome);
				break;

			case "JATE_CVALUE":
				terms = Jate.CValueAlgo(textbody, cimtHome);
				break;

			case "JATE_CHISQUARE":
				terms = Jate.ChiSquareAlgo(textbody, cimtHome);
				break;

			case "JATE_RAKE":
				terms = Jate.RAKEAlgo(textbody, cimtHome);
				break;

			case "JATE_WEIRDNESS":
				terms = Jate.WeirdnessAlgo(textbody, cimtHome);
				break;

			case "JATE_GLOSSEX":
				terms = Jate.GlossExAlgo(textbody, cimtHome);
				break;

			case "JATE_TERMEX":
				terms = Jate.TermExAlgo(textbody, cimtHome);
				break;
			default:
				break;
			}
			for (JATETerm term : terms) {
				Keyword keyword = new Keyword(term.getString(), term.getScore());
				keywords.add(keyword);
				if (++keywordCount > numKeywords)
					break;
			}

		} catch (JATEException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		} catch (SolrServerException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		log.info("JATE KeyphraseExtraction");
		return keywords;

	}
}
