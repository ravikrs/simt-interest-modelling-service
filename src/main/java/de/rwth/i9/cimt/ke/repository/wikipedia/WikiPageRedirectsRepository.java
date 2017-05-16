package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageRedirects;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageRedirectsId;

public interface WikiPageRedirectsRepository extends WikipediaBaseRepository<WikiPageRedirects, WikiPageRedirectsId> {
	List<WikiPageRedirects> findById(WikiPageRedirectsId id);

}
