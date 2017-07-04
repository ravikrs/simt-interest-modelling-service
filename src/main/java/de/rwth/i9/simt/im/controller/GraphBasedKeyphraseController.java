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

import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.model.Textbody;
import de.rwth.i9.simt.im.service.graph.TextRankKPExtraction;
import de.rwth.i9.simt.im.service.graph.TopicRankKPExtraction;

@RestController
@RequestMapping("/kpextraction/graph")
public class GraphBasedKeyphraseController {
	private static final Logger log = LoggerFactory.getLogger(GraphBasedKeyphraseController.class);

	@Autowired
	TextRankKPExtraction textRankKPExtraction;

	@Autowired
	TopicRankKPExtraction topicRankKPExtraction;

	@RequestMapping(value = "/tr", method = RequestMethod.GET)
	public ModelAndView getKPTR(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("kpextraction/graph/tr/trview", "model", "objectName");
	}

	@RequestMapping(value = "/tr", method = RequestMethod.POST)
	public List<Keyword> postKPTR(@ModelAttribute Textbody textbody, HttpServletRequest req) {
		int numKeyword = Integer.parseInt(textbody.getNumKeywords());
		if (numKeyword <= 0) {
			numKeyword = 15;
		}
		return textRankKPExtraction.extractKeywordTextRank(textbody.getText(), numKeyword);
	}

	@RequestMapping(value = "/trwordnet", method = RequestMethod.GET)
	public ModelAndView getKPTRWordnet(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("kpextraction/graph/tr/trwordnetview", "model", "objectName");
	}

	@RequestMapping(value = "/trwordnet", method = RequestMethod.POST)
	public List<Keyword> postKPTRWordnet(@ModelAttribute Textbody textbody, HttpServletRequest req) {
		int numKeyword = Integer.parseInt(textbody.getNumKeywords());
		if (numKeyword <= 0) {
			numKeyword = 15;
		}
		return textRankKPExtraction.extractKeywordTextRankWordnet(textbody.getText(), numKeyword);
	}

	@RequestMapping(value = "/topicrank", method = RequestMethod.GET)
	public ModelAndView getKPTopicRank(Model model) {
		log.info("Inside the getKPTR");
		model.addAttribute("textbody", new Textbody());
		return new ModelAndView("kpextraction/graph/topicrank/topicrankview", "model", "objectName");
	}

	@RequestMapping(value = "/topicrank", method = RequestMethod.POST)
	public List<Keyword> postKPTopicRank(@ModelAttribute Textbody textbody, HttpServletRequest req) {
		int numKeyword = Integer.parseInt(textbody.getNumKeywords());
		if (numKeyword <= 0) {
			numKeyword = 15;
		}
		return topicRankKPExtraction.extractKeywordTopicRank(textbody.getText(), numKeyword);
	}

}
