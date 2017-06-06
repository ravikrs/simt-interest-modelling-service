package de.rwth.i9.cimt.ke.service.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.constants.AuthorInterestType;
import de.rwth.i9.cimt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.cimt.ke.lib.util.WordCount;
import de.rwth.i9.cimt.ke.model.eval.Author;
import de.rwth.i9.cimt.ke.model.eval.AuthorInterests;
import de.rwth.i9.cimt.ke.model.eval.AuthorPublications;
import de.rwth.i9.cimt.ke.model.eval.Publication;
import de.rwth.i9.cimt.ke.model.eval.PublicationKeywords;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPagemapline;
import de.rwth.i9.cimt.ke.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.cimt.ke.repository.eval.AuthorPublicationsRepository;
import de.rwth.i9.cimt.ke.repository.eval.AuthorRepository;
import de.rwth.i9.cimt.ke.repository.eval.PublicationKeywordsRepository;
import de.rwth.i9.cimt.ke.repository.eval.PublicationRepository;
import de.rwth.i9.cimt.ke.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.cimt.ke.util.MapSortUtil;
import de.rwth.i9.cimt.ke.util.WikipediaUtil;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("authorInterestExtractorService")
public class AuthorInterestExtractorService {
	private static final Logger log = LoggerFactory.getLogger(AuthorInterestExtractorService.class);
	@Autowired
	AuthorInterestsRepository authorInterestsRepository;
	@Autowired
	AuthorPublicationsRepository authorPublicationsRepository;
	@Autowired
	AuthorRepository authorRepository;
	@Autowired
	PublicationKeywordsRepository publicationKeywordsRepository;
	@Autowired
	PublicationRepository publicationRepository;
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;
	private @Value("${cimt.home}") String cimtHome;
	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;

	public void runIMAlgorithmforKEAlgorithm(KeyphraseExtractionAlgorithm keAlgorithm) {

		int pageSize = 75;

		long count = authorRepository.count();
		for (int i = 0; i * pageSize < count; i++) {
			for (Author auth : authorRepository.findAll(new PageRequest(i, pageSize))) {
				try {
					this.computeScoreStats(auth.getId(), keAlgorithm);
				} catch (WikiApiException ex) {
					log.error(ExceptionUtils.getFullStackTrace(ex));
				}
			}
		}

	}

	public void performInterestMingingForAllAuthors() {
		KeyphraseExtractionAlgorithm[] alg = new KeyphraseExtractionAlgorithm[] {
				KeyphraseExtractionAlgorithm.KEY_CLUSTER, KeyphraseExtractionAlgorithm.RAKE,
				KeyphraseExtractionAlgorithm.TEXT_RANK, KeyphraseExtractionAlgorithm.TEXT_RANK_WORDNET,
				KeyphraseExtractionAlgorithm.TOPIC_RANK, KeyphraseExtractionAlgorithm.TOPICAL_PAGE_RANK };
		for (KeyphraseExtractionAlgorithm keAlgo : alg) {
			this.runIMAlgorithmforKEAlgorithm(keAlgo);
		}
	}

