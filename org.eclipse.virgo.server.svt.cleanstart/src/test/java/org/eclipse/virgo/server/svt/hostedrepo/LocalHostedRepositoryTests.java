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

package org.eclipse.virgo.server.svt.hostedrepo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class LocalHostedRepositoryTests extends AbstractWebTests {

	private static final String APP_URI_PLAN = "./../test-apps/scopedplan.plan";
	private static MBeanServerConnection connection = null;
	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";
	private static final String KEYSTORE = "/config/keystore";
	private static final String KEYPASSWORD = "changeit";
	public static final long HALF_SECOND = 500;
	public static final long TWO_MINUTES = 120 * 1000;

	private static String artifactType = null;
	private static String artifactName = null;
	private static String artifactVersion = null;
	private static String[] signature = null;
	private static Object[] params = null;
	
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
	public void testPlanPickupDeployThatDependsUponArtifactsInLocalHostedRepo()
			throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_PLAN).toURI().toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName = compositeData.get("symbolicName").toString();
		artifactType = compositeData.get("type").toString();
		artifactVersion = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType, artifactName, artifactVersion,
				HALF_SECOND, TWO_MINUTES);
		assertPlanArtifactExists(artifactType, artifactName,
				artifactVersion);
		assertPlanArtifactState(artifactType, artifactName,
				artifactVersion, "ACTIVE");
	}

	@Test
	public void testPlanPickupUnDeployThatDependsUponArtifactsInLocalHostedRepo()
			throws Exception {
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
		assertPlanArtifactNotExists(artifactType, artifactName, artifactVersion);
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
				"Eclipselink plan artifact %s:%s:%s does not exist", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private void assertPlanArtifactState(String type, String name,
			String version, String state) throws MalformedObjectNameException,
			IOException, Exception {
		assertEquals(String.format(
				"Eclipselink plan artifact %s:%s:%s is not in state %s", type,
				name, version, state), state, getMBeanServerConnection()
				.getAttribute(getObjectName(type, name, version), "State"));
	}

	private void assertPlanArtifactNotExists(String type,
			String name, String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format(
				"Eclipselink plan artifact %s:%s:%s is still exists", type,
				name, version), getMBeanServerConnection().isRegistered(
				getObjectName(type, name, version)));
	}

	private static MBeanServerConnection getMBeanServerConnection()
			throws Exception {
		String severDir = null;
		String[] creds = { "admin", "springsource" };
		Map<String, String[]> env = new HashMap<String, String[]>();

		File testExpanded = new File(
				"./../org.eclipse.virgo.server.svt/target/test-expanded/");
		for (File mainDir : testExpanded.listFiles()) {
			if (mainDir.isDirectory()) {
				severDir = new File(mainDir.toURI()).getCanonicalPath();
			}
		}
		env.put(JMXConnector.CREDENTIALS, creds);
		System.setProperty("javax.net.ssl.trustStore", severDir + KEYSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", KEYPASSWORD);
		JMXServiceURL url = new JMXServiceURL(JMXURL);
		connection = JMXConnectorFactory.connect(url, env)
				.getMBeanServerConnection();
		return connection;
	}

	private void waitForMBeanRegister(String type, String name, String version,
			long interval, long duration) throws Exception {
		long startTime = System.currentTimeMillis();
		boolean mbeanStatus = false;
		while (System.currentTimeMillis() - startTime < duration) {
			mbeanStatus = getMBeanServerConnection().isRegistered(
					getObjectName(type, name, version));
			if (mbeanStatus) {
				return;
			}
			Thread.sleep(interval);
		}
		fail(String.format("After %d ms, artifact %s mbean Status was",
				duration, name)
				+ mbeanStatus);
	}

	private void waitForMBeanDeRegister(String type, String name,
			String version, long interval, long duration) throws Exception {
		long startTime = System.currentTimeMillis();
		boolean mbeanStatus = true;
		while (System.currentTimeMillis() - startTime < duration) {
			mbeanStatus = getMBeanServerConnection().isRegistered(
					getObjectName(type, name, version));
			if (!mbeanStatus) {
				return;
			}
			Thread.sleep(interval);
		}
		fail(String.format("After %d ms, artifact %s mbean Status was",
				duration, name)
				+ mbeanStatus);
	}

	private static ObjectName getObjectName(String type, String name,
			String version) throws MalformedObjectNameException {
		return new ObjectName(
				String
						.format(
								"org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s",
								type, name, version));
	}
}
