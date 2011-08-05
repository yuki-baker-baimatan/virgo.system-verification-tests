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

package org.eclipse.virgo.server.svt.startclean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.Test;



public class FormTagsDeploymentForCleanStartVerificationTests extends
		AbstractWebTests {

	private static final String APP_URI_PAR = "./apps/formtags-par-2.0.1.BUILD-20100413113234.par";
	private static final String APP_URI_WAR = "./apps/formtags-war-2.0.1.BUILD-20100413113234.war";
	private static final String APP_URI_SHARED_LIBRARIES_WAR = "./apps/formtags-shared-libs-2.0.1.BUILD-20100413113234.war";

	private static String[] signature = null;
	private static Object[] params = null;
	
	String artifactType = null;
	String artifactName = null;
	String artifactVersion = null;

	@Test
	public void testFormTagsParDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_PAR).toURI().toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName = compositeData.get("symbolicName").toString();
		artifactType = compositeData.get("type").toString();
		artifactVersion = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(artifactType, artifactName, artifactVersion);

		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(
						getObjectName(artifactType, artifactName,
								artifactVersion), "Dependents");
		for (ObjectName objectName : objectNameList) {
			assertParArtifactDependents(objectName);
		}
		UrlWaitLatch.waitFor("http://localhost:8080/formtags-par");
		UrlWaitLatch.waitFor("http://localhost:8080/formtags-par/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-par/form.htm?id=1");
		assertArtifactState(artifactType, artifactName, artifactVersion,
				"ACTIVE");
	}

	@Test
	public void testFormTagsWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_WAR).toURI().toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName = compositeData.get("symbolicName").toString();
		artifactType = compositeData.get("type").toString();
		artifactVersion = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType, artifactName, artifactVersion);
		assertArtifactState(artifactType, artifactName, artifactVersion,
				"ACTIVE");
	}

	@Test
	public void testFormTagsSharedLibrariesWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_SHARED_LIBRARIES_WAR).toURI()
				.toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName = compositeData.get("symbolicName").toString();
		artifactType = compositeData.get("type").toString();
		artifactVersion = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType, artifactName, artifactVersion);
		assertArtifactState(artifactType, artifactName, artifactVersion,
				"ACTIVE");
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

	private void assertArtifactExists(String type, String name, String version)
			throws IOException, Exception, MalformedObjectNameException {
		assertTrue(String.format("Artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertArtifactState(String type, String name, String version,
			String state) throws MalformedObjectNameException, IOException,
			Exception {
		assertEquals(String.format("Artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertParArtifactDependents(ObjectName objectName)
			throws MalformedObjectNameException, IOException, Exception {

		assertTrue("par dependent artifact does not exist",
				getMBeanServerConnection().isRegistered(objectName));
		assertEquals(String.format("par dependent artifact is not in state %s",
				"ACTIVE"), "ACTIVE", getMBeanServerConnection().getAttribute(
				objectName, "State"));
	}
}
