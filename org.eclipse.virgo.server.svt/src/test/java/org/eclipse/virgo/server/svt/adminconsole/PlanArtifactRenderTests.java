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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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


public class PlanArtifactRenderTests extends AbstractWebTests {

	private static HtmlPage adminPage = null;
	private static WebClient webClient = null;
	private static DefaultCredentialsProvider credentialsProvider = null;
	private static final String ARTIFACTSDIR = "./../test-apps";
	private static String[] artifactNames = new String[] { "bundleA.jar",
			"bundleB.war", "foo.properties" };

	@BeforeClass
	public static void testContextSetup() throws IOException,
			InterruptedException {
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
	public void planArtifactRenderInAdminConsole() throws Exception {
		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement parElement = adminPage.getElementById("plan");
		assertNotNull("Plan Artifacts are not Listed out Properly", parElement);
		if (parElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = parElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement parHtmlElement = iterator.next();
				if (parHtmlElement.getTagName().equals("img")) {
					parHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					assertPlanArtifactRender("plan", "scoped.plan", "1.0.0");
					assertPlanArtifactAttributesRender("ACTIVE", "scoped-atomic");
					assertPlanReferencedArtifactsRender();
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

	private void assertPlanArtifactRender(String type, String name,
			String version) throws Exception {
		if (adminPage.getElementById("planplanscoped.plan1.0.0") != null) {
			assertNotNull(String.format(
					"Plan Artifact %s:%s:%s does not exist", type, name,
					version), adminPage
					.getElementById("planplanscoped.plan1.0.0"));
			assertTrue(String.format("Plan Artifact %s does not render properly",
					name), adminPage.getElementById("planplanscoped.plan1.0.0")
					.getTextContent().contains("scoped.plan"));
			assertTrue(String.format("Plan Artifact %s does not render properly",
                name), adminPage.getElementById("planplanscoped.plan1.0.0")
                .getTextContent().contains("1.0.0"));
		}
	}

	private void assertPlanArtifactAttributesRender(String state, String scopedatomic) throws IOException {
		if (adminPage.getElementById("planplanscoped.plan1.0.0") != null) {
			assertNotNull(
					"User installed Plan artifact is not listed out in admin console",
					adminPage.getElementById("planplanscoped.plan1.0.0"));
			if (adminPage.getElementById("planplanscoped.plan1.0.0") != null) {
				HtmlElement parChildElement = adminPage
						.getElementById("planplanscoped.plan1.0.0");
				Iterator<HtmlElement> subIterator = parChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement parChildHtmlElement = subIterator.next();
					if (parChildHtmlElement.getTagName().equals("img")) {
						parChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(
								String
										.format(
												"Plan Artifact %s state %s is not exists",
												"scoped.plan 1.0.0", state),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0planscoped.plan1.0.0ACTIVE")
										.getTextContent().contains(state));
						assertTrue(
								String
										.format(
												"Par Artifact %s scope %s is not exists",
												"scoped.plan 1.0.0", scopedatomic),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0planscoped.plan1.0.0scoped-atomic")
										.getTextContent().contains(scopedatomic));
						
						assertTrue(
								String
										.format(
												"Par Artifact %s is not showing up the User installed status",
												"scoped.plan 1.0.0"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0planscoped.plan1.0.0user.installed")
										.getTextContent().contains(
												"user.installed"));
						break;
					}
				}
			}
		}
	}

	private void assertPlanReferencedArtifactsRender() throws IOException {
		if (adminPage.getElementById("planplanscoped.plan1.0.0") != null) {
			if (adminPage
					.getElementById("planplanscoped.plan1.0.0configurationfoo0.0.0") != null) {
				HtmlElement parChildChildElement = adminPage
						.getElementById("planplanscoped.plan1.0.0configurationfoo0.0.0");
				Iterator<HtmlElement> subIterator = parChildChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement parChildChildHtmlElement = subIterator.next();
					if (parChildChildHtmlElement.getTagName().equals("img")) {
						parChildChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(String.format(
								"Plan Referenced Artifact %s does not render correctly",
								"foo 0.0.0"), adminPage.getElementById(
								"planplanscoped.plan1.0.0configurationfoo0.0.0")
								.getTextContent().contains("foo"));
						assertTrue(String.format(
                            "Plan Referenced Artifact %s does not render correctly",
                            "foo 0.0.0"), adminPage.getElementById(
                            "planplanscoped.plan1.0.0configurationfoo0.0.0")
                            .getTextContent().contains("0.0.0"));
						assertTrue(
								String
										.format(
												"Plan Referenced Artifact  %s state %s is not render",
												"foo 0.0.0", "ACTIVE"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0configurationfoo0.0.0configurationfoo0.0.0ACTIVE")
										.getTextContent().contains("ACTIVE"));
						break;
					}
				}
			}

			if (adminPage
					.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-bundleA1.0.0") != null) {
				HtmlElement parChildChildElement = adminPage
						.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-bundleA1.0.0");
				Iterator<HtmlElement> subIterator = parChildChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement parChildChildHtmlElement = subIterator.next();
					if (parChildChildHtmlElement.getTagName().equals("img")) {
						parChildChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(String.format(
								"Plan Referenced Artifact %s does not render correctly",
								"scoped.plan-1-bundleA 1.0.0"), adminPage.getElementById(
								"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleA1.0.0")
								.getTextContent().contains("scoped.plan-1-bundleA"));
						assertTrue(String.format(
                            "Plan Referenced Artifact %s does not render correctly",
                            "scoped.plan-1-bundleA 1.0.0"), adminPage.getElementById(
                            "planplanscoped.plan1.0.0bundlescoped.plan-1-bundleA1.0.0")
                            .getTextContent().contains("1.0.0"));
						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s  state %s is not render",
												"scoped.plan-1-bundleA 1.0.0", "ACTIVE"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleA1.0.0bundlescoped.plan-1-bundleA1.0.0ACTIVE")
										.getTextContent().contains("ACTIVE"));
						break;
					}
				}
			}

			if (adminPage.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0") != null) {
				HtmlElement parChildChildElement = adminPage
						.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0");
				Iterator<HtmlElement> subIterator = parChildChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement parChildChildHtmlElement = subIterator.next();
					if (parChildChildHtmlElement.getTagName().equals("img")) {
						parChildChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(String.format(
								"Plan Referenced Artifact %s does not render correctly",
								"scoped.plan-1-bundleB 1.0.0"), adminPage.getElementById(
								"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0")
								.getTextContent().contains("scoped.plan-1-bundleB"));
						assertTrue(String.format(
                            "Plan Referenced Artifact %s does not render correctly",
                            "scoped.plan-1-bundleB 1.0.0"), adminPage.getElementById(
                            "planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0")
                            .getTextContent().contains("1.0.0"));
						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s state  %s is not render",
												"scoped.plan-1-bundleB 1.0.0", "ACTIVE"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0bundlescoped.plan-1-bundleB1.0.0ACTIVE")
										.getTextContent().contains("ACTIVE"));

						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s type %s is not render",
												"scoped.plan-1-bundleB 1.0.0", "Web Bundle"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0bundlescoped.plan-1-bundleB1.0.0artifact-type: Web Bundle")
										.getTextContent()
										.contains("Web Bundle"));

						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s contextpath %s is not render",
												"scoped.plan-1-bundleB 1.0.0",
												"/bundleBContext"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-bundleB1.0.0bundlescoped.plan-1-bundleB1.0.0org.eclipse.virgo.web.contextPath: /bundleBContext")
										.getTextContent().contains(
												"/bundleBContext"));
						break;
					}
				}
			}

