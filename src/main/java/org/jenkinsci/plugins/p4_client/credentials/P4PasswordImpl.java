package org.jenkinsci.plugins.p4_client.credentials;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jenkinsci.plugins.p4_client.client.ConnectionHelper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsScope;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class P4PasswordImpl extends P4StandardCredentials {

	/**
	 * Ensure consistent serialisation.
	 */
	private static final long serialVersionUID = 1L;

	@NonNull
	private final Secret password;

	@DataBoundConstructor
	public P4PasswordImpl(@CheckForNull CredentialsScope scope,
			@CheckForNull String id, @CheckForNull String description,
			@CheckForNull String p4port, TrustImpl ssl,
			@CheckForNull String username, @CheckForNull String password) {
		super(scope, id, description, p4port, ssl, username);
		this.password = Secret.fromString(password);
	}

	@NonNull
	public Secret getPassword() {
		return password;
	}

	@Extension
	public static class DescriptorImpl extends P4CredentialsDescriptor {

		@Override
		public String getDisplayName() {
			return "Perforce Password Credential";
		}

		public FormValidation doTestConnection(
				@QueryParameter("p4port") String p4port,
				@QueryParameter("ssl") String ssl,
				@QueryParameter("trust") String trust,
				@QueryParameter("username") String username,
				@QueryParameter("password") String password)
				throws IOException, ServletException {
			try {
				TrustImpl sslTrust;
				sslTrust = ("true".equals(ssl)) ? new TrustImpl(trust) : null;

				P4PasswordImpl test = new P4PasswordImpl(null, null, null,
						p4port, sslTrust, username, password);

				ConnectionHelper p4 = new ConnectionHelper(test);
				p4.login();
				p4.logout();
				if (!p4.login()) {
					return FormValidation
							.error("Authentication Error: Unable to login.");
				}
				if (!p4.checkVersion(20121)) {
					return FormValidation
							.error("Server version is too old (min 2012.1)");
				}
				return FormValidation.ok("Success");
			} catch (Exception e) {
				return FormValidation.error(e.getMessage());
			}
		}
	}
}
