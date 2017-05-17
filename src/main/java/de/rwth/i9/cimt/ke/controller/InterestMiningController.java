package de.rwth.i9.cimt.ke.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

import de.rwth.i9.cimt.ke.constants.LatentInterests;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.model.Textbody;
import de.rwth.i9.cimt.ke.service.semantic.WBConceptMap;
import de.rwth.i9.cimt.ke.service.semantic.WikipediaBasedIM;
import de.rwth.i9.cimt.ke.service.semantic.WikipediaBasedKE;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@RestController
public class InterestMiningController {

	private static final Logger log = LoggerFactory.getLogger(InterestMiningController.class);

	@Autowired
	Wikipedia simpleWikiDb;

	@Autowired
	WBConceptMap wbConceptMap;

	@Autowired
	WikipediaBasedKE wikipediaBasedKE;

	@Autowired
	WikipediaBasedIM wikipediaBasedIM;

	@RequestMapping(value = "/interest", method = RequestMethod.GET)
	public ModelAndView getIMWikiBased(Model model) {
		log.info("Inside the getIMWikiBased");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("wbimview", "model", "objectName");
	}

	@RequestMapping(value = "/conceptmap", method = RequestMethod.GET)
	public ModelAndView getConceptMap(Model model) {
		log.info("Inside the getConceptMap");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("conceptmap", "model", "objectName");
	}

	@RequestMapping(value = "/conceptmap", method = RequestMethod.POST)
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

	@RequestMapping(value = "/interest", method = RequestMethod.POST)
	public List<Keyword> postIMWikiBased(@ModelAttribute Textbody textbody, HttpServletRequest req)
			throws WikiApiException {
		log.info("Inside the postIMWikiBased");
		return wikipediaBasedKE.performWBKE(textbody.getText());
	}

	@RequestMapping(value = "/interest/test", method = RequestMethod.POST)
	public List<Keyword> postIMWikiBasedtest(@ModelAttribute Textbody textbody, HttpServletRequest req)
			throws WikiApiException {
		log.info("Inside the postIMWikiBasedtest");
		return wikipediaBasedIM.getWikiBasedKeyphrase(textbody.getText());
	}

}
