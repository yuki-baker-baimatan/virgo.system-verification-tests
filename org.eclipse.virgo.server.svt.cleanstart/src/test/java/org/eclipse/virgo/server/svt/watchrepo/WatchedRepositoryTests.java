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

package org.eclipse.virgo.server.svt.watchrepo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WatchedRepositoryTests extends AbstractWebTests {

	private static String[] bundleNames = new String[] {
			"formtags-shared-services-service-2.0.1.BUILD-20100413113234.jar",
			"com.springsource.freemarker-2.3.15.jar",
			"com.springsource.javax.persistence-1.0.0.jar",
			"com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar",
			"com.springsource.org.apache.commons.pool-1.3.0.jar",
			"com.springsource.org.eclipse.persistence-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.antlr-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.asm-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.jpa-1.0.0.jar",
			"com.springsource.org.h2-1.0.71.jar"};
	private static final String bundlesDir = "src/test/resources/bundles";

	private static final String APP_URI_SHAREDSERVICES_WAR = "./../org.eclipse.virgo.server.svt/apps/formtags-shared-services-war-2.0.1.BUILD-20100413113234.war";
	private static final String APP_URI_PAR = "./../org.eclipse.virgo.server.svt/apps/greenpages-solution-2.0.1.SNAPSHOT.par";
	
	private static Process process = null;
	private static ProcessBuilder pb = null;
	private static File startup = null;
	private static String startupFileName = null;
	private static File shutdown = null;
	private static String shutdownFileName = null;
	private static File startupURI = null;
	private static File shutdownURI = null;
	private static OperatingSystemMXBean os = ManagementFactory
			.getOperatingSystemMXBean();
	private static MBeanServerConnection connection = null;
	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";
	private static final String KEYSTORE = "/config/keystore";
	private static final String KEYPASSWORD = "changeit";
	public static final long HALF_SECOND = 500;
	public static final long TWO_MINUTES = 120 * 1000;

	private static String artifactType1 = null;
	private static String artifactName1 = null;
	private static String artifactVersion1 = null;
	
	private static String artifactType2 = null;
	private static String artifactName2 = null;
	private static String artifactVersion2 = null;

	private static String[] signature = null;
	private static Object[] params = null;

	@BeforeClass
	public static void watchRepoSetUp() throws Exception {
		addBundlesToWatchedRepository(getWatchedRepoDir());
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully("http://localhost:8080/");
		new Thread(new StartUpThread()).start();
		UrlWaitLatch.waitFor("http://localhost:8080/");
	}

	public static void addBundlesToWatchedRepository(String repoDir)
			throws Exception {
		for (String bundleName : bundleNames) {
			FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
					repoDir, bundleName));
		}

	}

	@Test
	public void testSharedServicesWarDeployThatDependsUponArtifactsInWatchRepo()
			throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_SHAREDSERVICES_WAR).toURI()
				.toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName1 = compositeData.get("symbolicName").toString();
		artifactType1 = compositeData.get("type").toString();
		artifactVersion1 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType1, artifactName1, artifactVersion1,
				HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234/list.htm");
		UrlWaitLatch
				.waitFor("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234/form.htm?id=1");
		assertArtifactExists(artifactType1, artifactName1, artifactVersion1);
		assertArtifactState(artifactType1, artifactName1, artifactVersion1,
				"ACTIVE");
	}

	@Test
	public void testSharedServicesWarUndeployThatDependsUponArtifactsInWatchRepo()
			throws Exception {
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
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/formtags-shared-services-war-2.0.1.BUILD-20100413113234");
		assertArtifactNotExists(artifactType1, artifactName1, artifactVersion1);
	}

	@Test
	public void testParDeployThatDependsUponArtifactsInWatchRepo()
			throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_URI_PAR).toURI().toString() };
		CompositeData compositeData = deploy(signature, params);
		artifactName2 = compositeData.get("symbolicName").toString();
		artifactType2 = compositeData.get("type").toString();
		artifactVersion2 = compositeData.get("version").toString();

		waitForMBeanRegister(artifactType2, artifactName2, artifactVersion2,
				HALF_SECOND, TWO_MINUTES);
		assertArtifactExists(artifactType2, artifactName2, artifactVersion2);
		UrlWaitLatch.waitFor("http://localhost:8080/greenpages/app/home.htm");
		assertArtifactState(artifactType2, artifactName2, artifactVersion2,
				"ACTIVE");
	}

	@Test
	public void testParUndeployThatDependsUponArtifactsInWatchRepo()
			throws Exception {
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
		UrlWaitLatch.waitForNotExistence("http://localhost:8080/greenpages");
		assertArtifactNotExists(artifactType2, artifactName2, artifactVersion2);
	}

	
	@AfterClass
	public static void shutdownServerInstance() {
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully("http://localhost:8080/");
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

	private static class StartUpThread implements Runnable {

		public StartUpThread() {

		}

		public void run() {
			String[] args = null;
			try {
				if (os.getName().contains("Windows")) {
					startup = new File(getServerBinDir(), "startup.bat");
					startupURI = new File(startup.toURI());
					startupFileName = startupURI.getCanonicalPath();
				} else {
					startup = new File(getServerBinDir(), "startup.sh");
					startupURI = new File(startup.toURI());
					startupFileName = startupURI.getCanonicalPath();
				}
				args = new String[] { startupFileName, "-clean" };
				pb = new ProcessBuilder(args);
				Map<String, String> env = pb.environment();
				env.put("JAVA_HOME", System.getProperty("java.home"));
				pb.redirectErrorStream(true);

				process = pb.start();

				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class ShutdownThread implements Runnable {

		public ShutdownThread() {

		}

		public void run() {
			String[] args = null;
			try {

				if (os.getName().contains("Windows")) {
					shutdown = new File(getServerBinDir(), "shutdown.bat");
					shutdownURI = new File(shutdown.toURI());
					shutdownFileName = shutdownURI.getCanonicalPath();
				} else {
					shutdown = new File(getServerBinDir(), "shutdown.sh");
					shutdownURI = new File(shutdown.toURI());
					shutdownFileName = shutdownURI.getCanonicalPath();
				}
				args = new String[] { shutdownFileName, "-immediate" };
				pb = new ProcessBuilder(args);
				Map<String, String> env = pb.environment();
				env.put("JAVA_HOME", System.getProperty("java.home"));
				pb.redirectErrorStream(true);

				process = pb.start();

				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
