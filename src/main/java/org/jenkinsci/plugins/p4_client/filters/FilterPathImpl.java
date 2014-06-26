package org.jenkinsci.plugins.p4_client.filters;

import hudson.Extension;
import hudson.model.AutoCompletionCandidates;

import org.jenkinsci.plugins.p4_client.client.NavigateHelper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class FilterPathImpl extends Filter {

	private final String path;

	@DataBoundConstructor
	public FilterPathImpl(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Extension
	public static final class DescriptorImpl extends FilterDescriptor {

		@Override
		public String getDisplayName() {
			return "Exclude changes from Depot path";
		}

		public AutoCompletionCandidates doAutoCompletePath(
				@QueryParameter String value) {
			return NavigateHelper.getPath(value);
		}
	}
}
