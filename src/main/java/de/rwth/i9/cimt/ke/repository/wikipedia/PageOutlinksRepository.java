package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.PageOutlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.PageOutlinksId;

public interface PageOutlinksRepository extends CrudRepository<PageOutlinks, PageOutlinksId> {
	List<PageOutlinks> findById(PageOutlinksId id);

}
