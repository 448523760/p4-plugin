package org.jenkinsci.plugins.p4.scm;

import jenkins.scm.api.SCMRevision;

/**
 * Created by pallen on 13/02/2017.
 */
public class P4Revision extends SCMRevision {
	private final long change;

	P4Revision(P4Head branch, long change) {
		super(branch);
		this.change = change;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		P4Revision that = (P4Revision) o;
		return change == that.change && getHead().equals(that.getHead());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (int) (change ^ (change >>> 32));
	}

	@Override
	public String toString() {
		return Long.toString(change);
	}
}
