package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.Page;

public interface PageRepository extends CrudRepository<Page, Long> {
	List<Page> findById(Long id);

	List<Page> findByIsDisambiguation(Boolean isdisambiguation);

	List<Page> findByName(String name);

	List<Page> findByNameContaining(String pageName);

	List<Page> findByNameStartingWith(String pageName);

	List<Page> findByNameEquals(String pageName);

	List<Page> findByPageId(Integer pageid);

	List<Page> findByText(String text);
}
