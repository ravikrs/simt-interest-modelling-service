package de.rwth.i9.cimt.ke.service.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.util.NLPUtil;
import de.rwth.i9.cimt.ke.lib.util.WikipediaUtil;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPagemapline;
import de.rwth.i9.cimt.ke.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("wikipediaBasedKE")
public class WikipediaBasedKE {
	private static Logger log = LoggerFactory.getLogger(WikipediaBasedKE.class);

	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;

	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;

	public List<Keyword> performWBKE(String textContent) throws WikiApiException {
		List<Keyword> returnedKeywords = new ArrayList<>();
		//Map<String, Integer> candidateTokens = NLPUtil.splitTextByStopWords(textContent, openNLPImplSpring);
		Map<String, List<Integer>> candidateTokens = NLPUtil.getNounAdjSeqToken(textContent, openNLPImplSpring);
		Map<String, List<Page>> tokenPageMap = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entryToken : candidateTokens.entrySet()) {
			List<Page> wikipediaPages = this.getWikipediPageForToken(entryToken.getKey());
			if (!wikipediaPages.isEmpty()) {
				tokenPageMap.put(entryToken.getKey(), wikipediaPages);
			}

		}
		Set<String> wikiPage = new HashSet<>();
		Set<String> parentCategories = new HashSet<>();
		Set<String> siblingCategories = new HashSet<>();

		for (Map.Entry<String, List<Page>> entry : tokenPageMap.entrySet()) {
			for (Page p : entry.getValue()) {
				wikiPage.add(p.getTitle().getEntity());
				for (Category c : p.getCategories()) {
					if (!WikipediaUtil.isGenericWikipediaCategory(c.getPageId())) {
						parentCategories.add(c.getTitle().getEntity());
					}
				}
			}
		}
		wikiPage.addAll(parentCategories);
		for (String k : wikiPage) {
			returnedKeywords.add(new Keyword(k, 0));
		}
		return returnedKeywords;

	}

	private List<Page> getWikipediPageForToken(String token) throws WikiApiException {
		List<Page> pages = new ArrayList<>();
		Set<Integer> pageIds = new HashSet<>();
		if (token.isEmpty()) {
			return pages;
		}
		String tokenWB = token.trim().replaceAll("\\s+", "_");
		for (WikiPagemapline wpm : wikiPagemaplineRepository.findByName(tokenWB)) {
			pageIds.add(wpm.getPageId());
		}
		if (pageIds.isEmpty()) {
			String[] subtokens = token.split("\\s+");
			int tokenSize = subtokens.length;
			Set<String> subCandidates = NLPUtil.generateNgramsCandidate(token, tokenSize - 2, tokenSize - 1);
			for (String subCandidate : subCandidates) {
				if (!subCandidate.isEmpty()) {
					List<Page> p = this.getWikipediPageForToken(subCandidate);
					if (p != null && !p.isEmpty()) {
						pages.addAll(p);
					}
				}

			}
			return pages;

		} else {
			for (Integer pageId : pageIds) {
				Page p = this.simpleWikiDb.getPage(pageId);
				pages.add(p);
			}
			return pages;
		}

	}

	public List<WikiPagemapline> getPageIdForTitle(String title) {
		title = title.trim().replaceAll("\\s+", "_");
		return wikiPagemaplineRepository.findByName(title);

	}

}
