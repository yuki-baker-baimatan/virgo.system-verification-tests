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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import javax.management.MalformedObjectNameException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;

public class ConfigurationArtifactDeployTests extends AbstractWebTests {

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
	}

	@Test
	public void configurationArtifactDeploy() throws Exception {
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
		waitForArtifactState("configuration","foo","0.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
		assertConfigurationArtifactState("configuration", "foo", "0.0.0",
				"ACTIVE");
	}

	private void assertConfigurationArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertConfigurationArtifactExists(type, name, version);
		assertEquals(String.format(
				"Configuration Artifact %s:%s:%s is not in state %s", type,
				name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertConfigurationArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"Configuration Artifact %s:%s:%s does not exist", type, name,
				version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
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
