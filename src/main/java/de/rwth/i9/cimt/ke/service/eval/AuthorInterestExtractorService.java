package de.rwth.i9.cimt.ke.service.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.lib.constants.KeyphraseExtractionAlgorithm;
import de.rwth.i9.cimt.ke.lib.util.WordCount;
import de.rwth.i9.cimt.ke.model.eval.Author;
import de.rwth.i9.cimt.ke.model.eval.AuthorInterests;
import de.rwth.i9.cimt.ke.model.eval.AuthorPublications;
import de.rwth.i9.cimt.ke.model.eval.PublicationKeywords;
import de.rwth.i9.cimt.ke.repository.eval.AuthorInterestsRepository;
import de.rwth.i9.cimt.ke.repository.eval.AuthorPublicationsRepository;
import de.rwth.i9.cimt.ke.repository.eval.AuthorRepository;
import de.rwth.i9.cimt.ke.repository.eval.PublicationKeywordsRepository;
import de.rwth.i9.cimt.ke.repository.eval.PublicationRepository;
import de.rwth.i9.cimt.nlp.opennlp.OpenNLPImplSpring;

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

	public void runIMAlgorithmforKEAlgorithm(KeyphraseExtractionAlgorithm keAlgorithm) {
		int pageSize = 50;
		long count = authorRepository.count();
		for (int i = 0; i * pageSize < count; i++) {
			for (Author auth : authorRepository.findAll(new PageRequest(i, pageSize))) {
				this.computeScoreStats(auth.getId(), keAlgorithm);
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

	public void computeScoreStats(Integer authorId, KeyphraseExtractionAlgorithm keAlgorithm) {
		Map<String, Double> termSumScoreMap = new HashMap<>();
		log.info("######## Computing best keywords ###### AuthorId -> " + authorId);
		for (AuthorPublications authPub : authorPublicationsRepository.findByAuthorId(authorId)) {
			List<PublicationKeywords> pubKeywords = publicationKeywordsRepository
					.findByPublicationIdAndKeAlgorithm(authPub.getPublicationId(), keAlgorithm);
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
		Object[] a = termSumScoreMap.entrySet().toArray();
		Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<String, Double>) o2).getValue()
						.compareTo(((Map.Entry<String, Double>) o1).getValue());
			}
		});
		List<WordCount> interests = new ArrayList<>();
		for (int iter = 0; iter < a.length && iter < sizeLimit; iter++) {
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) a[iter];
			interests.add(new WordCount(entry.getKey(), entry.getValue()));
		}
		AuthorInterests authInt = new AuthorInterests();
		authInt.setAuthorId(authorId);
		authInt.setAuthorInterest(WordCount.formatIntoString(interests));
		authInt.setKeAlgorithm(keAlgorithm.toString());
		authorInterestsRepository.save(authInt);

	}

}
