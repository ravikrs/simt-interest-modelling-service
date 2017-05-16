package de.rwth.i9.cimt.ke.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.model.Textbody;
import de.rwth.i9.cimt.ke.service.semantic.WikipediaBasedKE;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@RestController
@RequestMapping("/interest")
public class InterestMiningController {

	private static final Logger log = LoggerFactory.getLogger(InterestMiningController.class);

	@Autowired
	WikipediaBasedKE wikipediaBasedKE;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView getIMWikiBased(Model model) {
		log.info("Inside the getIMWikiBased");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("wbimview", "model", "objectName");
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public List<Keyword> postIMWikiBased(@ModelAttribute Textbody textbody, HttpServletRequest req)
			throws WikiApiException {
		log.info("Inside the getIMWikiBased");
		return wikipediaBasedKE.performWBKE(textbody.getText());
	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public List<Keyword> postIMWikiBasedtest(@ModelAttribute Textbody textbody, HttpServletRequest req)
			throws WikiApiException {
		log.info("Inside the getIMWikiBased");
		return wikipediaBasedKE.performWBKE(textbody.getText());
	}
}
