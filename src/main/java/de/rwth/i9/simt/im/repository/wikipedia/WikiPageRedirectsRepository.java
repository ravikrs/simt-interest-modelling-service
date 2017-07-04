package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiPageRedirects;
import de.rwth.i9.simt.im.model.wikipedia.WikiPageRedirectsId;

public interface WikiPageRedirectsRepository extends WikipediaBaseRepository<WikiPageRedirects, WikiPageRedirectsId> {
	List<WikiPageRedirects> findById(WikiPageRedirectsId id);

}