			if (adminPage
					.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-synthetic.context1.0.0") != null) {
				HtmlElement parChildChildElement = adminPage
						.getElementById("planplanscoped.plan1.0.0bundlescoped.plan-1-synthetic.context1.0.0");
				Iterator<HtmlElement> subIterator = parChildChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement parChildChildHtmlElement = subIterator.next();
					if (parChildChildHtmlElement.getTagName().equals("img")) {
						parChildChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s does not render",
												"scoped.plan-1-synthetic.context 1.0.0"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-synthetic.context1.0.0")
										.getTextContent()
										.contains(
												"scoped.plan-1-synthetic.context"));
						assertTrue(
                            String
                                    .format(
                                            "Plan Referenced Artifact %s does not render",
                                            "scoped.plan-1-synthetic.context 1.0.0"),
                            adminPage
                                    .getElementById(
                                            "planplanscoped.plan1.0.0bundlescoped.plan-1-synthetic.context1.0.0")
                                    .getTextContent()
                                    .contains(
                                            "1.0.0"));
						assertTrue(
								String
										.format(
												"Plan Referenced Artifact %s state  %s is not render",
												"scoped.plan-1-synthetic.context 1.0.0",
												"ACTIVE"),
								adminPage
										.getElementById(
												"planplanscoped.plan1.0.0bundlescoped.plan-1-synthetic.context1.0.0bundlescoped.plan-1-synthetic.context1.0.0ACTIVE")
										.getTextContent().contains("ACTIVE"));
						break;
					}
				}
			}
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
