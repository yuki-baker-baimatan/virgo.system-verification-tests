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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.jwebunit.html.Cell;
import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.junit.WebTester;
import net.sourceforge.jwebunit.util.TestContext;

import org.eclipse.virgo.util.io.FileCopyUtils;

public abstract class AbstractWebTests {
	protected static final String URL = "http://localhost:8080/admin/web/artifact/overview.htm";
	protected static final String USER = "admin";
	protected static final String PASSWORD = "springsource";

	private static String pickupDir = null;
	private static String usrDir = null;
	private static String workDir = null;
	private static String serviceabilityDir = null;
	private static String repoPropertiesDir = null;
	private static String versionFile = null;

	private static String serverHomeDir = null;
	private static String binDir = null;
	
	private static Process process = null;
	private static ProcessBuilder pb = null;
	private static File startup = null;
	private static File startupURI = null;
	private static File shutdownURI = null;
	private static String startupFileName = null;
	private static File shutdown = null;
	private static String shutdownFileName = null;
	private static OperatingSystemMXBean os = ManagementFactory
			.getOperatingSystemMXBean();
	private static final String SPLASHURL = "http://localhost:8080/";

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
			String appsDir, String[] applicationNames) throws IOException {
		for (String applicationName : applicationNames) {
			FileCopyUtils.copy(new File(appsDir, applicationName), new File(
					pickupDir, applicationName));
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
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
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

	protected static String getUsrDir() throws IOException {
		if (usrDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File mainDir : testExpanded.listFiles()) {
				if (mainDir.isDirectory()) {
					File repositoryDir = new File(mainDir, "repository")
							.getCanonicalFile();
					usrDir = new File(repositoryDir, "usr").getCanonicalPath();
				}
			}
		}
		return usrDir;
	}

	public static String getServerBinDir() throws IOException {
		if (binDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					binDir = new File(candidate, "bin").getCanonicalPath();
					break;
				}
			}
		}
		return binDir;
	}

	protected static String getWorkDir() throws IOException {
		if (workDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
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
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
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

	protected static String getRepositoryPropertiesDir() throws IOException {
		if (repoPropertiesDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					File configDir = new File(candidate, "config");
					if (configDir.isDirectory()) {
						repoPropertiesDir = new File(configDir,
								"repository.properties").getCanonicalPath();

					}

				}
			}
		}

		return repoPropertiesDir;
	}

	protected static String getServerHomeDir() throws Exception {
		if (serverHomeDir == null) {
			File testExpanded = new File(
					"./../org.eclipse.virgo.server.svt/target/test-expanded/");
			for (File candidate : testExpanded.listFiles()) {
				if (candidate.isDirectory()) {
					serverHomeDir = new File(candidate.getCanonicalPath())
							.getCanonicalPath();
				}
			}
		}
		return serverHomeDir;
	}

	protected static String getHostedRepoDir() throws IOException {

		File hostedDir = null;

		File testExpanded = new File(
				"./../org.eclipse.virgo.server.svt/target/test-expanded/");
		for (File mainDir : testExpanded.listFiles()) {
			if (mainDir.isDirectory()) {
				File repositoryDir = new File(mainDir, "repository")
						.getCanonicalFile();

				hostedDir = new File(repositoryDir, "hosted");
				if (!hostedDir.exists()) {
					hostedDir.mkdir();
				}

			}
		}
		return hostedDir.getCanonicalPath();

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
			version = versions.getProperty("dm.server.version");
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

	public String getJavaInfo() {
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		return String.format("%s(%s) - %s", rt.getVmVendor(), rt.getVmName(),
				rt.getVmVersion());
	}
	
	public static void startServer() {
		new Thread(new StartUpThread()).start();
		sleep();
		UrlWaitLatch.waitFor(URL, USER, PASSWORD);
	}

	private static void sleep() {
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {
		}
	}
	
	public static void shutdownServer() {
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully(SPLASHURL);
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
