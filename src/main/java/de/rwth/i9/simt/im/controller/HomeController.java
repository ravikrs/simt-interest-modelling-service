package de.rwth.i9.simt.im.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.utils.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import de.rwth.i9.simt.im.service.KPExtraction;
import de.rwth.i9.simt.im.service.eval.AuthorInterestExtractorService;
import de.rwth.i9.simt.im.service.eval.FScoreComputeService;
import de.rwth.i9.simt.im.service.eval.KEExtractionService;
import de.rwth.i9.simt.im.service.eval.RecoService;
import de.rwth.i9.simt.im.service.eval.SimilarityVector;
import de.rwth.i9.simt.im.service.eval.SqlCorpusImporter;
import de.rwth.i9.simt.im.service.topic.TopicalPageRankKPExtraction;
import de.rwth.i9.simt.ke.lib.model.Keyword;
import de.rwth.i9.simt.ke.lib.model.Textbody;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import springfox.documentation.annotations.ApiIgnore;

@Configuration
@RestController
public class HomeController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);
	@Autowired
	KPExtraction kpExtraction;
	@Autowired
	TopicalPageRankKPExtraction topicalPageRankKPExtraction;
	@Autowired
	KEExtractionService keExtractionService;

	@Autowired
	SqlCorpusImporter sqlCorpusImporter;

	@Autowired
	AuthorInterestExtractorService authorInterestExtractorService;

	@Autowired
	FScoreComputeService fScoreComputeService;
	@Autowired
	RecoService recoService;

	public enum RecoSimilarityAlgorithm {
		Default, Reduced, WikiLink, Parent, Sibling, Descendent,;
		public static RecoSimilarityAlgorithm fromString(String value) {
			if ("Default".equalsIgnoreCase(value))
				return Default;
			if ("Reduced".equalsIgnoreCase(value))
				return Reduced;
			if ("WikiLink".equalsIgnoreCase(value))
				return WikiLink;
			if ("Parent".equalsIgnoreCase(value))
				return Parent;
			if ("Sibling".equalsIgnoreCase(value))
				return Sibling;
			if ("Descendent".equalsIgnoreCase(value))
				return Descendent;

			return null;
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ApiIgnore
	public ModelAndView getKE(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("home", "model", "objectName");
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ApiIgnore
	public ModelAndView postKE(Model model, @ModelAttribute Textbody textbody) {
		log.info("Inside the getKPTR");
		int numKeywords = Integer.parseInt(textbody.getNumKeywords());
		if (numKeywords <= 0) {
			numKeywords = 10;
		}
		List<Keyword> keywords = kpExtraction.extractKeyword(textbody.getText(), textbody.getAlgorithmName(),
				numKeywords);
		try {
			JSONArray jsonResp = new JSONArray();
			JSONObject obj;
			for (Keyword k : keywords) {
				obj = new JSONObject();
				obj.put("weight", k.getWeight());
				obj.put("text", k.getToken());
				jsonResp.put(obj);
			}
			model.addAttribute("words", jsonResp.toString().replaceAll("\\\"", "\""));
		} catch (JSONException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		model.addAttribute("keywords", keywords);

		return new ModelAndView("home", "model", model);
	}

	@RequestMapping(value = "/kpextraction", method = RequestMethod.POST)
	public List<Keyword> postKPExtraction(@ModelAttribute Textbody textbody) {
		int numKeywords = Integer.parseInt(textbody.getNumKeywords());
		if (numKeywords <= 0) {
			numKeywords = 10;
		}
		return kpExtraction.extractKeyword(textbody.getText(), textbody.getAlgorithmName(), numKeywords);
	}

	//@RequestMapping(value = "/ke", method = RequestMethod.GET)
	//@ApiIgnore
	public String getKE1(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		//keExtractionService.runKEAlgorithm();
		return "Done";
	}

	//@RequestMapping(value = "/ke1", method = RequestMethod.GET)
	//@ApiIgnore
	public String getKE21() {
		//authorInterestExtractorService.performInterestMingingForAllAuthors();
		return "Done";
	}

	//@RequestMapping(value = "/inspec", method = RequestMethod.GET)
	//@ApiIgnore
	public String getInspec() throws FileNotFoundException, IOException {
		//sqlCorpusImporter.runCorpusImporter();
		return "Done";
	}

	//@RequestMapping(value = "/inspecupd", method = RequestMethod.GET)
	//@ApiIgnore
	public String updateInspec() throws FileNotFoundException, IOException {
		//sqlCorpusImporter.updateKeywords();
		return "Done";
	}

	//@RequestMapping(value = "/fscore", method = RequestMethod.GET)
	//@ApiIgnore
	public String computeFScore() throws FileNotFoundException, IOException {
		//fScoreComputeService.computeFScore();
		return "Done";
	}

	//@RequestMapping(value = "/fscoreavg", method = RequestMethod.GET)
	//@ApiIgnore
	public String computeAverageFScore() throws FileNotFoundException, IOException {
		//	fScoreComputeService.computeAverageFScore();
		return "Done";
	}

	@RequestMapping(value = "/similarity/cosine", method = RequestMethod.POST)
	public List<Double> computeCSSimilarity(@RequestBody SimilarityVector sv) throws WikiApiException {
		List<Double> scores = new ArrayList<>();
		double score = 0.0;
		RecoSimilarityAlgorithm algo = RecoSimilarityAlgorithm.fromString(sv.getSimilarityAlgorithm());
		Set<String> setA = new HashSet<>(sv.getVector1());
		Set<String> setB = new HashSet<>(sv.getVector2());
		switch (algo) {
		case Default:
			score = recoService.computeDefaultorReduced(setA, setB, true);
			scores.add(score);
			break;
		case Reduced:
			score = recoService.computeDefaultorReduced(setA, setB, true);
			scores.add(score);
			break;
		case Parent:
			scores = recoService.computeParent(setA, setB, true);
			break;
		case Sibling:
			scores = recoService.computeSibling(setA, setB, true);
			break;
		case Descendent:
			scores = recoService.computeDescendent(setA, setB, true);
			break;
		case WikiLink:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			score = recoService.computeWikiLink(setA, setB, true);
			scores.add(score);
		default:
			break;
		}
		return scores;
	}

	@RequestMapping(value = "/similarity/pearson", method = RequestMethod.POST)
	public List<Double> computePBSimilarity(@RequestBody SimilarityVector sv) throws WikiApiException {
		List<Double> scores = new ArrayList<>();
		double score = 0.0;
		RecoSimilarityAlgorithm algo = RecoSimilarityAlgorithm.fromString(sv.getSimilarityAlgorithm());
		Set<String> setA = new HashSet<>(sv.getVector1());
		Set<String> setB = new HashSet<>(sv.getVector2());
		switch (algo) {
		case Default:
			score = recoService.computeDefaultorReduced(setA, setB, false);
			scores.add(score);
			break;
		case Reduced:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			score = recoService.computeDefaultorReduced(setA, setB, false);
			scores.add(score);
			break;
		case Parent:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			scores = recoService.computeParent(setA, setB, false);
			break;
		case Sibling:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			scores = recoService.computeSibling(setA, setB, false);
			break;
		case Descendent:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			scores = recoService.computeDescendent(setA, setB, false);
			break;
		case WikiLink:
			setA = authorInterestExtractorService.getReducedInterestsFromWikipedia(setA);
			setB = authorInterestExtractorService.getReducedInterestsFromWikipedia(setB);
			score = recoService.computeWikiLink(setA, setB, false);
			scores.add(score);
		default:
			break;
		}
		return scores;
	}

	@RequestMapping(value = "/interest/reduce", method = RequestMethod.POST)
	public List<String> computePBSimilarity(@RequestBody List<String> interests) throws WikiApiException {
		return new ArrayList<>(
				authorInterestExtractorService.getReducedInterestsFromWikipedia(new HashSet<>(interests)));
	}
}
