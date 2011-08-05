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

public class ConfigurationArtifactRenderTests extends AbstractWebTests {

	private static HtmlPage adminPage = null;
	private static WebClient webClient = null;
	private static DefaultCredentialsProvider credentialsProvider = null;


	@BeforeClass
	public static void testContextSetup() throws Exception {
		webClient = new WebClient();
		webClient.setThrowExceptionOnScriptError(false);
		credentialsProvider = (DefaultCredentialsProvider) webClient
				.getCredentialsProvider();
		credentialsProvider.addCredentials(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
		adminPage = webClient.getPage(AbstractWebTests.URL);

		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"./../test-apps/foo.properties"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("./../test-apps/foo.properties");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);
	}


	@Test
	public void configurationArtifactRenderInAdminConsole() throws Exception {
		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement propertiesElement = adminPage.getElementById("configuration");
		assertNotNull("Configuration Artifacts are not Listed out Properly",
				propertiesElement);
		if (propertiesElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = propertiesElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement propertiesHtmlElement = iterator.next();
				if (propertiesHtmlElement.getTagName().equals("img")) {
					propertiesHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					assertConfigurationArtifactRender("configuration", "foo",
							"0.0.0");
					assertConfigurationArtifactState("foo 0.0.0", "ACTIVE");
					break;
				}
			}
		}
	}

	private void assertConfigurationArtifactRender(String type, String name,
			String version) throws Exception {
		if (adminPage.getElementById("configurationconfigurationfoo0.0.0") != null) {
			assertNotNull(String.format(
					"Configration Artifact %s:%s:%s does not exist", type,
					name, version), adminPage
					.getElementById("configurationconfigurationfoo0.0.0"));
			assertTrue(String.format(
					"Configuration Artifact %s does not render properly", name),
					adminPage.getElementById(
							"configurationconfigurationfoo0.0.0")
							.getTextContent().contains("foo"));
			assertTrue(String.format(
                "Configuration Artifact %s does not render properly", name),
                adminPage.getElementById(
                        "configurationconfigurationfoo0.0.0")
                        .getTextContent().contains("0.0.0"));
		}
	}

	private void assertConfigurationArtifactState(String name, String state)
			throws IOException {
		HtmlElement propertiesChildElement = adminPage
				.getElementById("configurationconfigurationfoo0.0.0");
		if (propertiesChildElement != null) {
			Iterator<HtmlElement> subIterator = propertiesChildElement
					.getFirstChild().getAllHtmlChildElements().iterator();
			while (subIterator.hasNext()) {
				HtmlElement propertiesChildHtmlElement = subIterator.next();
				if (propertiesChildHtmlElement.getTagName().equals("img")) {
					propertiesChildHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					assertTrue(
							String
									.format(
											"Configuration Artifact %s state %s is not render",
											name, state),
							adminPage
									.getElementById(
											"configurationconfigurationfoo0.0.0configurationfoo0.0.0ACTIVE")
									.getTextContent().contains(state));
					assertTrue(
							String
									.format(
											"Configuration Artifact %s is not showing up the User installed status",
											name),
							adminPage
									.getElementById(
											"configurationconfigurationfoo0.0.0configurationfoo0.0.0user.installed")
									.getTextContent().contains(
											"user.installed"));
					break;
				}
			}
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		Object[] params = {};
		String[] signature = {};
		getMBeanServerConnection().invoke(
				getObjectName("configuration", "foo", "0.0.0"), "uninstall",
				params, signature);
	}
}
