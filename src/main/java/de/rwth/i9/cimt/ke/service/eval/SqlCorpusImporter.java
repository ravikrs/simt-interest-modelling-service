package de.rwth.i9.cimt.ke.service.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.model.eval.Publication;
import de.rwth.i9.cimt.ke.repository.eval.PublicationRepository;

@Service("sqlCorpusImporter")
public class SqlCorpusImporter {
	private static final Logger log = LoggerFactory.getLogger(SqlCorpusImporter.class);

	@Autowired
	PublicationRepository publicationRepository;

	public void runCorpusImporter() throws FileNotFoundException, IOException {
		String inputDirectory = "C:\\rks\\Thesis\\Datasets\\AutomaticKeyphraseExtraction-master\\Test";
		File dir = new File(inputDirectory);
		String[] extensions = new String[] { "abstr" };
		Collection<File> files = FileUtils.listFiles(dir, extensions, true);
		for (File file : files) {
			log.info("###########################");
			log.info("Starting processing: " + file.getName());
			String title = FilenameUtils.removeExtension(file.getName()).trim();
			if (!publicationRepository.findByTitle(title).isEmpty()) {
				log.info("Publication already exists" + title);
			}
			FileReader fr = new FileReader(file);
			List<String> lines = IOUtils.readLines(fr);
			fr.close();
			StringBuilder textContent = new StringBuilder();
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				textContent.append(line.trim() + " ");
			}

			Publication pub = new Publication();
			pub.setTitle(title);
			pub.setTextContent(textContent.toString().trim());
			pub = publicationRepository.save(pub);
		}

		extensions = new String[] { "contr", "uncontr" };
		files = FileUtils.listFiles(dir, extensions, true);
		for (File file : files) {
			log.info("###########################");
			log.info("Starting Keyword processing: " + file.getName());
			String title = FilenameUtils.removeExtension(file.getName()).trim();
			StringBuilder keywordContent = new StringBuilder();
			List<Publication> publications = publicationRepository.findByTitle(title);
			if (!publications.isEmpty()) {
				List<String> lines = IOUtils.readLines(new FileReader(file));
				for (String line : lines) {
					if (lines.isEmpty()) {
						continue;
					}
					keywordContent.append(line.trim().replace(";", ",") + " ");
				}
				Publication pub = publications.get(0);
				StringBuilder sb = new StringBuilder();
				if (pub.getDefaultKeywords() != null && !pub.getDefaultKeywords().isEmpty()
						&& !pub.getDefaultKeywords().equalsIgnoreCase("null")) {
					sb.append(pub.getDefaultKeywords() + ",");
				}
				sb.append(keywordContent.toString());
				pub.setDefaultKeywords(sb.toString());
				publicationRepository.save(pub);

			} else {
				log.info("Publication doesnot exists" + title);
			}

		}

	}

	public void updateKeywords() throws FileNotFoundException, IOException {
		String inputDirectory = "C:\\rks\\Thesis\\Datasets\\AutomaticKeyphraseExtraction-master\\Test";
		File dir = new File(inputDirectory);
		String[] extensions = new String[] { "uncontr" };
		Collection<File> files = FileUtils.listFiles(dir, extensions, true);
		for (File file : files) {
			log.info("###########################");
			log.info("Starting Keyword processing: " + file.getName());
			String title = FilenameUtils.removeExtension(file.getName()).trim();
			StringBuilder keywordContent = new StringBuilder();
			List<Publication> publications = publicationRepository.findByTitle(title);
			if (!publications.isEmpty()) {
				List<String> lines = IOUtils.readLines(new FileReader(file));
				for (String line : lines) {
					if (lines.isEmpty()) {
						continue;
					}
					keywordContent.append(line.trim().replace(";", ",") + " ");
				}
				Publication pub = publications.get(0);
				StringBuilder sb = new StringBuilder();

				sb.append(keywordContent.toString());
				pub.setDefaultKeywords(sb.toString());
				publicationRepository.save(pub);

			} else {
				log.info("Publication doesnot exists" + title);
			}

		}

	}

}
