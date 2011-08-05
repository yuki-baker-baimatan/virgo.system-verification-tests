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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

@Ignore("bug 353996: user region configuration properties are not visible via JMX")
public class ConfigurationArtifactRefreshTests extends AbstractWebTests {

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
	public void configurationArtifactRefresh()
			throws MalformedObjectNameException, IOException, Exception {
		assertConfigurationArtifactExists("configuration", "foo", "0.0.0");
		assertConfigurationArtifactRefresh();
	}

	private void assertConfigurationArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"Configuration Artifact %s:%s:%s does not exist", type, name,
				version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
		// The following no longer works. See bug 353996.
		assertTrue(getMBeanServerConnection().getAttribute(
				getConfigObjectName("foo"), "Properties").toString().contains(
				"bar"));
	}

	private void assertConfigurationArtifactRefresh()
			throws MalformedObjectNameException, IOException, Exception {
		assertTrue(getMBeanServerConnection().getAttribute(
				getConfigObjectName("foo"), "Properties").toString().contains(
				"bar"));
		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"src/test/resources/foo.properties"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("src/test/resources/foo.properties");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);

		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement bundleElement = adminPage.getElementById("configuration");
		assertNotNull("Configuration Artifacts are not Listed out Properly",
				bundleElement);
		if (bundleElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = bundleElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement bundleHtmlElement = iterator.next();
				if (bundleHtmlElement.getTagName().equals("img")) {
					bundleHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					if (adminPage
							.getElementById("configurationconfigurationfoo0.0.0") != null) {
						adminPage.getElementById(
								"configurationconfigurationfoo0.0.0").click();
						assertNotNull(
								"Refresh button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_2"));
						adminPage.getElementById("dijit_form_Button_2").click();
						webClient
								.waitForBackgroundJavaScriptStartingBefore(500);
					}

					break;
				}
			}
		}
		assertTrue("Configuration Artifact Refresh is Not Successful",
				getMBeanServerConnection().getAttribute(
						getConfigObjectName("foo"), "Properties").toString()
						.contains("boo"));
	}

	private ObjectName getConfigObjectName(String name)
			throws MalformedObjectNameException {
		return new ObjectName(String.format(
				"org.eclipse.virgo.kernel:type=Configuration,name=%s", name));
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
