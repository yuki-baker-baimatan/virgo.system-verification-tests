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

public class ParArtifactRefreshTests extends AbstractWebTests {
	private static HtmlPage adminPage = null;
	private static DefaultCredentialsProvider credentialsProvider = null;
	private static WebClient webClient = null;

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
				"./../test-apps/appA.par"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("./../test-apps/appA.par");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);
	}

	@Test
	public void parArtifactRefreshForASN() throws Exception {
		assertParArtifactASNBeforeRefresh("par", "appA", "1.0.0");
		assertParArtifactStateBeforeRefresh("par", "appA", "1.0.0", "ACTIVE");
		byte[] byteArray = FileCopyUtils.copyToByteArray(new File(
				"src/test/resources/appA.par"));
		final HtmlForm uploadform = adminPage.getFormByName("uploadForm");
		final HtmlFileInput applicationField = uploadform
				.getInputByName("application");
		applicationField.setData(byteArray);
		applicationField.setValueAttribute("src/test/resources/appA.par");
		final HtmlSubmitInput uploadButton = uploadform
				.getElementById("deploy_application_submit_button");
		uploadform.submit(uploadButton);

		adminPage = webClient.getPage(AbstractWebTests.URL);
		webClient.waitForBackgroundJavaScriptStartingBefore(500);
		HtmlElement bundleElement = adminPage.getElementById("par");
		assertNotNull("Par Artifacts are not Listed out Properly",
				bundleElement);
		if (bundleElement.getFirstChild() != null) {
			Iterator<HtmlElement> iterator = bundleElement.getFirstChild()
					.getAllHtmlChildElements().iterator();
			while (iterator.hasNext()) {
				HtmlElement bundleHtmlElement = iterator.next();
				if (bundleHtmlElement.getTagName().equals("img")) {
					bundleHtmlElement.click();
					webClient.waitForBackgroundJavaScriptStartingBefore(500);
					if (adminPage.getElementById("parparappA_Updated2.0.0") != null) {
						adminPage.getElementById("parparappA_Updated2.0.0")
								.click();
						assertNotNull(
								"Refresh button is not listed out in admin console",
								adminPage.getElementById("dijit_form_Button_2"));
						adminPage.getElementById("dijit_form_Button_2").click();
						waitForArtifactState("par","appA_Updated","2.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
						assertParArtifactASNAfterRefresh("par", "appA_Updated",
								"2.0.0");
						assertParArtifactStateAfterRefresh("par",
								"appA_Updated", "2.0.0", "ACTIVE");
					}

					break;
				}
			}
		}
	}

	private void assertParArtifactASNBeforeRefresh(String type, String name,
			String version) throws MalformedObjectNameException, IOException,
			Exception {
		assertTrue(String.format("Par Artifact with ASN %s does not exist",
				name), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertParArtifactStateBeforeRefresh(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Par Artifact %s:%s:%s is not in state After Refresh %s", type,
				name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertParArtifactASNAfterRefresh(String type, String name,
			String version) throws MalformedObjectNameException, IOException,
			Exception {
		assertTrue(String.format("Par Artifact with ASN %s does not exist",
				name), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertParArtifactStateAfterRefresh(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Par Artifact %s:%s:%s is not in state After Refresh %s", type,
				name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		Object[] params = {};
		String[] signature = {};
		getMBeanServerConnection().invoke(
				getObjectName("par", "appA_Updated", "2.0.0"), "uninstall",
				params, signature);
	}
}
