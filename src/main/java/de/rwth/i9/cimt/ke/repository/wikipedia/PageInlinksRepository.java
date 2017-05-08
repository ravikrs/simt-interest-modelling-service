package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.PageInlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.PageInlinksId;

public interface PageInlinksRepository extends CrudRepository<PageInlinks, PageInlinksId> {
	List<PageInlinks> findById(PageInlinksId id);

}
