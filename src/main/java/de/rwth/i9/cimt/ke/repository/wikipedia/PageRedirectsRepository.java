package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.PageRedirects;
import de.rwth.i9.cimt.ke.model.wikipedia.PageRedirectsId;

public interface PageRedirectsRepository extends CrudRepository<PageRedirects, PageRedirectsId> {
	List<PageRedirects> findById(PageRedirectsId id);

}
