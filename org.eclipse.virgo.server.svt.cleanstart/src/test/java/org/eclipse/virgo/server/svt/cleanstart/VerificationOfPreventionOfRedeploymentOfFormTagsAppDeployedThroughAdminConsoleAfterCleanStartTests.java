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

package org.eclipse.virgo.server.svt.cleanstart;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class VerificationOfPreventionOfRedeploymentOfFormTagsAppDeployedThroughAdminConsoleAfterCleanStartTests
		extends AbstractWebTests {
	
	private static MBeanServerConnection connection = null;
	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";
	private static final String KEYSTORE = "/config/keystore";
	private static final String KEYPASSWORD = "changeit";
	public static final long HALF_SECOND = 500;
	public static final long TWO_MINUTES = 120 * 1000;
	
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
	public void testPreventionOfArtifactsRedeploymentByCleanStart()
			throws Exception {
		waitForMBeanDeRegister("par", "formtags-par", "2.0.1.BUILD-20100413113234",
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch.waitForNotExistence("http://localhost:8080/formtags-par");
		assertArtifactNotExists("par", "formtags-par", "2.0.1.BUILD-20100413113234");

		waitForMBeanDeRegister("bundle", "/formtags-shared-services-war-2.0.1.BUILD-20100413113234.war",
				"0.0.0", HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-war-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists("bundle", "formtags-war-2.0.1.BUILD-20100413113234.war",
				"0.0.0");

		waitForMBeanDeRegister("bundle",
				"org.springframework.showcase.formtags_shared_libs",
				"2.0.1.BUILD-20100413113234", HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-shared-libs-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists("bundle",
				"org.springframework.showcase.formtags_shared_libs",
				"2.0.1.BUILD-20100413113234");
	}

	private void assertArtifactNotExists(String type, String name,
			String version) throws IOException, Exception,
			MalformedObjectNameException {
		assertFalse(String.format("Artifact %s:%s:%s is still exists", type,
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
