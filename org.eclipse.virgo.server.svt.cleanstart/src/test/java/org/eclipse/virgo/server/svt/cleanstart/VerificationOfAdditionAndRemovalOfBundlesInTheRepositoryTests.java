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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class VerificationOfAdditionAndRemovalOfBundlesInTheRepositoryTests
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
	public void testAddtionOfBundlesToTheExtRepository() throws Exception {
		String repoName = (String) getMBeanServerConnection().getAttribute(
				new ObjectName(
						"org.eclipse.virgo.kernel:type=Repository,name=ext"),
				"Name");
		assertEquals("ext", repoName);
		CompositeData[] object = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=ext"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object.length; i++) {
			if ((object[i].get("name")
					.equals("org.springframework.dmServer.testtool.incoho"))) {
				assertEquals("org.springframework.dmServer.testtool.incoho",
						object[i].get("name"));
				assertEquals("1.0.0.RELEASE", object[i].get("version"));
				assertEquals("bundle", object[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfBundlesToTheUsrRepository() throws Exception {
		String repoName = (String) getMBeanServerConnection().getAttribute(
				new ObjectName(
						"org.eclipse.virgo.kernel:type=Repository,name=usr"),
				"Name");
		assertEquals("usr", repoName);
		CompositeData[] object = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=usr"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object.length; i++) {
			if ((object[i].get("name")
					.equals("org.springframework.dmServer.testtool.incoho"))) {
				assertEquals("org.springframework.dmServer.testtool.incoho",
						object[i].get("name"));
				assertEquals("1.0.0.RELEASE", object[i].get("version"));
				assertEquals("bundle", object[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfLibrariesToTheExtRepository() throws Exception {
		String repoName = (String) getMBeanServerConnection().getAttribute(
				new ObjectName(
						"org.eclipse.virgo.kernel:type=Repository,name=ext"),
				"Name");
		assertEquals("ext", repoName);
		CompositeData[] object = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=ext"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object.length; i++) {
			if ((object[i].get("name").equals("org.springframework.spring") && object[i]
					.get("version").equals("2.5.4.A"))) {
				assertEquals("org.springframework.spring", object[i]
						.get("name"));
				assertEquals("2.5.4.A", object[i].get("version").toString());
				assertEquals("library", object[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfLibrariesToTheUsrRepository() throws Exception {
		String repoName = (String) getMBeanServerConnection().getAttribute(
				new ObjectName(
						"org.eclipse.virgo.kernel:type=Repository,name=usr"),
				"Name");
		assertEquals("usr", repoName);
		CompositeData[] object = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=usr"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object.length; i++) {
			if ((object[i].get("name").equals("org.springframework.spring") && object[i]
					.get("version").equals("2.5.4.A"))) {
				assertEquals("org.springframework.spring", object[i]
						.get("name"));
				assertEquals("2.5.4.A", object[i].get("version").toString());
				assertEquals("library", object[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfBundlesAndLibrariesToTheHostedRepository()
			throws Exception {
		String repoName = (String) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=hosted-repository"),
						"Name");
		assertEquals("hosted-repository", repoName);
		CompositeData[] object1 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=hosted-repository"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object1.length; i++) {
			if (object1[i].get("name")
					.equals("org.springframework.petclinic.domain") && object1[i].get("version").equals("2.0.1.BUILD-20091126110002")) {
				assertEquals("org.springframework.petclinic.domain", object1[i]
						.get("name"));
				assertEquals("2.0.1.BUILD-20091126110002", object1[i].get("version")
						.toString());
				assertEquals("bundle", object1[i].get("type"));
			}
		}

		CompositeData[] object2 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=hosted-repository"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object2.length; i++) {
			if ((object2[i].get("name").equals("org.springframework.spring"))) {
				assertEquals("org.springframework.spring", object2[i]
						.get("name"));
				assertEquals("2.5.4.A", object2[i].get("version").toString());
				assertEquals("library", object2[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfBundlesAndLibrariesToTheRemoteRepository()
			throws Exception {
		String repoName = (String) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=remote-repo"),
						"Name");
		assertEquals("remote-repo", repoName);
		CompositeData[] object1 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=remote-repo"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object1.length; i++) {
			if (object1[i].get("name")
					.equals("org.springframework.petclinic.domain") && object1[i].get("version").equals("2.0.1.BUILD-20091126110002")){
				assertEquals("org.springframework.petclinic.domain", object1[i]
						.get("name"));
				assertEquals("2.0.1.BUILD-20091126110002", object1[i].get("version")
						.toString());
				assertEquals("bundle", object1[i].get("type"));
			}
		}

		CompositeData[] object2 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=remote-repo"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object2.length; i++) {
			if ((object2[i].get("name").equals("org.springframework.spring"))) {
				assertEquals("org.springframework.spring", object2[i]
						.get("name"));
				assertEquals("2.5.4.A", object2[i].get("version").toString());
				assertEquals("library", object2[i].get("type"));
			}
		}
	}

	@Test
	public void testAddtionOfBundlesAndLibrariesToTheWatchedRepository()
			throws Exception {
		String repoName = (String) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=watched-repo"),
						"Name");
		assertEquals("watched-repo", repoName);
		CompositeData[] object1 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=watched-repo"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object1.length; i++) {
			if ((object1[i].get("name")
					.equals("org.springframework.petclinic.domain"))) {
				assertEquals("org.springframework.petclinic.domain", object1[i]
						.get("name"));
				assertEquals("2.0.1.BUILD-20091126110002", object1[i].get("version")
						.toString());
				assertEquals("bundle", object1[i].get("type"));
			}
		}

		CompositeData[] object2 = (CompositeData[]) getMBeanServerConnection()
				.getAttribute(
						new ObjectName(
								"org.eclipse.virgo.kernel:type=Repository,name=watched-repo"),
						"AllArtifactDescriptorSummaries");

		for (int i = 0; i < object2.length; i++) {
			if ((object2[i].get("name").equals("org.springframework.spring"))) {
				assertEquals("org.springframework.spring", object2[i]
						.get("name"));
				assertEquals("2.5.4.A", object2[i].get("version").toString());
				assertEquals("library", object2[i].get("type"));
			}
		}
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

}
