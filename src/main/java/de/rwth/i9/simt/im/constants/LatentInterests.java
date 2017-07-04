package de.rwth.i9.simt.im.constants;

public enum LatentInterests {
	CTG_PARENT, CTG_PARENT_SIBLING, CTG_PARENT_DESCENDENT, CTG_PARENT_SIBLING_DESCENDENT, DEFAULT;

	public static LatentInterests fromString(String value) {
		if ("CTG_PARENT".equalsIgnoreCase(value))
			return CTG_PARENT;

		if ("CTG_PARENT_SIBLING".equalsIgnoreCase(value))
			return CTG_PARENT_SIBLING;

		if ("CTG_PARENT_DESCENDENT".equalsIgnoreCase(value))
			return CTG_PARENT_DESCENDENT;

		if ("CTG_PARENT_SIBLING_DESCENDENT".equalsIgnoreCase(value))
			return CTG_PARENT_SIBLING_DESCENDENT;

		if ("DEFAULT".equalsIgnoreCase(value))
			return CTG_PARENT_DESCENDENT;

		return null;
	}

}
