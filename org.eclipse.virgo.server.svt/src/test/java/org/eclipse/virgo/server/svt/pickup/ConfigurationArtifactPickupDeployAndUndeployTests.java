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

package org.eclipse.virgo.server.svt.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import javax.management.MalformedObjectNameException;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.Test;

public class ConfigurationArtifactPickupDeployAndUndeployTests extends
		AbstractWebTests {

	private static final String APPS_DIR = "./../test-apps";
	private static final String[] APPLICATION_NAMES = new String[] { "foo.properties" };

	@Test
	public void configurationArtifactDeploy() throws Exception {
		copyApplicationsToPickup(getPickupDir(), APPS_DIR, APPLICATION_NAMES);
		waitForMBeanRegister("configuration", "foo", "0.0.0", HALF_SECOND,
				TWO_MINUTES);
		assertConfigurationArtifactExists("configuration", "foo", "0.0.0");
		waitForArtifactState("configuration", "foo", "0.0.0", "ACTIVE",
				HALF_SECOND, TWO_MINUTES);
		assertConfigurationArtifactState("configuration", "foo", "0.0.0",
				"ACTIVE");
	}

	@Test
	public void configurationArtifactUndeploy() throws Exception {
		deleteApplicationsFromPickup(getPickupDir(), APPLICATION_NAMES);
		waitForMBeanDeRegister("configuration", "foo", "0.0.0", HALF_SECOND,
				TWO_MINUTES);
		assertConfigurationArtifactNotExists("configuration", "foo", "0.0.0");
	}

	private void assertConfigurationArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"Configuration Artifact %s:%s:%s does not exist", type, name,
				version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertConfigurationArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Configuration Artifact %s:%s:%s is not in state %s", type,
				name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertConfigurationArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format(
				"Configuration Artifact %s:%s:%s is still exists", type, name,
				version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

}
