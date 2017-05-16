package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPagemapline;

public interface WikiPagemaplineRepository extends WikipediaBaseRepository<WikiPagemapline, Long> {
	List<WikiPagemapline> findByLemma(String lemma);

	List<WikiPagemapline> findById(Long id);

	List<WikiPagemapline> findByName(String name);

	List<WikiPagemapline> findByPageId(Integer pageid);

	List<WikiPagemapline> findByStem(String stem);

}
