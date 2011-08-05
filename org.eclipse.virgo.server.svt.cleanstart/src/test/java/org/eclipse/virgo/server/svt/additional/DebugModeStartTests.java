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

package org.eclipse.virgo.server.svt.additional;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.AfterClass;
import org.junit.Test;


public class DebugModeStartTests extends AbstractWebTests {

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
	private static String jvmMsg;
	private static final String SPLASHURL = "http://localhost:8080/";

	@Test
	public void testdmServerStartWithDefaultRemoteDebugPort()
			throws InterruptedException {
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully(SPLASHURL);
		new Thread(new StartUpThread()).start();
		UrlWaitLatch.waitFor(URL, USER, PASSWORD);
		assertEquals("Listening for transport dt_socket at address: 8000",
				jvmMsg);
	}

	@Test
	public void testdmServerStartWithSpecificDebugPort()
			throws InterruptedException {
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully(SPLASHURL);
		new Thread(new StartUpThreadWithSpecificDebugPort()).start();
		UrlWaitLatch.waitFor(URL, USER, PASSWORD);
		assertEquals("Listening for transport dt_socket at address: 8001",
				jvmMsg);
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		new Thread(new ShutdownThread()).start();
		UrlWaitLatch.waitForServerShutdownFully(SPLASHURL);
	}

	private static class StartUpThread implements Runnable {

		public StartUpThread() {

		}

		public void run() {
			String[] msg = new String[100];
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
				args = new String[] { startupFileName, "-debug", "-clean" };
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
					for (int i = 0; i < msg.length; i++) {
						if (line
								.contentEquals("Listening for transport dt_socket at address: 8000")) {
							jvmMsg = line;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class StartUpThreadWithSpecificDebugPort implements Runnable {

		public StartUpThreadWithSpecificDebugPort() {

		}

		public void run() {
			String[] msg = new String[100];
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
				args = new String[] { startupFileName, "-debug", "8001",
						"-clean" };
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
					for (int i = 0; i < msg.length; i++) {
						if (line
								.contentEquals("Listening for transport dt_socket at address: 8001")) {
							jvmMsg = line;
						}
					}
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
