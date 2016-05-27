package org.jenkinsci.plugins.p4.review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;

public class ReviewAction implements Action {

	private final AbstractProject<?, ?> project;

	public AbstractProject<?, ?> getProject() {
		return project;
	}

	public ReviewAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public String getIconFileName() {
		return "/plugin/p4/icons/p4.png";
	}

	public String getDisplayName() {
		return "Build Review";
	}

	public String getUrlName() {
		return "review";
	}

	/**
	 * Jelly Method
	 * 
	 * @return
	 */
	public List<StringParameterValue> getAvailableParameters() {
		List<StringParameterValue> stringParameters = new ArrayList<StringParameterValue>();

		for (ParameterDefinition parameterDefinition : getParameterDefinitions()) {
			StringParameterValue stringParameter = new StringParameterValue(parameterDefinition.getName(),
					parameterDefinition.getDescription());
			stringParameters.add(stringParameter);
		}

		return stringParameters;
	}

	public void doBuildSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {

		JSONObject formData = req.getSubmittedForm();
		if (!formData.isEmpty()) {
			doBuild(req, rsp);
		}
	}

	public void doBuild(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {

		project.checkPermission(AbstractProject.BUILD);

		List<ParameterValue> values = new ArrayList<ParameterValue>();
		List<ParameterDefinition> defs = new ArrayList<ParameterDefinition>();

		Enumeration<?> names = req.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			defs.add(new StringParameterDefinition(name, null));
		}

		for (ParameterDefinition d : defs) {
			StringParameterValue value = (StringParameterValue) d.createValue(req);
			if (value != null && value.value != null && !value.value.isEmpty()) {
				values.add(value);
			}
		}

		// Schedule build
		TimeDuration delay = new TimeDuration(project.getQuietPeriod());
		CauseAction cause = new CauseAction(new Cause.UserIdCause());
		ParametersAction params = new ParametersAction(values);

		Jenkins j = Jenkins.getInstance();
		if (j != null) {
			Queue queue = j.getQueue();
			queue.schedule(project, delay.getTime(), params, cause);

			// send the user back to the job top page.
			rsp.sendRedirect("../");
		}
	}

	private List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> swarm = new ArrayList<ParameterDefinition>();

		// Swarm parameters
		swarm.add(new StringParameterDefinition(ReviewProp.REVIEW.getProp(), null));
		swarm.add(new StringParameterDefinition(ReviewProp.CHANGE.getProp(), null));
		swarm.add(new StringParameterDefinition(ReviewProp.STATUS.getProp(), null));
		swarm.add(new StringParameterDefinition(ReviewProp.PASS.getProp(), null));
		swarm.add(new StringParameterDefinition(ReviewProp.FAIL.getProp(), null));

		// Custom parameters
		swarm.add(new StringParameterDefinition(ReviewProp.LABEL.toString(), null));

		return swarm;
	}

	final static Logger LOGGER = Logger.getLogger(ReviewAction.class.getName());
}
