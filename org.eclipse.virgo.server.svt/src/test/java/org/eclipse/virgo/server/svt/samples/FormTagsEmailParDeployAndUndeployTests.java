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

package org.eclipse.virgo.server.svt.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.Test;

public class FormTagsEmailParDeployAndUndeployTests extends AbstractWebTests {
	private static final String APP_URI = "./apps/formtags-email-par.par";

	private static String artifactType = null;
	private static String artifactName = null;
	private static String artifactVersion = null;
	private static String[] signature = null;
	private static Object[] params = null;

	@Test
	public void testFormTagsEmailParDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI).toURI().toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName = compositeData.get("symbolicName").toString();
		artifactType = compositeData.get("type").toString();
		artifactVersion = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		assertFormtagsEmailParArtifactExists(artifactType, artifactName,
				artifactVersion);

		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(
						getObjectName(artifactType, artifactName,
								artifactVersion), "Dependents");
		for (ObjectName objectName : objectNameList) {
			assertFormtagsEmailParArtifactDependents(objectName);
		}
		UrlWaitLatch
				.waitFor("http://localhost:8080/formTagsParWithWebModule/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formTagsParWithWebModule/bundleContext.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formTagsParWithWebModule/servletContext.htm");
		assertFormtagsEmailParArtifactState(artifactType, artifactName,
				artifactVersion, "ACTIVE");
	}

	@Test
	public void testFormtagsEmailParUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { artifactName, artifactVersion };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formTagsParWithWebModule");
		assertFormtagsEmailParArtifactNotExists(artifactType, artifactName,
				artifactVersion);
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

	private void assertFormtagsEmailParArtifactExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertTrue(String.format(
				"Formtags Email Par Artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertFormtagsEmailParArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Formtags Email Par Artifact %s:%s:%s is not in state %s",
				type, name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertFormtagsEmailParArtifactNotExists(String type,
			String name, String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format(
				"Formtags Email Par Artifact %s:%s:%s is still exists", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertFormtagsEmailParArtifactDependents(ObjectName objectName)
			throws MalformedObjectNameException, IOException, Exception {
		assertTrue("Formtags email par dependent artifact does not exist",
				getMBeanServerConnection().isRegistered(objectName));
		assertEquals(String.format(
				"Formtags email par dependent artifact is not in state %s",
				"ACTIVE"), "ACTIVE", getMBeanServerConnection().getAttribute(
				objectName, "State"));
	}
}
