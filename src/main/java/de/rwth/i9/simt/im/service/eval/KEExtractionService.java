package de.rwth.i9.simt.im.service.eval;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.rwth.i9.simt.im.model.eval.Publication;
import de.rwth.i9.simt.im.model.eval.PublicationKeywords;
import de.rwth.i9.simt.im.model.wikipedia.WikiPagemapline;
import de.rwth.i9.simt.im.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.simt.im.repository.eval.AuthorPublicationsRepository;
import de.rwth.i9.simt.im.repository.eval.AuthorRepository;
import de.rwth.i9.simt.im.repository.eval.PublicationKeywordsRepository;
import de.rwth.i9.simt.im.repository.eval.PublicationRepository;
import de.rwth.i9.simt.im.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.simt.im.util.MapSortUtil;
import de.rwth.i9.simt.im.util.WikipediaUtil;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.jate.Jate;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.rake.Rake;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.textrank.LanguageEnglish;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.textrank.TextRankWordnet;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.unsupervised.graphranking.TextRank;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.unsupervised.graphranking.TopicRank;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.unsupervised.topicclustering.KeyCluster;
import de.rwth.i9.simt.ke.lib.algorithm.kpextraction.unsupervised.topicclustering.TopicalPageRank;
import de.rwth.i9.simt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.simt.ke.lib.model.Keyword;
import de.rwth.i9.simt.ke.lib.util.WordCount;
import de.rwth.i9.simt.nlp.opennlp.OpenNLPImplSpring;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import uk.ac.shef.dcs.jate.model.JATETerm;

@Service("keExtractionService")
public class KEExtractionService {
	private static final Logger log = LoggerFactory.getLogger(KEExtractionService.class);

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
	private Wikipedia simpleWikiDb;
	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;
	@Autowired
	OpenNLPImplSpring openNLPImplSpring;
	private @Value("${cimt.home}") String cimtHome;
	@Autowired
	LanguageEnglish languageEnglish;

	public void runKEAlgorithm() {
		int pageSize = 10;
		long count = publicationRepository.count();
		for (int i = 0; i * pageSize < count; i++) {
			Page<Publication> publications = publicationRepository.findAll(new PageRequest(i, pageSize));
			//	Publication pub = publicationRepository.findOne(2186);
			for (Publication pub : publications) {
				log.info("keyphrase extraction. PubID -> " + pub.getId());
				//				if (pub.getId() < 186) {
				//					continue;
				//				}
				KeyphraseExtractionAlgorithm[] alg = new KeyphraseExtractionAlgorithm[] {
						KeyphraseExtractionAlgorithm.KEY_CLUSTER, KeyphraseExtractionAlgorithm.RAKE,
						KeyphraseExtractionAlgorithm.TEXT_RANK, KeyphraseExtractionAlgorithm.TEXT_RANK_WORDNET,
						KeyphraseExtractionAlgorithm.TOPIC_RANK, KeyphraseExtractionAlgorithm.TOPICAL_PAGE_RANK };
				//		KeyphraseExtractionAlgorithm[] alg = new KeyphraseExtractionAlgorithm[] {
				//				KeyphraseExtractionAlgorithm.JATE_ATTF, KeyphraseExtractionAlgorithm.JATE_CHISQUARE,
				//				KeyphraseExtractionAlgorithm.JATE_CVALUE, KeyphraseExtractionAlgorithm.JATE_GLOSSEX,
				//				KeyphraseExtractionAlgorithm.JATE_RAKE, KeyphraseExtractionAlgorithm.JATE_RIDF,
				//				KeyphraseExtractionAlgorithm.JATE_TERMEX, KeyphraseExtractionAlgorithm.JATE_TFIDF,
				//				KeyphraseExtractionAlgorithm.JATE_TTF, KeyphraseExtractionAlgorithm.JATE_WEIRDNESS };
				for (KeyphraseExtractionAlgorithm k : alg) {
					if (k.equals(KeyphraseExtractionAlgorithm.DEFAULT)) {
						continue;
					}
					try {
						List<WordCount> refinedKeywords = this.performKeyphraseExtraction(pub.getTextContent(), k);
						String keyphrases = WordCount.formatIntoString(refinedKeywords);
						PublicationKeywords pubKw = new PublicationKeywords();
						pubKw.setKeAlgorithm(k.toString());
						pubKw.setPublicationId(pub.getId());
						pubKw.setKeywordTokens(keyphrases);
						pubKw.setIsWikipediaBased(false);
						publicationKeywordsRepository.save(pubKw);
						//wikipedia pages
						refinedKeywords = this.getWikipediaArticles(refinedKeywords);
						keyphrases = WordCount.formatIntoString(refinedKeywords);
						pubKw = new PublicationKeywords();
						pubKw.setKeAlgorithm(k.toString());
						pubKw.setPublicationId(pub.getId());
						pubKw.setKeywordTokens(keyphrases);
						pubKw.setIsWikipediaBased(true);
						publicationKeywordsRepository.save(pubKw);
					} catch (Exception e) {
						log.error("Couldnot perform keyphrase extraction. PubID -> " + pub.getId() + " algorithm -> "
								+ k.toString());
						e.printStackTrace();
					}

				}
			}

		}

	}

