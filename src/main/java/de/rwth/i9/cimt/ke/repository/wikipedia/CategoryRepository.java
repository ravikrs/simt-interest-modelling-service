package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.Category;

public interface CategoryRepository extends CrudRepository<Category, Long> {
	List<Category> findByNameIsLike(String categoryName);

	List<Category> findByNameContaining(String categoryName);

	List<Category> findByName(String categoryName);

	List<Category> findById(Long id);

	List<Category> findByPageId(Integer pageid);

}
