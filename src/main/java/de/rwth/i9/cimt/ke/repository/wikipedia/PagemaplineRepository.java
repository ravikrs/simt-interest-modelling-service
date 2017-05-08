package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.Pagemapline;

public interface PagemaplineRepository extends CrudRepository<Pagemapline, Long> {
	List<Pagemapline> findByLemma(String lemma);

	List<Pagemapline> findById(Long id);

	List<Pagemapline> findByName(String name);

	List<Pagemapline> findByPageId(Integer pageid);

	List<Pagemapline> findByStem(String stem);

}
