package de.rwth.i9.simt.im.controller;

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

import de.rwth.i9.simt.im.service.JATEKPExtraction;
import de.rwth.i9.simt.im.service.RAKEKPExtraction;
import de.rwth.i9.simt.ke.lib.model.Keyword;
import de.rwth.i9.simt.ke.lib.model.Textbody;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/kpextraction")
@ApiIgnore
public class SupervisedKeyphraseController {
	private static final Logger log = LoggerFactory.getLogger(SupervisedKeyphraseController.class);
	@Autowired
	JATEKPExtraction jateKPExtraction;
	@Autowired
	RAKEKPExtraction rakeKPExtraction;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getKP() {
		return "keyphrase extraction";
	}

	@RequestMapping(value = "/rake", method = RequestMethod.GET)
	public ModelAndView getKPRake(Model model) {
		log.info("Inside the getKPRake");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("kpextraction/rake/rakeview", "model", "objectName");
	}

	@RequestMapping(value = "/rake", method = RequestMethod.POST)
	public List<Keyword> postKPRAKE(@ModelAttribute Textbody textbody, HttpServletRequest req) {
		int numKeyword = Integer.parseInt(textbody.getNumKeywords());
		if (numKeyword <= 0) {
			numKeyword = 15;
		}
		return rakeKPExtraction.extractKeyword(textbody.getText(), numKeyword);
	}

	@RequestMapping(value = "/jate", method = RequestMethod.GET)
	public ModelAndView getKPJate(Model model) {
		log.info("Inside the getKPJate");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("kpextraction/jate/jateview", "model", "objectName");
	}

	@RequestMapping(value = "/jate", method = RequestMethod.POST)
	public List<Keyword> postKPJATE(@ModelAttribute Textbody textbody, HttpServletRequest req) {
		int numKeyword = Integer.parseInt(textbody.getNumKeywords());
		if (numKeyword <= 0) {
			numKeyword = 15;
		}
		return jateKPExtraction.extractKeyword(textbody.getText(), textbody.getAlgorithmName(), numKeyword);
	}
}
