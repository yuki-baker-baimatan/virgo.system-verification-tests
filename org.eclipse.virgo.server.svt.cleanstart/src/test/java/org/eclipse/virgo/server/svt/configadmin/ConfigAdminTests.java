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

package org.eclipse.virgo.server.svt.configadmin;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class ConfigAdminTests extends AbstractWebTests {

	private static final String APPS_DIR = "src/test/resources";
	private static final String[] APPLICATION_NAMES = new String[] { "configadminservice.war" };
	
	@BeforeClass
	public static void startUp() {
		shutdownServer();
		startServer();
	}
	
	@AfterClass
	public static void shutDown() {
		shutdownServer();
	}

	@Test
	public void testConfigAdminPersistedData() throws Exception {

		copyApplicationsToPickup(getPickupDir(), APPS_DIR, APPLICATION_NAMES);

		UrlWaitLatch
				.waitFor("http://localhost:8080/configadminservice/list.htm");
		getTester()
				.beginAt("http://localhost:8080/configadminservice/list.htm");
		getTester().assertTextPresent("spring");
		getTester().assertTextPresent("source");
	}

}
