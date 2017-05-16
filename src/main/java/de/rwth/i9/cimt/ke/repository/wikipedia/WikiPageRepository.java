package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPage;

public interface WikiPageRepository extends WikipediaBaseRepository<WikiPage, Long> {
	List<WikiPage> findById(Long id);

	List<WikiPage> findByIsDisambiguation(Boolean isdisambiguation);

	List<WikiPage> findByName(String name);

	List<WikiPage> findByNameContaining(String pageName);

	List<WikiPage> findByNameStartingWith(String pageName);

	List<WikiPage> findByNameEquals(String pageName);

	List<WikiPage> findByPageId(Integer pageid);

	List<WikiPage> findByText(String text);
}