	public void computeScoreStats(Integer authorId, KeyphraseExtractionAlgorithm keAlgorithm) throws WikiApiException {
		Map<String, Double> termSumScoreMap = new HashMap<>();
		log.info("######## Computing best keywords ###### AuthorId -> " + authorId);
		List<AuthorPublications> authPubs = authorPublicationsRepository.findByAuthorId(authorId);
		for (AuthorPublications authPub : authPubs) {
			List<PublicationKeywords> pubKeywords = publicationKeywordsRepository
					.findByPublicationIdAndKeAlgorithmAndIsWikipediaBased(authPub.getPublicationId(),
							keAlgorithm.toString(), false);
			for (PublicationKeywords pubKw : pubKeywords) {
				String keywords = pubKw.getKeywordTokens();
				List<WordCount> keyphrases = WordCount.parseIntoList(keywords);
				for (WordCount wc : keyphrases) {
					if (termSumScoreMap.containsKey(wc.getX())) {
						termSumScoreMap.put(wc.getX(), termSumScoreMap.get(wc.getX()) + wc.getY());
					} else {
						termSumScoreMap.put(wc.getX(), wc.getY());
					}
				}

			}

		}
		int sizeLimit = 50;
		termSumScoreMap = MapSortUtil.sortByValueDesc(termSumScoreMap);

		List<WordCount> interests = new ArrayList<>();
		Double sum = 0.0;
		int count = 0;
		for (Map.Entry<String, Double> entry : termSumScoreMap.entrySet()) {
			if (++count > sizeLimit) {
				break;
			}
			interests.add(new WordCount(entry.getKey(), entry.getValue()));
			sum += entry.getValue();
		}
		List<WordCount> normInterests = new ArrayList<>();
		for (WordCount wc : interests) {
			normInterests.add(new WordCount(wc.getX(), wc.getY() / sum));
		}
		AuthorInterests authInt = new AuthorInterests();
		authInt.setAuthorId(authorId);
		authInt.setAuthorInterest(WordCount.formatIntoString(normInterests));
		authInt.setKeAlgorithm(keAlgorithm.toString());
		authInt.setInterestType(AuthorInterestType.DEFAULT.toString());
		authorInterestsRepository.save(authInt);

		// save Wikipedia based interests as well
		termSumScoreMap = new HashMap<>();
		for (AuthorPublications authPub : authPubs) {
			List<PublicationKeywords> pubKeywords = publicationKeywordsRepository
					.findByPublicationIdAndKeAlgorithmAndIsWikipediaBased(authPub.getPublicationId(),
							keAlgorithm.toString(), true);
			for (PublicationKeywords pubKw : pubKeywords) {
				String keywords = pubKw.getKeywordTokens();
				List<WordCount> keyphrases = WordCount.parseIntoList(keywords);
				for (WordCount wc : keyphrases) {
					if (termSumScoreMap.containsKey(wc.getX())) {
						termSumScoreMap.put(wc.getX(), termSumScoreMap.get(wc.getX()) + wc.getY());
					} else {
						termSumScoreMap.put(wc.getX(), wc.getY());
					}
				}

			}

		}
		termSumScoreMap = MapSortUtil.sortByValueDesc(termSumScoreMap);

		interests = new ArrayList<>();
		sum = 0.0;
		count = 0;
		for (Map.Entry<String, Double> entry : termSumScoreMap.entrySet()) {
			if (++count > sizeLimit) {
				break;
			}
			interests.add(new WordCount(entry.getKey(), entry.getValue()));
			sum += entry.getValue();
		}
		normInterests = new ArrayList<>();
		for (WordCount wc : interests) {
			normInterests.add(new WordCount(wc.getX(), wc.getY() / sum));
		}
		authInt = new AuthorInterests();
		authInt.setAuthorId(authorId);
		authInt.setAuthorInterest(WordCount.formatIntoString(normInterests));
		authInt.setKeAlgorithm(keAlgorithm.toString());
		authInt.setInterestType(AuthorInterestType.WIKIPEDIA_BASED.toString());
		authorInterestsRepository.save(authInt);

	}

