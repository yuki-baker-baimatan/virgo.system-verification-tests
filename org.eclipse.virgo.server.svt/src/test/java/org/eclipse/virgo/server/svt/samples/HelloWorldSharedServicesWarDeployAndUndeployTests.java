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


public class HelloWorldSharedServicesWarDeployAndUndeployTests extends
		AbstractWebTests {
	private static final String APP_URI_BUNDLE1 = "./apps/hello.domain.jar";
	private static final String APP_URI_BUNDLE2 = "./apps/hello.service.api.jar";
	private static final String APP_URI_BUNDLE3 = "./apps/hello.service.impl-en.jar";
	private static final String APP_URI_SHARED_SERVICES_WAR = "./apps/hello_war_shared_services.war";

	private static String type = null;
	private static String name = null;
	private static String version = null;
	private static String[] signature = null;
	private static Object[] params = null;
	private static CompositeData compositeData=null;

	@Test
	public void testSharedServicesWarDeployWithDependencies() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_BUNDLE1).toURI().toString() };
		compositeData = deploy(signature, params);
		name = compositeData.get("symbolicName").toString();
		type = compositeData.get("type").toString();
		version = compositeData.get("version").toString();

		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"deploy", params, signature);
		waitForMBeanRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(type, name, version);
		assertArtifactState(type, name, version, "ACTIVE");

		params = new Object[] { new File(APP_URI_BUNDLE2).toURI().toString() };
		compositeData = deploy(signature, params);
		name = compositeData.get("symbolicName").toString();
		type = compositeData.get("type").toString();
		version = compositeData.get("version").toString();
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"deploy", params, signature);
		waitForMBeanRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(type, name, version);
		assertArtifactState(type, name, version, "ACTIVE");

		params = new Object[] { new File(APP_URI_BUNDLE3).toURI().toString() };
		compositeData = deploy(signature, params);
		name = compositeData.get("symbolicName").toString();
		type = compositeData.get("type").toString();
		version = compositeData.get("version").toString();
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"deploy", params, signature);
		waitForMBeanRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(type, name, version);
		assertArtifactState(type, name, version, "ACTIVE");

		params = new Object[] { new File(APP_URI_SHARED_SERVICES_WAR).toURI()
				.toString() };
		compositeData = deploy(signature, params);
		name = compositeData.get("symbolicName").toString();
		type = compositeData.get("type").toString();
		version = compositeData.get("version").toString();
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"deploy", params, signature);
		waitForMBeanRegister(type, name, version, HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/hello_war_shared_services/");
		assertArtifactExists(type, name, version);
		assertArtifactState(type, name, version, "ACTIVE");
	}

	@Test
	public void testSharedServicesWarUndeployWithDependencies()
			throws Exception, InterruptedException {
		String[] signature = { String.class.getName(), String.class.getName() };
		Object[] params1 = { "hello_war_shared_services", "1.0.0" };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params1, signature);
		waitForMBeanDeRegister("bundle", "hello_war_shared_services", "1.0.0",
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/hello_war_shared_services/");
		assertArtifactNotExists("bundle", "hello_war_shared_services", "1.0.0");

		Object[] params2 = { "hello.service.impl_en", "1.0.0" };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params2, signature);
		waitForMBeanDeRegister("bundle", "hello.service.impl_en", "1.0.0",
				HALF_SECOND, TWO_MINUTES);
		assertArtifactNotExists("bundle", "hello.service.impl_en", "1.0.0");

		Object[] params3 = { "hello.service.api", "1.0.0" };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params3, signature);
		waitForMBeanDeRegister("bundle", "hello.service.api", "1.0.0",
				HALF_SECOND, TWO_MINUTES);
		assertArtifactNotExists("bundle", "hello.service.api", "1.0.0");

		Object[] params4 = { "hello.domain", "1.0.0" };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params4, signature);
		waitForMBeanDeRegister("bundle", "hello.domain", "1.0.0", HALF_SECOND,
				TWO_MINUTES);
		assertArtifactNotExists("bundle", "hello.domain", "1.0.0");
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

	private void assertArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format("Artifact %s:%s:%s is still exists", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

}
