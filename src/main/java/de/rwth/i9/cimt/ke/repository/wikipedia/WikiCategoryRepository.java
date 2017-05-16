package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiCategory;

public interface WikiCategoryRepository extends WikipediaBaseRepository<WikiCategory, Long> {
	List<WikiCategory> findByNameIsLike(String categoryName);

	List<WikiCategory> findByNameContaining(String categoryName);

	List<WikiCategory> findByName(String categoryName);

	List<WikiCategory> findById(Long id);

	List<WikiCategory> findByPageId(Integer pageid);

}
