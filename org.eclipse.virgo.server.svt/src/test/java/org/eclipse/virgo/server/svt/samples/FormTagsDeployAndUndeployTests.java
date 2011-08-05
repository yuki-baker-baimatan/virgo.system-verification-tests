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


public class FormTagsDeployAndUndeployTests extends AbstractWebTests {

	private static final String APP_URI_PAR = "./apps/formtags-par-2.0.1.BUILD-20100413113234.par";
	private static final String APP_URI_WAR = "./apps/formtags-war-2.0.1.BUILD-20100413113234.war";
	private static final String APP_URI_BUNDLE = "./apps/formtags-shared-services-service-2.0.1.BUILD-20100413113234.jar";
	private static final String APP_URI_SHARED_SERVICES_WAR = "./apps/formtags-shared-services-war-2.0.1.BUILD-20100413113234.war";
	private static final String APP_URI_SHARED_LIBRARIES_WAR = "./apps/formtags-shared-libs-2.0.1.BUILD-20100413113234.war";

	private static String artifactType1 = null;
	private static String artifactName1 = null;
	private static String artifactVersion1 = null;
	
	private static String artifactType2 = null;
	private static String artifactName2 = null;
	private static String artifactVersion2 = null;
	
	private static String artifactType3 = null;
	private static String artifactName3 = null;
	private static String artifactVersion3 = null;
	
	private static String artifactType4 = null;
	private static String artifactName4 = null;
	private static String artifactVersion4 = null;
	
	private static String artifactType5 = null;
	private static String artifactName5 = null;
	private static String artifactVersion5 = null;
	
	private static String[] signature = null;
	private static Object[] params = null;

	@Test
	public void testFormTagsParDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_PAR).toURI().toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName1 = compositeData.get("symbolicName").toString();
		artifactType1 = compositeData.get("type").toString();
		artifactVersion1 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType1, artifactName1, artifactVersion1,
				HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(artifactType1, artifactName1, artifactVersion1);

		ObjectName[] objectNameList = (ObjectName[]) getMBeanServerConnection()
				.getAttribute(
						getObjectName(artifactType1, artifactName1,
								artifactVersion1), "Dependents");
		for (ObjectName objectName : objectNameList) {
			assertParArtifactDependents(objectName);
		}
		UrlWaitLatch.waitFor("http://localhost:8080/formtags-par");
		UrlWaitLatch.waitFor("http://localhost:8080/formtags-par/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-par/form.htm?id=1");
		assertArtifactState(artifactType1, artifactName1, artifactVersion1,
				"ACTIVE");
	}
	
	@Test
	public void testFormTagsParUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { artifactName1, artifactVersion1 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(artifactType1, artifactName1, artifactVersion1,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch.waitForNotExistence("http://localhost:8080/formtags-par");
		assertArtifactNotExists(artifactType1, artifactName1, artifactVersion1);
	}

	@Test
	public void testFormTagsWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_WAR).toURI().toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName2 = compositeData.get("symbolicName").toString();
		artifactType2 = compositeData.get("type").toString();
		artifactVersion2 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType2, artifactName2, artifactVersion2,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType2, artifactName2, artifactVersion2);
		assertArtifactState(artifactType2, artifactName2, artifactVersion2,
				"ACTIVE");
	}
	
	@Test
	public void testFormTagsWarUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { artifactName2, artifactVersion2 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(artifactType2, artifactName2, artifactVersion2,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists(artifactType2, artifactName2, artifactVersion2);
	}

	@Test
	public void testFormTagsBundleDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_BUNDLE).toURI().toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName3 = compositeData.get("symbolicName").toString();
		artifactType3 = compositeData.get("type").toString();
		artifactVersion3 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType3, artifactName3, artifactVersion3,
				HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(artifactType3, artifactName3, artifactVersion3);
		assertArtifactState(artifactType3, artifactName3, artifactVersion3,
				"ACTIVE");
	}


	@Test
	public void testFormTagsSharedServicesWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_SHARED_SERVICES_WAR).toURI()
				.toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName4 = compositeData.get("symbolicName").toString();
		artifactType4 = compositeData.get("type").toString();
		artifactVersion4 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType4, artifactName4, artifactVersion4,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType4, artifactName4, artifactVersion4);
		assertArtifactState(artifactType4, artifactName4, artifactVersion4,
				"ACTIVE");
	}

	@Test
	public void testFormTagsSharedServicesWarUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { artifactName4, artifactVersion4 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(artifactType4, artifactName4, artifactVersion4,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists(artifactType4, artifactName4, artifactVersion4);
	}
	
	@Test
	public void testFormTagsSharedLibrariesWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_SHARED_LIBRARIES_WAR).toURI()
				.toString() };

		CompositeData compositeData = deploy(signature, params);
		artifactName5 = compositeData.get("symbolicName").toString();
		artifactType5 = compositeData.get("type").toString();
		artifactVersion5 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType5, artifactName5, artifactVersion5,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType5, artifactName5, artifactVersion5);
		assertArtifactState(artifactType5, artifactName5, artifactVersion5,
				"ACTIVE");
	}
	
	@Test
	public void testFormTagsSharedLibrariesWarUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { artifactName5, artifactVersion5 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(artifactType5, artifactName5, artifactVersion5,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists(artifactType5, artifactName5, artifactVersion5);
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
	
	private void assertArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format("Artifact %s:%s:%s is still exists", type,
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
