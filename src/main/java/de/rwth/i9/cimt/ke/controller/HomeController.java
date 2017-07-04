package de.rwth.i9.cimt.ke.controller;

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

import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.model.Textbody;
import de.rwth.i9.cimt.ke.service.KPExtraction;
import de.rwth.i9.cimt.ke.service.eval.AuthorInterestExtractorService;
import de.rwth.i9.cimt.ke.service.eval.FScoreComputeService;
import de.rwth.i9.cimt.ke.service.eval.KEExtractionService;
import de.rwth.i9.cimt.ke.service.eval.RecoService;
import de.rwth.i9.cimt.ke.service.eval.SimilarityVector;
import de.rwth.i9.cimt.ke.service.eval.SqlCorpusImporter;
import de.rwth.i9.cimt.ke.service.topic.TopicalPageRankKPExtraction;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

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
		Default, Reduced, Metrics, Parent, Sibling, Descendent,;
		public static RecoSimilarityAlgorithm fromString(String value) {
			if ("Default".equalsIgnoreCase(value))
				return Default;
			if ("Reduced".equalsIgnoreCase(value))
				return Reduced;
			if ("Metrics".equalsIgnoreCase(value))
				return Metrics;
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
	public ModelAndView getKE(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("home", "model", "objectName");
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
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

	@RequestMapping(value = "/ke", method = RequestMethod.GET)
	public String getKE1(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		//keExtractionService.runKEAlgorithm();
		return "Done";
	}

	@RequestMapping(value = "/ke1", method = RequestMethod.GET)
	public String getKE21() {
		//authorInterestExtractorService.performInterestMingingForAllAuthors();
		return "Done";
	}

	@RequestMapping(value = "/inspec", method = RequestMethod.GET)
	public String getInspec() throws FileNotFoundException, IOException {
		//sqlCorpusImporter.runCorpusImporter();
		return "Done";
	}

	@RequestMapping(value = "/inspecupd", method = RequestMethod.GET)
	public String updateInspec() throws FileNotFoundException, IOException {
		//sqlCorpusImporter.updateKeywords();
		return "Done";
	}

	@RequestMapping(value = "/fscore", method = RequestMethod.GET)
	public String computeFScore() throws FileNotFoundException, IOException {
		//fScoreComputeService.computeFScore();
		return "Done";
	}

	@RequestMapping(value = "/fscoreavg", method = RequestMethod.GET)
	public String computeAverageFScore() throws FileNotFoundException, IOException {
		fScoreComputeService.computeAverageFScore();
		return "Done";
	}

	@RequestMapping(value = "/cb", method = RequestMethod.POST)
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

		default:
			break;
		}
		return scores;
	}

	@RequestMapping(value = "/pb", method = RequestMethod.POST)
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
			score = recoService.computeDefaultorReduced(setA, setB, false);
			scores.add(score);
			break;
		case Parent:
			scores = recoService.computeParent(setA, setB, false);
			break;
		case Sibling:
			scores = recoService.computeSibling(setA, setB, false);
			break;
		case Descendent:
			scores = recoService.computeDescendent(setA, setB, false);
			break;

		default:
			break;
		}
		return scores;
	}

}
