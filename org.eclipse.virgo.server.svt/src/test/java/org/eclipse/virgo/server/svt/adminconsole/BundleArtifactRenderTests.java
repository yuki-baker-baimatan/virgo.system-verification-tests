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

public class BundleArtifactRenderTests extends AbstractWebTests {

	private static HtmlPage adminPage = null;
	private static WebClient webClient = null;
	private static DefaultCredentialsProvider credentialsProvider = null;

	@BeforeClass
	public static void testContextSetup() throws IOException {
		webClient = new WebClient();
		webClient.setThrowExceptionOnScriptError(false);
		credentialsProvider = (DefaultCredentialsProvider) webClient
				.getCredentialsProvider();
		credentialsProvider.addCredentials(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
		adminPage = webClient.getPage(AbstractWebTests.URL);

		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"./../test-apps/bundleA.jar"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("./../test-apps/bundleA.jar");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);
	}

	@Test
	public void bundleArtifactRenderInAdminConsole() throws Exception {
		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement bundleElement = adminPage.getElementById("bundle");
		assertNotNull("Bundle Artifacts are not Listed out Properly",
				bundleElement);
		if (bundleElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = bundleElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement bundleHtmlElement = iterator.next();
				if (bundleHtmlElement.getTagName().equals("img")) {
					bundleHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					assertBundleArtifactRender("bundle", "bundleA", "1.0.0");
					assertBundleArtifactState("ACTIVE");
					assertBundleArtifactDependents(
							"com.springsource.javax.servlet", "2.5.0", "ACTIVE");
					break;
				}
			}
		}
	}

	private void assertBundleArtifactRender(String type, String name,
			String version) throws Exception {
		if (adminPage.getElementById("bundlebundlebundleA1.0.0") != null) {
			assertNotNull(String.format(
					"Bundle Artifact %s:%s:%s does not exist", type, name,
					version), adminPage
					.getElementById("bundlebundlebundleA1.0.0"));
			assertTrue(String.format(
					"Bundle Artifact %s is not rendered properly", name),
					adminPage.getElementById("bundlebundlebundleA1.0.0")
							.getTextContent().contains("bundleA"));
			assertTrue(String.format(
                "Bundle Artifact %s is not rendered properly", name),
                adminPage.getElementById("bundlebundlebundleA1.0.0")
                        .getTextContent().contains("1.0.0"));
		}
	}

	private void assertBundleArtifactState(String state) throws IOException {
		if (adminPage.getElementById("bundlebundlebundleA1.0.0") != null) {
			assertNotNull(
					"User installed bundle artifact is not listed out in admin console",
					adminPage.getElementById("bundlebundlebundleA1.0.0"));
			if (adminPage.getElementById("bundlebundlebundleA1.0.0") != null) {
				HtmlElement bundleChildElement = adminPage
						.getElementById("bundlebundlebundleA1.0.0");
				Iterator<HtmlElement> subIterator = bundleChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement bundleChildHtmlElement = subIterator.next();
					if (bundleChildHtmlElement.getTagName().equals("img")) {
						bundleChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(
								String
										.format(
												"Bundle Artifact state %s is not Exists",
												state),
								adminPage
										.getElementById(
												"bundlebundlebundleA1.0.0bundlebundleA1.0.0ACTIVE")
										.getTextContent().contains(state));
						assertTrue(
								String
										.format(
												"Bundle Artifact %s is not showing up the User installed status",
												"bundleA 1.0.0"),
								adminPage
										.getElementById(
												"bundlebundlebundleA1.0.0bundlebundleA1.0.0user.installed")
										.getTextContent().contains(
												"user.installed"));
						break;
					}
				}
			}
		}
	}

	private void assertBundleArtifactDependents(String name, String version, String state)
			throws IOException {
		if (adminPage.getElementById("bundlebundlebundleA1.0.0") != null) {
			assertNotNull(
					String
							.format(
									"Bundle Dependent artifact %s is not listed out in admin console",
									name),
					adminPage
							.getElementById("bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0"));
			if (adminPage
					.getElementById("bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0") != null) {
				HtmlElement bundleChildChildElement = adminPage
						.getElementById("bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0");
				Iterator<HtmlElement> subIterator = bundleChildChildElement
						.getFirstChild().getAllHtmlChildElements().iterator();
				while (subIterator.hasNext()) {
					HtmlElement bundleChildChildHtmlElement = subIterator
							.next();
					if (bundleChildChildHtmlElement.getTagName().equals("img")) {
						bundleChildChildHtmlElement.click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
						assertTrue(
								String
										.format(
												"Dependent Bundle Artifact %s does not exist",
												name),
								adminPage
										.getElementById(
												"bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0")
										.getTextContent().contains(name));
						assertTrue(
                            String
                                    .format(
                                            "Dependent Bundle Artifact %s does not exist",
                                            name),
                            adminPage
                                    .getElementById(
                                            "bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0")
                                    .getTextContent().contains(version));
						assertTrue(
								String
										.format(
												"Dependent Bundle Artifact %s state %s does not exist",
												name, state),
								adminPage
										.getElementById(
												"bundlebundlebundleA1.0.0bundlecom.springsource.javax.servlet2.5.0bundlecom.springsource.javax.servlet2.5.0ACTIVE")
										.getTextContent().contains(state));
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
				getObjectName("bundle", "bundleA", "1.0.0"), "uninstall",
				params, signature);
	}
}
