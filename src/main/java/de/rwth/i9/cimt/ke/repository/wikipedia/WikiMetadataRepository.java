package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiMetadata;

public interface WikiMetadataRepository extends WikipediaBaseRepository<WikiMetadata, Long> {

	List<WikiMetadata> findById(Long id);

	List<WikiMetadata> findByLanguage(String language);
}
