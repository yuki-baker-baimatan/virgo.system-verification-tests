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

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;

public class PlanArtifactPickupDeployAndUndeployTests extends AbstractWebTests {
	private static final String APPS_DIR = "./../test-apps";
	private static final String[] APPLICATION_NAMES = new String[] { "scopedplan.plan" };
	private static final String ARTIFACTSDIR = "./../test-apps";
	private static String[] artifactNames = new String[] { "bundleA.jar",
			"bundleB.war", "foo.properties" };

	@BeforeClass
	public static void testContextSetup() throws Exception {
		copyPlanReferencedArtifactsToRepository(getRepositoryUsrDir(),
				ARTIFACTSDIR, artifactNames);
	}

	private static void copyPlanReferencedArtifactsToRepository(String usrDir,
			String artifactsDir, String[] artifactsNames) throws IOException,
			InterruptedException {
		for (String artifactName : artifactsNames) {
			FileCopyUtils.copy(new File(artifactsDir, artifactName), new File(
					usrDir, artifactName));
		}
	}

	@Test
	public void planArtifactDeploy() throws Exception {
		copyApplicationsToPickup(getPickupDir(), APPS_DIR, APPLICATION_NAMES);
		waitForMBeanRegister("plan", "scoped.plan", "1.0.0", HALF_SECOND,
				TWO_MINUTES);
		assertPlanArtifactExists("plan", "scoped.plan", "1.0.0");
		waitForArtifactState("plan","scoped.plan","1.0.0","ACTIVE",HALF_SECOND, TWO_MINUTES);
		assertPlanArtifactState("plan", "scoped.plan", "1.0.0", "ACTIVE");
		assertPlanReferencedArtifacts("bundle", "bundleA", "1.0.0");
		assertPlanReferencedArtifacts("bundle", "bundleB", "1.0.0");
		assertPlanReferencedArtifacts("configuration", "foo", "0.0.0");
		assertPlanReferencedArtifacts("bundle",
				"scoped.plan-synthetic.context", "1.0.0");
	}

	@Test
	public void planArtifactUndeploy() throws Exception {
		deleteApplicationsFromPickup(getPickupDir(), APPLICATION_NAMES);
		Thread.sleep(5000);
		waitForMBeanDeRegister("plan", "scoped.plan", "1.0.0", HALF_SECOND,
				TWO_MINUTES);
		assertPlanArtifactNotExists("plan", "scoped.plan", "1.0.0");
	}

	private void assertPlanArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format("Plan Artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertPlanArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format("Plan Artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format("Plan Artifact %s:%s:%s is still exists",
				type, name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertPlanReferencedArtifacts(String type, String name,
			String version) throws MalformedObjectNameException, IOException,
			Exception {
		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(getObjectName("plan", "scoped.plan", "1.0.0"),
						"Dependents");
		for (ObjectName objectName : objectNameList) {
			assertTrue(String.format(
					"Plan Referenced Artifact %s:%s:%s does not exist", type,
					name, version), getMBeanServerConnection().isRegistered(
					objectName));
		}
	}
}
