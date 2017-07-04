package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiPageOutlinks;
import de.rwth.i9.simt.im.model.wikipedia.WikiPageOutlinksId;

public interface WikiPageOutlinksRepository extends WikipediaBaseRepository<WikiPageOutlinks, WikiPageOutlinksId> {
	List<WikiPageOutlinks> findById(WikiPageOutlinksId id);

}
