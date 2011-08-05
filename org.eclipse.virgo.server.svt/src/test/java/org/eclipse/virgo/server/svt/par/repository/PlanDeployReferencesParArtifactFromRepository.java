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

package org.eclipse.virgo.server.svt.par.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.Test;

public class PlanDeployReferencesParArtifactFromRepository extends
		AbstractWebTests {

	private static final String APP_URI = "./../test-apps/parreferencedplan.plan";
	private static String type = null;
	private static String name = null;
	private static String version = null;
	private static String[] signature = null;
	private static Object[] params = null;

	@Test
	public void planDeployReferencesParArtifactFromRepository() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI).toURI().toString() };

		CompositeData compositeData = deploy(signature, params);
		name = compositeData.get("symbolicName").toString();
		type = compositeData.get("type").toString();
		version = compositeData.get("version").toString();

		waitForMBeanRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		assertPlanArtifactExists(type, name, version);
		assertPlanArtifactState(type, name, version, "ACTIVE");
	}
	
	@Test
	public void planUnDeployReferencesParArtifactFromRepository() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { name, version };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		assertPlanArtifactNotExists(type, name, version);
	}

	private static CompositeData deploy(String[] signature, Object[] params)
			throws Exception {
		CompositeData compsiteData = (CompositeData) getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"deploy", params, signature);
		return compsiteData;
	}

	private void assertPlanArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"Plan Artifact referencing par artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertPlanArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Plan Artifact referencing par artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format(
				"Plan Artifact referencing par artifact %s:%s:%s is still exists", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}
}
