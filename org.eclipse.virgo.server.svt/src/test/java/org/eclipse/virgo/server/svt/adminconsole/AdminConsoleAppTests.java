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
import java.io.IOException;
import javax.management.MalformedObjectNameException;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.Test;


public class AdminConsoleAppTests extends AbstractWebTests {

	@Test
	public void adminApplicationProperties() throws Exception {
		waitForMBeanRegister("plan", "org.eclipse.virgo.apps.admin.plan", "3.0.0", HALF_SECOND,
				TWO_MINUTES);
		UrlWaitLatch.waitFor("http://localhost:8080/admin");
		assertAdminConsoleArtifactExists("plan", "org.eclipse.virgo.apps.admin.plan", "3.0.0");
		assertAdminConsoleArtifactState("plan", "org.eclipse.virgo.apps.admin.plan", "3.0.0",
				"ACTIVE");
	}

	private void assertAdminConsoleArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"admin console plan artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertAdminConsoleArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"admin console plan artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}
}
