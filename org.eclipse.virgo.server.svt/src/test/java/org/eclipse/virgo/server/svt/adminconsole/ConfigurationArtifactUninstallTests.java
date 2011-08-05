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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.management.MalformedObjectNameException;

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

public class ConfigurationArtifactUninstallTests extends AbstractWebTests {

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
	public void configurationArtifactUninstall()
			throws MalformedObjectNameException, Exception {
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
					HtmlElement configNode = adminPage
							.getElementById("configurationconfigurationfoo0.0.0");
					if (configNode != null) {
						clickConfig(configNode);
						assertNotNull(
								"Uninstall button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_3"));
						adminPage.getElementById("dijit_form_Button_3").click();
						waitForMBeanDeRegister("configuration","foo","0.0.0",HALF_SECOND, TWO_MINUTES);
						assertConfigurationArtifactNotExists("configuration",
								"foo", "0.0.0");
					}
					break;
				}
			}
		}
	}

	private void assertConfigurationArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format(
				"Configuration Artifact %s:%s:%s is still exist", type, name,
				version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}
	
	private void clickConfig(HtmlElement configNode) throws IOException {
		// 1st child -> 3rd child -> 2nd child -> click
		getHtmlChildElement(getHtmlChildElement(getHtmlChildElement(configNode, 0), 2), 1).click();
	}

}
