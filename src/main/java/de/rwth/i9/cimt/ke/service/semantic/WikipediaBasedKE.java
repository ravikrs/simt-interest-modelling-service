package de.rwth.i9.cimt.ke.service.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.constants.AuthorInterestType;
import de.rwth.i9.cimt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.cimt.ke.lib.model.Keyword;
import de.rwth.i9.cimt.ke.lib.util.WordCount;
import de.rwth.i9.cimt.ke.model.eval.AuthorInterests;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPagemapline;
import de.rwth.i9.cimt.ke.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.cimt.ke.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.cimt.ke.util.MapSortUtil;
import de.rwth.i9.cimt.ke.util.WikipediaUtil;
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

	public List<Keyword> getkeywordsWBForAuthor(int authorId, KeyphraseExtractionAlgorithm algorithmName,
			int numKeywords) throws WikiApiException {
		List<Keyword> returnedKp = new ArrayList<>();
		Map<Integer, Double> pageScore = new HashMap<>();
		Map<Integer, String> pageName = new HashMap<>();
		List<AuthorInterests> authorInterests = authorInterestsRepo.findByAuthorIdAndKeAlgorithm(authorId,
				algorithmName.toString());
		for (AuthorInterests authorInterest : authorInterests) {
			List<WordCount> wordCounts = WordCount.parseIntoList(authorInterest.getAuthorInterest());
			double sum = 0.0;
			for (WordCount wc : wordCounts) {
				sum += wc.getY();
			}
			for (WordCount wc : wordCounts) {
				if (simpleWikiDb.existsPage(wc.getX())) {
					Page p = simpleWikiDb.getPage(wc.getX());
					// add normalised score of wikipages
					if (pageScore.containsKey(p.getPageId())) {
						pageScore.put(p.getPageId(), pageScore.get(p.getPageId()) + wc.getY() / sum);
					} else {
						pageScore.put(p.getPageId(), wc.getY() / sum);
					}
					if (!pageName.containsKey(p.getPageId())) {
						pageName.put(p.getPageId(), p.getTitle().getEntity());
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
						if (pageScore.containsKey(wpm.getPageId())) {
							pageScore.put(wpm.getPageId(), pageScore.get(wpm.getPageId()) + wc.getY() / sum);
						} else {
							pageScore.put(wpm.getPageId(), wc.getY() / sum);
						}
					}
					for (int wpmPageId : wpmPageIds) {
						Page p = simpleWikiDb.getPage(wpmPageId);
						if (!pageName.containsKey(p.getPageId())) {
							pageName.put(p.getPageId(), p.getTitle().getEntity());
						}
					}
				}
			}
		}

		pageScore = MapSortUtil.sortByValueDesc(pageScore);
		int counter = 0;
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			counter++;
			Keyword kw = new Keyword(pageName.get(entry.getKey()), entry.getValue());
			returnedKp.add(kw);
			if (counter >= numKeywords)
				break;
		}

		return returnedKp;

	}

	public List<Keyword> getkeywordsDefaultForAuthor(int authorId, KeyphraseExtractionAlgorithm algorithmName,
			int numKeywords) {
		List<Keyword> returnedKp = new ArrayList<>();
		List<Keyword> keyphrases = new ArrayList<>();
		List<AuthorInterests> authorInterests = authorInterestsRepo.findByAuthorIdAndKeAlgorithm(authorId,
				algorithmName.toString());
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

	public List<Keyword> getkeywordsForAuthor(int authorId, KeyphraseExtractionAlgorithm algorithmName, int numKeywords,
			String authorInterestType) throws WikiApiException {

		AuthorInterestType interestType = AuthorInterestType.fromString(authorInterestType);
		if (interestType.equals(AuthorInterestType.WIKIPEDIA_BASED)) {
			return this.getkeywordsWBForAuthor(authorId, algorithmName, numKeywords);
		} else {
			return this.getkeywordsDefaultForAuthor(authorId, algorithmName, numKeywords);
		}
	}

}