	public List<WordCount> performKeyphraseExtraction(String textContent, KeyphraseExtractionAlgorithm keAlgo)
			throws Exception {
		List<WordCount> refinedKeywords = new ArrayList<>();
		List<Keyword> keyphrases = new ArrayList<>();
		List<JATETerm> terms = new ArrayList<JATETerm>();
		switch (keAlgo) {
		case KEY_CLUSTER:
			keyphrases = KeyCluster.performKeyClusterKE(textContent, openNLPImplSpring);
			break;
		case JATE_ATTF:
			terms = Jate.TTFAlgo(textContent, cimtHome);
			break;
		case JATE_CHISQUARE:
			terms = Jate.ChiSquareAlgo(textContent, cimtHome);
			break;
		case JATE_CVALUE:
			terms = Jate.CValueAlgo(textContent, cimtHome);
			break;
		case JATE_GLOSSEX:
			terms = Jate.GlossExAlgo(textContent, cimtHome);
			break;
		case JATE_RAKE:
			terms = Jate.RAKEAlgo(textContent, cimtHome);
			break;
		case JATE_RIDF:
			terms = Jate.RIDFAlgo(textContent, cimtHome);
			break;
		case JATE_TERMEX:
			terms = Jate.TermExAlgo(textContent, cimtHome);
			break;
		case JATE_TTF:
			terms = Jate.TTFAlgo(textContent, cimtHome);
			break;
		case JATE_TFIDF:
			terms = Jate.TFIDFAlgo(textContent, cimtHome);
			break;
		case JATE_WEIRDNESS:
			terms = Jate.WeirdnessAlgo(textContent, cimtHome);
			break;
		case RAKE:
			keyphrases = Rake.extractKeyword(textContent, openNLPImplSpring);
			break;
		case TEXT_RANK:
			keyphrases = TextRank.performTextRankKE(textContent, openNLPImplSpring);
			break;
		case TEXT_RANK_WORDNET:
			keyphrases = TextRankWordnet.extractKeywordTextRankWordnet(textContent, openNLPImplSpring, languageEnglish,
					cimtHome + "/LexSemResources/WordNet3.0", true);
			break;
		case TOPIC_RANK:
			keyphrases = TopicRank.performTopicRankKE(textContent, openNLPImplSpring);
			break;
		case TOPICAL_PAGE_RANK:
			keyphrases = TopicalPageRank.performTopicalPageRankKE(textContent, openNLPImplSpring, cimtHome);
			break;
		default:
			break;
		}

		if (!keyphrases.isEmpty()) {
			Collections.sort(keyphrases, Keyword.KeywordComparatorDesc);

		} else if (!terms.isEmpty()) {
			for (JATETerm term : terms) {
				Keyword keyword = new Keyword(term.getString(), term.getScore());
				keyphrases.add(keyword);
			}
			Collections.sort(keyphrases, Keyword.KeywordComparatorDesc);
		}
		int i = 0;
		for (Keyword token : keyphrases) {
			refinedKeywords.add(new WordCount(token.getToken(), token.getWeight()));
			i++;
			if (i >= 30) {
				break;
			}

		}
		return refinedKeywords;
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
