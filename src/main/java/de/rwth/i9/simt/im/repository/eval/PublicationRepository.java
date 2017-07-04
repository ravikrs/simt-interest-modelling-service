package de.rwth.i9.simt.im.repository.eval;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.rwth.i9.simt.im.model.eval.Publication;

public interface PublicationRepository extends PagingAndSortingRepository<Publication, Integer> {
	List<Publication> findByTitle(String title);

}
