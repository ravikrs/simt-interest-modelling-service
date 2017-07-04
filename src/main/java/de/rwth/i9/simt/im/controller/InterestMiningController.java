package de.rwth.i9.simt.im.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.utils.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import de.rwth.i9.simt.im.constants.AuthorInterestType;
import de.rwth.i9.simt.im.constants.LatentInterests;
import de.rwth.i9.simt.im.model.eval.AuthorInterests;
import de.rwth.i9.simt.im.model.eval.InterestRequestBody;
import de.rwth.i9.simt.im.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.simt.im.service.semantic.WBConceptMap;
import de.rwth.i9.simt.im.service.semantic.WikipediaBasedIM;
import de.rwth.i9.simt.im.service.semantic.WikipediaBasedKE;
import de.rwth.i9.simt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.simt.ke.lib.model.Keyword;
import de.rwth.i9.simt.ke.lib.model.Textbody;
import de.rwth.i9.simt.ke.lib.util.WordCount;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class InterestMiningController {

	private static final Logger log = LoggerFactory.getLogger(InterestMiningController.class);

	@Autowired
	Wikipedia simpleWikiDb;

	@Autowired
	WBConceptMap wbConceptMap;

	@Autowired
	WikipediaBasedKE wikipediaBasedKE;

	@Autowired
	AuthorInterestsRepository authorInterestRepository;
	@Autowired
	WikipediaBasedIM wikipediaBasedIM;

	@RequestMapping(value = "/interest", method = RequestMethod.GET)
	public ModelAndView getIMWikiBased(Model model) {
		log.info("Inside the getIMWikiBased");
		model.addAttribute("interestRequestBody", new InterestRequestBody());
		return new ModelAndView("wbimview", "model", "objectName");
	}

	@RequestMapping(value = "/interest", method = RequestMethod.POST)
	public ModelAndView postIMWikiBased(Model model, @ModelAttribute InterestRequestBody interestReqBody)
			throws JSONException, WikiApiException {

		KeyphraseExtractionAlgorithm ke = KeyphraseExtractionAlgorithm.fromString(interestReqBody.getAlgorithmName());
		List<AuthorInterests> interests = authorInterestRepository.findByAuthorIdAndKeAlgorithmAndInterestType(
				interestReqBody.getAuthorId(), ke.toString(), "WIKIPEDIA_BASED");
		List<WordCount> wcs = new ArrayList<>();
		int maxNum = interestReqBody.getNumKeywords();
		int count = 0;
		for (AuthorInterests authInt : interests) {
			for (WordCount wc : WordCount.parseIntoList(authInt.getAuthorInterest())) {
				if (++count >= maxNum) {
					break;
				}
				wcs.add(wc);
			}
		}

		JSONObject jsonRet = new JSONObject();
		LatentInterests conceptMapType = LatentInterests.fromString(interestReqBody.getLatentInterestType());
		switch (conceptMapType) {
		case CTG_PARENT:
			jsonRet = wikipediaBasedIM.getConceptMapJsonForLatentParentCategories(wcs, 15);
			break;
		case CTG_PARENT_SIBLING:
			jsonRet = wikipediaBasedIM.getConceptMapJsonForLatentParentSiblingCategories(wcs, 15,
					interestReqBody.getFilterCommonCategory());
			break;
		case CTG_PARENT_DESCENDENT:
			jsonRet = wikipediaBasedIM.getConceptMapJsonForLatentParentDescendentCategories(wcs, 15,
					interestReqBody.getFilterCommonCategory());
			break;
		case CTG_PARENT_SIBLING_DESCENDENT:
			jsonRet = wikipediaBasedIM.getConceptMapJsonForLatentParentSiblingDescendentCategories(wcs, 15,
					interestReqBody.getFilterCommonCategory());
			break;

		case DEFAULT:
			jsonRet = wikipediaBasedIM.getConceptMapJsonForLatentParentCategories(wcs, 15);
		}
		model.addAttribute("conceptjson", jsonRet.toString());
		return new ModelAndView("wbimview", "model", model);
	}

	@RequestMapping(value = "/interest/kewb", method = RequestMethod.GET)
	public ModelAndView getIMKEWikiBased(Model model) {
		log.info("Inside the getIMWikiBased");
		model.addAttribute("interestRequestBody", new InterestRequestBody());
		return new ModelAndView("wbkeview", "model", "objectName");
	}

	@RequestMapping(value = "/interest/kewb", method = RequestMethod.POST)
	public ModelAndView postIMKEWikiBased(Model model, @ModelAttribute InterestRequestBody interestReqBody)
			throws JSONException, WikiApiException {

		KeyphraseExtractionAlgorithm ke = KeyphraseExtractionAlgorithm.fromString(interestReqBody.getAlgorithmName());
		List<AuthorInterests> interests = authorInterestRepository.findByAuthorIdAndKeAlgorithmAndInterestType(
				interestReqBody.getAuthorId(), ke.toString(), AuthorInterestType.DEFAULT.toString());

		List<WordCount> wcs = new ArrayList<>();
		for (AuthorInterests authInt : interests) {
			wcs.addAll(WordCount.parseIntoList(authInt.getAuthorInterest()));
		}

		JSONObject jsonRet = wikipediaBasedKE.getConceptMapJsonForKeywords(wcs, interestReqBody.getNumKeywords());
		model.addAttribute("conceptjson", jsonRet.toString());
		return new ModelAndView("wbkeview", "model", model);
	}

	//	@RequestMapping(value = "/conceptmap", method = RequestMethod.GET)
	public ModelAndView getConceptMap(Model model) {
		log.info("Inside the getConceptMap");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("conceptmap", "model", "objectName");
	}

	//@RequestMapping(value = "/conceptmap", method = RequestMethod.POST)
	public ModelAndView postConceptMap(Model model, @ModelAttribute Textbody textbody) throws JSONException {
		log.info("Inside the postConceptMap");
		String[] tokens = textbody.getText().split(",");
		List<String> pagesString = Arrays.asList(tokens);
		JSONObject jsonRet = new JSONObject();
		LatentInterests conceptMapType = LatentInterests.fromString(textbody.getAlgorithmName());
		switch (conceptMapType) {
		case CTG_PARENT:
			jsonRet = wbConceptMap.getConceptMapJsonForLatentParentInterests(pagesString);
			break;
		case CTG_PARENT_SIBLING:
			jsonRet = wbConceptMap.getConceptMapJsonForLatentParentSiblingInterests(pagesString);
			break;
		case CTG_PARENT_SIBLING_DESCENDENT:
			jsonRet = wbConceptMap.getConceptMapJsonForLatentParentSiblingDescendentInterests(pagesString);
			break;

		default:
			jsonRet = wbConceptMap.getConceptMapJsonForLatentParentDescendentInterests(pagesString);
			break;
		}
		model.addAttribute("conceptjson", jsonRet.toString());
		return new ModelAndView("conceptmap", "model", model);
	}

	@RequestMapping(value = "/interest/author", method = RequestMethod.GET)
	public ModelAndView getAuthorInterestKE(Model model) {
		log.info("Inside the getAuthorInterestKE");
		model.addAttribute("interestRequestBody", new InterestRequestBody());
		return new ModelAndView("authorinterest", "model", model);
	}

	@RequestMapping(value = "/interest/author", method = RequestMethod.POST)
	public ModelAndView postAuthorInterestKE(Model model, @ModelAttribute InterestRequestBody interestRequestBody)
			throws WikiApiException {
		log.info("Inside the postAuthorInterestKE");
		int numKeywords = interestRequestBody.getNumKeywords();
		if (numKeywords <= 0) {
			numKeywords = 15;
		}
		List<Keyword> keywords = wikipediaBasedKE.getkeywordsForAuthor(interestRequestBody.getAuthorId(),
				KeyphraseExtractionAlgorithm.fromString(interestRequestBody.getAlgorithmName()),
				AuthorInterestType.fromString(interestRequestBody.getLatentInterestType()), numKeywords);
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

		return new ModelAndView("authorinterest", "model", model);
	}
}
