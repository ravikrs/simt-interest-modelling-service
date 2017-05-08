package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.CategoryOutlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.CategoryOutlinksId;

public interface CategoryOutlinksRespository extends CrudRepository<CategoryOutlinks, CategoryOutlinksId> {
	List<CategoryOutlinks> findById(CategoryOutlinksId id);
}
