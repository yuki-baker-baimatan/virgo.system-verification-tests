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

package org.eclipse.virgo.server.svt.adminmetadata;

import net.sourceforge.jwebunit.html.Table;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;

public class ServerAdminMetadataTests extends AbstractWebTests {

	private static WebClient webClient = null;
	private static DefaultCredentialsProvider credentialsProvider = null;

	@Before
	public void testContextSetup() throws Exception {
		getTestContext().setBaseUrl(AbstractWebTests.URL);
		getTestContext().setAuthorization(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
		webClient = new WebClient();
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setThrowExceptionOnFailingStatusCode(false);
		credentialsProvider = (DefaultCredentialsProvider) webClient
				.getCredentialsProvider();
		credentialsProvider.addCredentials(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
	}

	@Test
	public void testServerProperties() throws Exception {
		Table propTable;
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setThrowExceptionOnFailingStatusCode(false);
		getTester()
				.beginAt("http://localhost:8080/admin/web/info/overview.htm");
		getTester().assertTextPresent("Server Properties");
		getTester().assertTablePresent("properties");

		propTable = new Table(new Object[][] { { "Name", "Value" },
				{ "Java VM Description", getJavaVMDescription() },
				{ "Java Version", getJavaVersion() },
				{ "Operating System", getOperatingSystem() },
				{ "Server Time Zone", System.getProperty("user.timezone") },
				{ "Virgo Server Version", getServerVersion() } });
		getTester().assertTableRowCountEquals("properties", 6);
		getTester().assertTableEquals("properties", propTable);
	}
}
