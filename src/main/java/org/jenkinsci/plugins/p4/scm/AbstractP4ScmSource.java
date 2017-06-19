package org.jenkinsci.plugins.p4.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.p4.PerforceScm;
import org.jenkinsci.plugins.p4.browsers.P4Browser;
import org.jenkinsci.plugins.p4.client.ClientHelper;
import org.jenkinsci.plugins.p4.populate.Populate;
import org.jenkinsci.plugins.p4.review.P4Review;
import org.jenkinsci.plugins.p4.tasks.CheckoutStatus;
import org.jenkinsci.plugins.p4.workspace.ManualWorkspaceImpl;
import org.jenkinsci.plugins.p4.workspace.Workspace;
import org.jenkinsci.plugins.p4.workspace.WorkspaceSpec;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractP4ScmSource extends SCMSource {

	private static Logger logger = Logger.getLogger(AbstractP4ScmSource.class.getName());
	public static final String scmSourceClient = "jenkins-master";

	protected final String credential;

	private String includes;
	private final String charset;
	private final String format;
	private Populate populate;

	public AbstractP4ScmSource(String id, String credential, String charset, String format) {
		super(id);

		this.credential = credential;
		this.charset = charset;
		this.format = format;
	}

	@DataBoundSetter
	public void setPopulate(Populate populate) {
		this.populate = populate;
	}

	@DataBoundSetter
	public void setIncludes(String includes) {
		this.includes = includes;
	}

	public String getCredential() {
		return credential;
	}

	public String getIncludes() {
		return includes;
	}

	public String getCharset() {
		return charset;
	}

	public String getFormat() {
		return format;
	}

	public Populate getPopulate() {
		return populate;
	}

	public abstract P4Browser getBrowser();

	public abstract List<P4Head> getHeads(@NonNull TaskListener listener) throws Exception;

	public abstract List<P4ChangeRequestSCMHead> getTags(@NonNull TaskListener listener) throws Exception;

	public Workspace getWorkspace(List<String> paths) {
		String client = getFormat();

		StringBuffer sb = new StringBuffer();
		for (String path : paths) {
			String view = "+" + path + "/Jenkinsfile" + " //" + client + "/Jenkinsfile";
			sb.append(view).append("\n");
		}

		WorkspaceSpec spec = new WorkspaceSpec(false, false, false, false,
				false, false, null, "LOCAL", sb.toString(), null,
				null, null, true);

		return new ManualWorkspaceImpl(getCharset(), false, client, spec);
	}

	@Override
	public PerforceScm build(SCMHead head, SCMRevision revision) {
		if (head instanceof P4Head) {
			P4Head perforceHead = (P4Head) head;
			List<String> paths = perforceHead.getPaths();
			Workspace workspace = getWorkspace(paths);
			PerforceScm scm = new PerforceScm(credential, workspace, null, populate, getBrowser());
			return scm;
		}

		if (head instanceof P4ChangeRequestSCMHead) {
			P4ChangeRequestSCMHead perforceTag = (P4ChangeRequestSCMHead) head;
			List<String> paths = perforceTag.getPaths();
			Workspace workspace = getWorkspace(paths);
			PerforceScm scm = new PerforceScm(credential, workspace, null, populate, getBrowser());

			P4Review review = new P4Review(head.getName(), CheckoutStatus.SHELVED);
			scm.setReview(review);
			return scm;
		}
		throw new IllegalArgumentException("SCMHead not a Perforce instance!");
	}

	@Override
	protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer, @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {
		try {
			List<P4Head> heads = getHeads(listener);

			List<P4ChangeRequestSCMHead> tags = getTags(listener);
			heads.addAll(tags);

			for (P4Head head : heads) {
				// null criteria means that all branches match.
				if (criteria == null) {
					// get revision and add observe
					SCMRevision revision = getRevision(head, listener);
					observer.observe(head, revision);
				} else {
					ClientHelper p4 = new ClientHelper(getOwner(), credential, listener, scmSourceClient, charset);
					SCMSourceCriteria.Probe probe = new P4Probe(p4, head);
					if (criteria.isHead(probe, listener)) {
						// get revision and add observe
						SCMRevision revision = getRevision(head, listener);
						observer.observe(head, revision);
					}
				}
				// check for user abort
				checkInterrupt();
			}
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	protected List<String> getIncludePaths() {
		String[] array = includes.split("[\\r\\n]+");
		return Arrays.asList(array);
	}

	public P4Revision getRevision(P4Head head, TaskListener listener) throws Exception {
		try (ClientHelper p4 = new ClientHelper(getOwner(), credential, listener, scmSourceClient, charset)) {
			long change = -1;
			for (String path : head.getPaths()) {
				long c = p4.getHead(path + "/...");
				change = (c > change) ? c : change;
			}
			P4Revision revision = new P4Revision(head, change);
			return revision;
		}
	}
}
