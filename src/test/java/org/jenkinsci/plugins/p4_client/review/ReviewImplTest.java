package org.jenkinsci.plugins.p4_client.review;

import static org.junit.Assert.assertEquals;
import hudson.model.FreeStyleProject;

import org.jenkinsci.plugins.p4_client.PerforceScm;
import org.jenkinsci.plugins.p4_client.credentials.P4PasswordImpl;
import org.jenkinsci.plugins.p4_client.populate.AutoCleanImpl;
import org.jenkinsci.plugins.p4_client.populate.Populate;
import org.jenkinsci.plugins.p4_client.workspace.StaticWorkspaceImpl;
import org.jenkinsci.plugins.p4_client.workspace.Workspace;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ReviewImplTest {

	private final String credential = "id";
	private final static String P4PORT = "localhost:1999";

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	@Test
	public void testStaticReviewImpl() throws Exception {

		P4PasswordImpl auth = new P4PasswordImpl(CredentialsScope.SYSTEM,
				credential, "desc", P4PORT, null, "jenkins", "jenkins");
		SystemCredentialsProvider.getInstance().getCredentials().add(auth);
		SystemCredentialsProvider.getInstance().save();

		String client = "test.ws";
		FreeStyleProject project = jenkins
				.createFreeStyleProject("StaticReview");
		Workspace workspace = new StaticWorkspaceImpl("none", client);
		Populate populate = new AutoCleanImpl(true, true, null);
		PerforceScm scm = new PerforceScm(credential, workspace, populate);
		project.setScm(scm);
		project.save();

		HtmlPage page = jenkins.createWebClient().getPage(project, "review");
		HtmlElement review = page.getElementByName("review");
		assertEquals(review.getAttribute("type"), "text");
	}
}
