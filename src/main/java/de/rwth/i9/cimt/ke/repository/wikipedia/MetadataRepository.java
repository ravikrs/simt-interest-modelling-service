package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.Metadata;

public interface MetadataRepository extends CrudRepository<Metadata, Long> {

	List<Metadata> findById(Long id);

	List<Metadata> findByLanguage(String language);
}
