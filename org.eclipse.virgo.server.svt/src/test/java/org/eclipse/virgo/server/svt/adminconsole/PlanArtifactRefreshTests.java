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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.management.MalformedObjectNameException;
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

public class PlanArtifactRefreshTests extends AbstractWebTests {

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
	public void planArtifactRefreshForAtomicity() throws Exception {
		assertPlanArtifactAtomicityBeforeRefresh("plan", "scoped.plan",
				"1.0.0", true);
		assertPlanArtifactStateBeforeRefresh("plan", "scoped.plan", "1.0.0",
				"ACTIVE");
		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"src/test/resources/scopedplan.plan"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField
				.setValueAttribute("src/test/resources/scopedplan.plan");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);

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
					if (adminPage.getElementById("planplanscoped.plan1.0.0") != null) {
						adminPage.getElementById("planplanscoped.plan1.0.0")
								.click();
						assertNotNull(
								"Refresh button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_2"));
						adminPage.getElementById("dijit_form_Button_2").click();
						waitForArtifactState("plan","scoped.plan","1.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
						assertPlanArtifactAtomicityAfterRefresh("plan",
								"scoped.plan", "1.0.0", false);
						assertPlanArtifactStateAfterRefresh("plan",
								"scoped.plan", "1.0.0", "ACTIVE");
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

	private void assertPlanArtifactAtomicityBeforeRefresh(String type,
			String name, String version, Boolean atomic) throws IOException,
			Exception, MalformedObjectNameException {
		assertEquals(
				String
						.format(
								"Plan Artifact %s:%s:%s is not having Atomicity %s before Refresh ",
								type, name, version, atomic), atomic,
				getMBeanServerConnection().getAttribute(
						getObjectName(type, name, version), "Atomic"));
	}

	private void assertPlanArtifactStateBeforeRefresh(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertPlanArtifactExists(type, name, version);
		assertEquals(String.format(
				"Plan Artifact %s:%s:%s is not in state %s before refresh",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanArtifactStateAfterRefresh(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertPlanArtifactExists(type, name, version);
		assertEquals(String.format(
				"Plan Artifact %s:%s:%s is not in state %s after Refresh ",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanArtifactAtomicityAfterRefresh(String type,
			String name, String version, Boolean atomic) throws IOException,
			Exception, MalformedObjectNameException {
		assertEquals(
				String
						.format(
								"Plan Artifact %s:%s:%s is not having Atomicity %s after Refresh ",
								type, name, version, atomic), atomic,
				getMBeanServerConnection().getAttribute(
						getObjectName(type, name, version), "Atomic"));
	}

	private void assertPlanArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format("Plan Artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
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
