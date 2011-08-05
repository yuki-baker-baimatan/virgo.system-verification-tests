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

package org.eclipse.virgo.server.svt.dumpinspector;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.eclipse.virgo.util.io.FileSystemUtils;

public class DumpInspectorTests extends AbstractWebTests {
	private static final String APPS_DIR = "./src/test/resources";

	private static final String[] APPLICATION_NAMES_USES_VOILATION = new String[] {
			"eclipselink1.jar", "eclipselink2.jar", "spring.jar", "app1.jar" };

	private static final String[] APPLICATION_NAMES_CLASSNOTFOUND = new String[] { "formtags-par-1.4.0.RELEASE_FAILURE.par" };
	private static final String EXCLUDE_PATTERN = ".DS_Store";

	@Before
	public void testContextSetup() throws Exception {
		getTestContext().setBaseUrl(
				"http://localhost:8080/admin/web/dump/inspector.htm");
		getTestContext().setAuthorization(AbstractWebTests.USER,
				AbstractWebTests.PASSWORD);
	}

	@Test
	public void testDumpContentInAdminScreenUponDumpGenerationOfUsesVoilation()
			throws Exception {
		if (getDumpDir() != null) {
			FileSystemUtils.deleteRecursively(getDumpDir());
		}
		copyApplicationsToPickup(getPickupDir(), APPS_DIR,
				APPLICATION_NAMES_USES_VOILATION);
		Thread.sleep(2000);
		UrlWaitLatch.waitFor(
				"http://localhost:8080/admin/web/dump/inspector.htm", USER,
				PASSWORD);
		getTester().beginAt(
				"http://localhost:8080/admin/web/dump/inspector.htm");

		File[] dumpIds = getDumpIds();
		if (dumpIds != null) {
			for (int i = 0; i < dumpIds.length; i++) {
				if (!dumpIds[i].getName().equals(EXCLUDE_PATTERN)) {
					getTester().clickButton("dump_selector_submit");
					File[] dumpEntries = getDumpEntries(dumpIds[i]);
					for (int j = 0; j < dumpEntries.length; j++) {
						if (dumpEntries[j].getName().startsWith("summary")
								&& dumpEntries[j].getName().endsWith(".txt")) {
							getTester().selectOptionByValue("dumpEntryName",
									dumpEntries[j].getName());
							getTester().clickButton("dump_selector_submit");
							getTester().assertTextPresent(
									"Cause: resolutionFailure");
						}
					}
				}
			}
		}
		FileSystemUtils.deleteRecursively(getDumpDir());
		deleteApplicationsFromPickup(getPickupDir(),
				APPLICATION_NAMES_USES_VOILATION);
	}

	@Ignore("Since dump is not generating on ClassNotFoundException as of 2.0.0.M5")
	@Test
	public void testDumpContentInAdminScreenUponDumpGenerationOfClassNotFounException()
			throws Exception {

		if (getDumpDir() != null) {
			FileSystemUtils.deleteRecursively(getDumpDir());
		}

		copyApplicationsToPickup(getPickupDir(), APPS_DIR,
				APPLICATION_NAMES_CLASSNOTFOUND);
		Thread.sleep(2000);

		getTester().beginAt("http://localhost:8080/admin/web/dump/dump.htm");

		File[] dumpIds = getDumpIds();
		if (dumpIds != null) {

			for (int i = 0; i < dumpIds.length; i++) {

				if (!dumpIds[i].getName().equals(EXCLUDE_PATTERN)) {
					getTester().selectOptionByValue("dumpID",
							applyNameFormatting(dumpIds[i]));
					getTester().clickButtonWithText("Select Dump");
					File[] dumpEntries = getDumpEntries(dumpIds[i]);
					assertEquals(5, dumpEntries.length);
					for (int j = 0; j < dumpEntries.length; j++) {

						getTester().selectOptionByValue("dumpEntryName",
								dumpEntries[j].getName());
						if (j == 0) {
							assertEquals(true, dumpEntries[0].getName()
									.startsWith("osgi")
									&& dumpEntries[0].getName()
											.endsWith(".zip"));
						}
						if (j == 1) {
							assertEquals(true, dumpEntries[1].getName()
									.startsWith("summary")
									&& dumpEntries[1].getName()
											.endsWith(".txt"));
						}
						if (j == 2) {
							assertEquals(true, dumpEntries[2].getName()
									.startsWith("system")
									&& dumpEntries[2].getName().endsWith(
											".json"));
						}
						if (j == 3) {
							assertEquals(true, dumpEntries[3].getName()
									.startsWith("thread")
									&& dumpEntries[3].getName()
											.endsWith(".txt"));
						}
						if (j == 4) {
							assertEquals(true, dumpEntries[4].getName()
									.startsWith("trickle")
									&& dumpEntries[4].getName()
											.endsWith(".log"));
						}
						getTester().assertTextPresent("Dump Entry Viewer");
						if (dumpEntries[j].getName().startsWith("summary")
								&& dumpEntries[j].getName().endsWith(".txt")) {

							getTester().clickButtonWithText("Select Entry");
							getTester().assertTextPresent("Cause: ERROR");
						}

					}
				}
			}
		}

		deleteApplicationsFromPickup(getPickupDir(),
				APPLICATION_NAMES_CLASSNOTFOUND);
		Thread.sleep(1000);
		FileSystemUtils.deleteRecursively(getDumpDir());
	}

}
