package de.rwth.i9.simt.im.service.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.util.WordCount;
import de.rwth.i9.simt.im.constants.AuthorInterestType;
import de.rwth.i9.simt.im.model.eval.AuthorInterests;
import de.rwth.i9.simt.im.model.wikipedia.WikiPagemapline;
import de.rwth.i9.simt.im.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.simt.im.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.simt.im.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("wikipediaBasedKE")
public class WikipediaBasedKE {
	private static Logger log = LoggerFactory.getLogger(WikipediaBasedKE.class);

	@Autowired
	private Wikipedia simpleWikiDb;

	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;

	@Autowired
	AuthorInterestsRepository authorInterestsRepo;

	public List<Keyword> getkeywordsForAuthor(int authorId, KeyphraseExtractionAlgorithm algorithmName,
			AuthorInterestType interestType, int numKeywords) {
		List<Keyword> returnedKp = new ArrayList<>();
		List<Keyword> keyphrases = new ArrayList<>();
		List<AuthorInterests> authorInterests = authorInterestsRepo.findByAuthorIdAndKeAlgorithmAndInterestType(
				authorId, algorithmName.toString(), interestType.toString());
		for (AuthorInterests authorInterest : authorInterests) {
			List<WordCount> wordCounts = WordCount.parseIntoList(authorInterest.getAuthorInterest());
			double sum = 0.0;
			for (WordCount wc : wordCounts) {
				sum += wc.getY();
			}

			for (WordCount wc : wordCounts) {
				Keyword kw = new Keyword(wc.getX(), wc.getY() / sum);
				keyphrases.add(kw);
			}
		}
		Collections.sort(keyphrases, Keyword.KeywordComparatorDesc);
		int counter = 0;
		for (Keyword kw : keyphrases) {
			counter++;
			returnedKp.add(kw);
			if (counter >= numKeywords)
				break;
		}
		return returnedKp;

	}

	public JSONObject getConceptMapJsonForKeywords(List<WordCount> wordCounts, int numKeywords)
			throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, String> idPage = new HashMap<>();

		Map<String, Set<Integer>> kePages = new HashMap<>();

		int ditemId = 0;
		int count = 0;
		for (WordCount wc : wordCounts) {
			if (++count > numKeywords) {
				break;
			}
			kePages.put(wc.getX(), new HashSet<>());
			if (simpleWikiDb.existsPage(wc.getX())) {
				Page p = simpleWikiDb.getPage(wc.getX());
				kePages.get(wc.getX()).add(p.getPageId());
				if (!idPage.containsKey(p.getPageId())) {
					idPage.put(p.getPageId(), p.getTitle().getEntity());
				}

			} else {
				Set<Integer> wpmPageIds = new HashSet<>();
				List<WikiPagemapline> wpms = wikiPagemaplineRepository
						.findByName(WikipediaUtil.toWikipediaArticleName(wc.getX()));
				if (wpms.isEmpty()) {
					wpms = wikiPagemaplineRepository.findByStem(WikipediaUtil.toWikipediaArticleStem(wc.getX()));
				}
				for (WikiPagemapline wpm : wpms) {
					if (wpmPageIds.contains(wpm.getPageId())) {
						continue;
					}
					wpmPageIds.add(wpm.getPageId());
					Page p = simpleWikiDb.getPage(wpm.getPageId());
					kePages.get(wc.getX()).add(p.getPageId());

					if (!idPage.containsKey(p.getPageId())) {
						idPage.put(p.getPageId(), p.getTitle().getEntity());
					}
				}
			}
		}

		for (Map.Entry<Integer, String> entry : idPage.entrySet()) {
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "Page: " + entry.getValue());
			themeJson.put("description", "Wikipedia Page : " + entry.getValue());
			themeJson.put("slug", "Page: " + entry.getValue());
			themes.put(themeJson);
		}
		for (Map.Entry<String, Set<Integer>> entry : kePages.entrySet()) {
			JSONObject dItemJson = new JSONObject();
			JSONArray ditemsLinks = new JSONArray();
			dItemJson.put("type", "ditem");
			dItemJson.put("name", entry.getKey());
			dItemJson.put("description", "Interest: " + entry.getKey());
			dItemJson.put("ditem", ++ditemId);
			dItemJson.put("slug", entry.getKey());
			for (int pageId : entry.getValue()) {
				ditemsLinks.put("Page: " + idPage.get(pageId));
			}
			dItemJson.put("links", ditemsLinks);
			ditems.put(dItemJson);

		}

		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

}
