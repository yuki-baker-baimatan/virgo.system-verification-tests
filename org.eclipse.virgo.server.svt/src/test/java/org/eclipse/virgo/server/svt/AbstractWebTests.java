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

package org.eclipse.virgo.server.svt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.sourceforge.jwebunit.html.Cell;
import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.junit.WebTester;
import net.sourceforge.jwebunit.util.TestContext;

import org.eclipse.virgo.util.io.FileCopyUtils;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public abstract class AbstractWebTests {
	protected static final String URL = "http://localhost:8080/admin/web/artifact/overview.htm";
	protected static final String USER = "admin";
	protected static final String PASSWORD = "springsource";

	static final String TXT_FILE_SUFFIX = ".txt";
	static final String TXT_FILE_PREFIX = "summary";

	private static String pickupDir = null;
	private static String repositoryUsrDir = null;
	private static String repositoryExtDir = null;
	private static String repoPropertiesDir = null;
	private static String hostedRepoPropertiesDir = null;
	private static String kernelPropertiesDir = null;

	private static String workDir = null;
	private static String serviceabilityDir = null;
	private static String versionFile = null;

	private static MBeanServerConnection connection = null;
	private static final String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";
	private static final String KEYSTORE = "/config/keystore";
	private static final String KEYPASSWORD = "changeit";
	public static final long HALF_SECOND = 500;
	public static final long TWO_MINUTES = 120 * 1000;

	private final WebTester tester = new WebTester();

	protected final TestContext getTestContext() {
		return this.tester.getTestContext();
	}

	protected final WebTester getTester() {
		return this.tester;
	}

	@SuppressWarnings("unchecked")
	protected void assertTableContent(String tableId, Object[][] expectedRows) {
		ArrayList<Row> actualRows = getTester().getTable(tableId).getRows();

		boolean tableMatched = true;
		for (Object[] expectedRow : expectedRows) {
			Object bsn = expectedRow[0];
			boolean rowMatched = false;
			for (Row actualRow : actualRows) {
				Cell[] cells = ((ArrayList<Cell>) actualRow.getCells())
						.toArray(new Cell[actualRow.getCellCount()]);
				if (bsn.equals(cells[0].getValue())) {
					rowMatched = rowEqual(expectedRow, cells);

					break;
				}
			}
			tableMatched &= rowMatched;

		}
		assertTrue("Table did not match", tableMatched);
	}

	protected static void copyApplicationsToPickup(String pickupDir,
			String appsDir, String[] applicationNames) throws IOException,
			InterruptedException {
		for (String applicationName : applicationNames) {

			FileCopyUtils.copy(new File(appsDir, applicationName), new File(
					pickupDir, applicationName));
			Thread.sleep(1500);
		}
	}

	protected static void copyDependentBundlesToUsrRepository(String usrDir,
			String bundlesDir, String[] bundleNames) throws IOException {
		for (String bundleName : bundleNames) {
			FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
					usrDir, bundleName));
		}
	}

	protected static void deleteApplicationsFromPickup(String pickupDir,
			String[] applicationNames) throws IOException {
		for (String applicationName : applicationNames) {
			new File(pickupDir, applicationName).delete();
		}
	}

	protected static String getPickupDir() throws IOException {
		if (pickupDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					pickupDir = new File(candidate, "pickup")
							.getCanonicalPath();
					break;
				}
			}
		}
		return pickupDir;
	}

	protected static String getWorkDir() throws IOException {
		if (workDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					workDir = new File(candidate, "work").getCanonicalPath();
					break;
				}
			}
		}
		return workDir;
	}

	protected static String getServiceabilityDir() throws IOException {
		if (serviceabilityDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					serviceabilityDir = new File(candidate, "serviceability")
							.getCanonicalPath();
					break;
				}
			}
		}
		return serviceabilityDir;
	}

	protected static String getRepositoryUsrDir() throws IOException {
		if (repositoryUsrDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File mainDir : testExpanded.listFiles()) {
				if (mainDir.isDirectory()) {
					File repositoryDir = new File(mainDir, "repository")
							.getCanonicalFile();
					repositoryUsrDir = new File(repositoryDir, "usr")
							.getCanonicalPath();
				}
			}
		}
		return repositoryUsrDir;
	}

	protected static String getRepositoryExtDir() throws IOException {
		if (repositoryExtDir == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File mainDir : testExpanded.listFiles()) {
				if (mainDir.isDirectory()) {
					File repositoryDir = new File(mainDir, "repository")
							.getCanonicalFile();
					repositoryExtDir = new File(repositoryDir, "ext")
							.getCanonicalPath();
				}
			}
		}
		return repositoryExtDir;
	}

	protected static String getHostedRepoDir() throws IOException {

		/*
		 * File hostedDir = null;
		 * 
		 * File testExpanded = new File("./target/test-expanded/"); for (File
		 * mainDir : testExpanded.listFiles()) { if (mainDir.isDirectory()) {
		 * File repositoryDir = new File(mainDir, "repository")
		 * .getCanonicalFile();
		 * 
		 * hostedDir = new File(repositoryDir, "hosted"); hostedDir.mkdir();
		 * 
		 * } } return hostedDir.getCanonicalPath();
		 */

		File hostedDir = new File(System.getProperty("user.home"), "hosted");
		if (!hostedDir.exists()) {
			hostedDir.mkdir();
		}
		return hostedDir.getCanonicalPath();
	}

	protected static String getRepositoryPropertiesDir() throws IOException {
		if (repoPropertiesDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					File configDir = new File(candidate, "config");
					if (configDir.isDirectory()) {
						repoPropertiesDir = new File(configDir,
								"org.eclipse.virgo.repository.properties")
								.getCanonicalPath();

					}

				}
			}
		}

		return repoPropertiesDir;
	}

	protected static String getConfigDir() throws IOException {
		File configDir = null;
		if (configDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					configDir = new File(candidate, "config");

				}
			}
		}

		return configDir.getCanonicalPath();
	}

	protected static String getHostedRepositoryPropertiesDir()
			throws IOException {
		if (hostedRepoPropertiesDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					File configDir = new File(candidate, "config");
					if (configDir.isDirectory()) {
						hostedRepoPropertiesDir = new File(configDir,
								"org.eclipse.virgo.apps.repository.properties")
								.getCanonicalPath();

					}

				}
			}
		}

		return hostedRepoPropertiesDir;
	}

	protected static String getKernelPropertiesDir() throws IOException {

		if (kernelPropertiesDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					File configDir = new File(candidate, "config");
					if (configDir.isDirectory()) {
						kernelPropertiesDir = new File(configDir,
								"org.eclipse.virgo.kernel.properties")
								.getCanonicalPath();

					}

				}
			}
		}

		return kernelPropertiesDir;
	}

	protected static String getWatchedRepoDir() throws IOException {
		File watchedRepoDir = null;

		File testExpanded = new File(
				"./../org.eclipse.virgo.server.svt/target/test-expanded/");
		for (File candidate : testExpanded.listFiles()) {
			if (candidate.isDirectory()) {
				watchedRepoDir = new File(candidate, "watched-repo");
				if (!watchedRepoDir.exists()) {
					watchedRepoDir.mkdir();
				}
			}
		}
		return watchedRepoDir.getCanonicalPath();

	}

	protected File[] getDumpIds() throws IOException {

		File[] dumpFiles = null;

		File testExpanded = new File(
				"./../org.eclipse.virgo.server.svt/target/test-expanded/");
		for (File candidate : testExpanded.listFiles()) {
			if (candidate.isDirectory()) {
				File serviceabilityDir = new File(candidate, "serviceability");

				for (File serviceabilityFile : serviceabilityDir.listFiles()) {

					if (serviceabilityFile.isDirectory()
							&& serviceabilityFile.getName().equals("dump")) {

						dumpFiles = serviceabilityFile.listFiles();

					}
				}
			}
		}
		return dumpFiles;
	}

	protected File[] getDumpEntries(File dumpFile) throws IOException {

		File[] dumpSummaries = dumpFile.listFiles();

		return dumpSummaries;
	}

	protected static String getDumpDir() throws IOException {
		File dumpDir = null;
		File testExpanded = new File(
				"./../org.eclipse.virgo.server.svt/target/test-expanded/");
		for (File candidate : testExpanded.listFiles()) {
			if (candidate.isDirectory()) {
				File serviceabilityDir = new File(candidate, "serviceability");

				if (serviceabilityDir.isDirectory()) {

					dumpDir = new File(serviceabilityDir, "dump");

				}
			}
		}
		return dumpDir.getCanonicalPath();
	}

	public String applyNameFormatting(File sourceFolder) {
		String modDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
				sourceFolder.lastModified());
		String modTime = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(
				sourceFolder.lastModified());
		return String.format("%s at %s", modDate, modTime);
	}

	final FilenameFilter summaryFilenameFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			return name.endsWith(TXT_FILE_SUFFIX)
					&& name.startsWith(TXT_FILE_PREFIX);
		}
	};

	protected static String getServerVersion() throws IOException {

		String version;
		if (versionFile == null) {
			File testExpanded = new File("./target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					versionFile = new File(candidate, "lib/.version")
							.getCanonicalPath();
					break;
				}
			}
		}

		Properties versions = new Properties();
		InputStream stream = null;
		try {
			stream = new FileInputStream(versionFile);
			versions.load(stream);
			version = versions.getProperty("virgo.server.version");
			stream.close();
		} catch (IOException e) {
			version = "Unknown";
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e1) {

			}
		}
		return version;
	}

	private boolean rowEqual(Object[] expected, Cell[] actual) {
		if (expected.length != actual.length) {

			return false;
		}

		for (int i = 0; i < expected.length; i++) {
			if (!expected[i].equals(actual[i].getValue())) {

				return false;
			}
		}
		return true;
	}

	public String getOperatingSystem() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		return String.format("%s(%s) - %s", os.getName(), os.getArch(), os
				.getVersion());
	}

	public String getJavaVMDescription() {
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		return String.format("%s(%s) - %s", rt.getVmVendor(), rt.getVmName(),
				rt.getVmVersion());
	}

	public String getJavaVersion() {
		String vendor = System.getProperty("java.vendor");
		String version = System.getProperty("java.version");
		return String.format("%s - %s", vendor, version);
	}

	protected static MBeanServerConnection getMBeanServerConnection()
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

	protected void waitForMBeanRegister(String type, String name,
			String version, long interval, long duration) throws Exception {
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

	protected void waitForMBeanDeRegister(String type, String name,
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

	protected static ObjectName getObjectName(String type, String name,
			String version) throws MalformedObjectNameException {
		return new ObjectName(
				String
						.format(
								"org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s",
								type, name, version));
	}

	protected void waitForArtifactState(String type, String name,
			String version, String state, long interval, long duration)
			throws Exception {
		long startTime = System.currentTimeMillis();
		String artifactstate = null;
		while (System.currentTimeMillis() - startTime < duration) {
			artifactstate = getMBeanServerConnection().getAttribute(
					getObjectName(type, name, version), "State").toString();
			if (artifactstate.equals(state)) {
				return;
			}
			Thread.sleep(interval);
		}
		fail(String
				.format("After %d ms, artifact %s state was", duration, name)
				+ artifactstate);
	}
	
	protected static HtmlElement getHtmlChildElement(HtmlElement htmlElement, int index) {
		Iterable<HtmlElement> htmlChildElements = htmlElement.getAllHtmlChildElements();
		Iterator<HtmlElement> iterator = htmlChildElements.iterator();

		HtmlElement child = null;
		for (int childIndex = 0; childIndex <= index; childIndex++) {
			assertTrue(iterator.hasNext());
			child = iterator.next();
		}

		return child;
	}
}
