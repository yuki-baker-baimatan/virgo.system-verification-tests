/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.server.svt.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;

public class PlanArtifactStartAndStopTests extends AbstractWebTests {

	private static HtmlPage adminPage = null;
	private static DefaultCredentialsProvider credentialsProvider = null;
	private static WebClient webClient = null;
	private static final String ARTIFACTSDIR = "./../test-apps";
	private static String[] artifactNames = new String[] { "bundleA.jar",
			"bundleB.war", "foo.properties" };

	@BeforeClass
	public static void testContextSetup() throws Exception {
		copyPlanReferencedArtifactsToRepository(getRepositoryUsrDir(),
				ARTIFACTSDIR, artifactNames);
		Thread.sleep(10000);
		webClient = new WebClient();
		webClient.setThrowExceptionOnScriptError(false);
		credentialsProvider = (DefaultCredentialsProvider) webClient
				.getCredentialsProvider();
		credentialsProvider.addCredentials(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
		adminPage = webClient.getPage(AbstractWebTests.URL);

		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"./../test-apps/scopedplan.plan"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("./../test-apps/scopedplan.plan");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);
	}

	@Test
	public void planArtifactStop() throws MalformedObjectNameException,
			Exception {
		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement bundleElement = adminPage.getElementById("plan");
		assertNotNull("Plan Artifacts are not Listed out Properly",
				bundleElement);
		if (bundleElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = bundleElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement bundleHtmlElement = iterator.next();
				if (bundleHtmlElement.getTagName().equals("img")) {
					bundleHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					HtmlElement planNode = adminPage.getElementById("planplanscoped.plan1.0.0");
					if (planNode != null) {
						clickPlan(planNode);
						assertNotNull(
								"Stop button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_1"));
						adminPage.getElementById("dijit_form_Button_1").click();
						waitForArtifactState("plan","scoped.plan","1.0.0","RESOLVED",HALF_SECOND, TWO_MINUTES);
						assertPlanArtifactState("plan", "scoped.plan", "1.0.0",
								"RESOLVED");
						assertPlanReferencedArtifactState("bundle", "bundleA",
								"1.0.0", "RESOLVED");
						assertPlanReferencedArtifactState("bundle", "bundleB",
								"1.0.0", "RESOLVED");
						assertPlanReferencedArtifactState("configuration", "foo",
								"0.0.0", "RESOLVED");
						assertPlanReferencedArtifactState("bundle",
								"scoped.plan-synthetic.context", "1.0.0",
								"RESOLVED");
					}

					break;
				}
			}
		}
	}
	
	private void clickPlan(HtmlElement planNode) throws IOException {
		// 1st child -> 3rd child -> 2nd child -> click
		getHtmlChildElement(getHtmlChildElement(getHtmlChildElement(planNode, 0), 2), 1).click();
	}

	@Test
	public void planArtifactStart() throws MalformedObjectNameException,
			IOException, Exception {
		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement bundleElement = adminPage.getElementById("plan");
		assertNotNull("Plan Artifacts are not Listed out Properly",
				bundleElement);
		if (bundleElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = bundleElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement bundleHtmlElement = iterator.next();
				if (bundleHtmlElement.getTagName().equals("img")) {
					bundleHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					HtmlElement planNode = adminPage.getElementById("planplanscoped.plan1.0.0");
					if (planNode != null) {
						clickPlan(planNode);

						assertNotNull(
								"Start button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_0"));
						adminPage.getElementById("dijit_form_Button_0").click();
						waitForArtifactState("plan","scoped.plan","1.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
						assertPlanArtifactState("plan", "scoped.plan", "1.0.0",
								"ACTIVE");
						assertPlanReferencedArtifactState("bundle", "bundleA",
								"1.0.0", "ACTIVE");
						assertPlanReferencedArtifactState("bundle", "bundleB",
								"1.0.0", "ACTIVE");
						assertPlanReferencedArtifactState("configuration", "foo",
								"0.0.0", "ACTIVE");
						assertPlanReferencedArtifactState("bundle",
								"scoped.plan-synthetic.context", "1.0.0",
								"ACTIVE");
					}

					break;
				}
			}
		}
	}

	private static void copyPlanReferencedArtifactsToRepository(String usrDir,
			String artifactsDir, String[] artifactsNames) throws IOException,
			InterruptedException {
		for (String artifactName : artifactsNames) {
			FileCopyUtils.copy(new File(artifactsDir, artifactName), new File(
					usrDir, artifactName));
		}
	}

	private void assertPlanArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format("Plan Artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanReferencedArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(getObjectName("plan", "scoped.plan", "1.0.0"),
						"Dependents");
		for (ObjectName objectName : objectNameList) {
			assertEquals(String.format(
					"Plan Referenced Artifact %s:%s:%s is not in state %s",
					type, name, version, state), state,
					getMBeanServerConnection()
							.getAttribute(objectName, "State"));
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		Object[] params = {};
		String[] signature = {};
		getMBeanServerConnection().invoke(
				getObjectName("plan", "scoped.plan", "1.0.0"), "uninstall",
				params, signature);
	}
}