	public void computeScoreStatsDefault(Integer authorId) throws WikiApiException {
		Map<String, Double> termSumScoreMap = new HashMap<>();
		log.info("######## Computing best keywords ###### AuthorId -> " + authorId);
		List<AuthorPublications> authPubs = authorPublicationsRepository.findByAuthorId(authorId);
		for (AuthorPublications authPub : authPubs) {
			Publication pubs = publicationRepository.findOne(authPub.getPublicationId());
			String defaultKwds = pubs.getDefaultKeywords();
			if (defaultKwds != null && !defaultKwds.isEmpty()) {
				for (String defaultKwd : defaultKwds.split(",")) {
					defaultKwd = defaultKwd.toLowerCase().trim();
					if (termSumScoreMap.containsKey(defaultKwd)) {
						termSumScoreMap.put(defaultKwd, termSumScoreMap.get(defaultKwd) + 1.0);
					} else {
						termSumScoreMap.put(defaultKwd, 1.0);
					}
				}
			}
		}

		int sizeLimit = 50;
		termSumScoreMap = MapSortUtil.sortByValueDesc(termSumScoreMap);

		List<WordCount> interests = new ArrayList<>();
		Double sum = 0.0;
		int count = 0;
		for (Map.Entry<String, Double> entry : termSumScoreMap.entrySet()) {
			if (++count > sizeLimit) {
				break;
			}
			interests.add(new WordCount(entry.getKey(), entry.getValue()));
			sum += entry.getValue();
		}
		List<WordCount> normInterests = new ArrayList<>();
		for (WordCount wc : interests) {
			normInterests.add(new WordCount(wc.getX(), wc.getY() / sum));
		}
		AuthorInterests authInt = new AuthorInterests();
		authInt.setAuthorId(authorId);
		authInt.setAuthorInterest(WordCount.formatIntoString(normInterests));
		authInt.setKeAlgorithm(KeyphraseExtractionAlgorithm.DEFAULT.toString());
		authInt.setInterestType(AuthorInterestType.DEFAULT.toString());
		authorInterestsRepository.save(authInt);

		// save Wikipedia based interests as well

		List<WordCount> refinedKeywords = this.getWikipediaArticles(normInterests);
		authInt = new AuthorInterests();
		authInt.setAuthorId(authorId);
		authInt.setAuthorInterest(WordCount.formatIntoString(refinedKeywords));
		authInt.setKeAlgorithm(KeyphraseExtractionAlgorithm.DEFAULT.toString());
		authInt.setInterestType(AuthorInterestType.WIKIPEDIA_BASED.toString());
		authorInterestsRepository.save(authInt);

	}

	private List<WordCount> getWikipediaArticles(List<WordCount> wcs) throws WikiApiException {
		List<WordCount> normWikiInterests = new ArrayList<>();
		Map<String, Double> pageNameScore = new HashMap<>();
		for (WordCount wc : wcs) {

			if (simpleWikiDb.existsPage(wc.getX())) {
				de.tudarmstadt.ukp.wikipedia.api.Page p = simpleWikiDb.getPage(wc.getX());
				// add normalised score of wikipages
				normWikiInterests.add(new WordCount(p.getTitle().getEntity(), wc.getY()));
				String pageName = p.getTitle().getEntity();
				if (pageNameScore.containsKey(pageName)) {
					pageNameScore.put(pageName, pageNameScore.get(pageName) + wc.getY());
				} else {
					pageNameScore.put(pageName, wc.getY());
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

				}
				for (int wpmPageId : wpmPageIds) {
					de.tudarmstadt.ukp.wikipedia.api.Page p = simpleWikiDb.getPage(wpmPageId);
					String pageName = p.getTitle().getEntity();
					if (pageNameScore.containsKey(pageName)) {
						pageNameScore.put(pageName, pageNameScore.get(pageName) + wc.getY());
					} else {
						pageNameScore.put(pageName, wc.getY());
					}
				}
			}

		}
		pageNameScore = MapSortUtil.sortByValueDesc(pageNameScore);

		List<WordCount> interests = new ArrayList<>();
		double sum = 0.0;
		int count = 0, sizeLimit = 25;
		for (Map.Entry<String, Double> entry : pageNameScore.entrySet()) {
			if (++count > sizeLimit) {
				break;
			}
			interests.add(new WordCount(entry.getKey(), entry.getValue()));
			sum += entry.getValue();
		}
		List<WordCount> normInterests = new ArrayList<>();
		for (WordCount wc : interests) {
			normInterests.add(new WordCount(wc.getX(), wc.getY() / sum));
		}
		return normInterests;

	}

}
