package org.jenkinsci.plugins.p4.review;

public enum ReviewProp {

	STATUS("status", true), 
	CHANGE("change", true), 
	LABEL("label", false), // reserved by Jenkins for Slave Axes
	REVIEW("review", true),
	PASS("pass", true),
	FAIL("fail", true);

	public static String NAMESPACE = "p4.";
	final private String prop;
	final private boolean load;

	ReviewProp(String prop, boolean load) {
		this.prop = prop;
		this.load = load;
	}

	public String toString() {
		return NAMESPACE + prop;
	}

	public String getProp() {
		return prop;
	}

	public boolean isLoad() {
		return load;
	}

	public static boolean isProp(String prop) {
		for (ReviewProp p : ReviewProp.values()) {
			if (p.isLoad() && p.getProp().equals(prop))
				return true;
		}
		return false;
	}
}
