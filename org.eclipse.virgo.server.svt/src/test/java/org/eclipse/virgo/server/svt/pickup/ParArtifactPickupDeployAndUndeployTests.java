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
import javax.management.ObjectName;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.Test;

public class ParArtifactPickupDeployAndUndeployTests extends AbstractWebTests {

	private static final String APPS_DIR = "./../test-apps";
	private static final String[] APPLICATION_NAMES = new String[] { "appA.par" };

	@Test
	public void parArtifactDeploy() throws Exception {
		copyApplicationsToPickup(getPickupDir(), APPS_DIR, APPLICATION_NAMES);
		waitForMBeanRegister("par", "appA", "1.0.0", HALF_SECOND, TWO_MINUTES);
		assertParArtifactExists("par", "appA", "1.0.0");
		waitForArtifactState("par","appA","1.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
		assertParArtifactState("par", "appA", "1.0.0", "ACTIVE");
		assertParArtifactDependents("bundle", "bundleA", "1.0.0");
		assertParArtifactDependents("bundle", "bundleB", "1.0.0");
		assertParArtifactDependents("configuration", "foo", "0.0.0");
		assertParArtifactDependents("bundle", "appA-synthetic.context", "1.0.0");
	}

	@Test
	public void parArtifactUndeploy() throws Exception {
		deleteApplicationsFromPickup(getPickupDir(), APPLICATION_NAMES);
		Thread.sleep(5000);
		waitForMBeanDeRegister("par", "appA", "1.0.0", HALF_SECOND, TWO_MINUTES);
		assertParArtifactNotExists("par", "appA", "1.0.0");
	}

	private void assertParArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format("Par Artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertParArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format("Par Artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertParArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format("Par Artifact %s:%s:%s is still exists",
				type, name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertParArtifactDependents(String type, String name,
			String version) throws MalformedObjectNameException, IOException,
			Exception {
		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(getObjectName("par", "appA", "1.0.0"),
						"Dependents");
		for (ObjectName objectName : objectNameList) {
			assertTrue(String.format(
					"Par Dependent Artifact %s:%s:%s does not exist", type,
					name, version), getMBeanServerConnection().isRegistered(
					objectName));
		}
	}
}
