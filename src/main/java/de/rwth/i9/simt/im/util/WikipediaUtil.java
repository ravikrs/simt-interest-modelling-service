package de.rwth.i9.simt.im.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rwth.i9.cimt.nlp.util.StemmerEn;

public class WikipediaUtil {
	private static final Logger log = LoggerFactory.getLogger(WikipediaUtil.class);

	private static Set<Long> genericWikipediaCategories;

	public static boolean isGenericWikipediaCategory(int categoryPageId) {
		if (genericWikipediaCategories == null) {
			loadGenericWikipediaCategories();
		}
		return genericWikipediaCategories.contains(new Long(categoryPageId));

	}

	private static void loadGenericWikipediaCategories() {
		genericWikipediaCategories = new HashSet<>();
		InputStream is = WikipediaUtil.class.getClassLoader().getResourceAsStream("/WikipediaSpecificCategories.txt");
		List<String> data;
		try {
			data = IOUtils.readLines(is);
			for (String d : data) {
				if (!d.isEmpty() && !d.startsWith("#")) {
					long categoryPageId = Long.parseLong(d);
					genericWikipediaCategories.add(categoryPageId);
				}
			}
		} catch (IOException e) {
			log.equals(ExceptionUtils.getStackTrace(e));
		}
	}

	public static String toWikipediaArticleName(String token) {
		return token.trim().replaceAll("\\s+", "_");
	}

	public static String toWikipediaArticleStem(String token) {
		return StemmerEn.stemToken(token.trim().replaceAll("\\s+", "_"));
	}
}
