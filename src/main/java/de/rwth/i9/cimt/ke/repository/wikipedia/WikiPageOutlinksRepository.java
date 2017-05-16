package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageOutlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageOutlinksId;

public interface WikiPageOutlinksRepository extends WikipediaBaseRepository<WikiPageOutlinks, WikiPageOutlinksId> {
	List<WikiPageOutlinks> findById(WikiPageOutlinksId id);

}
