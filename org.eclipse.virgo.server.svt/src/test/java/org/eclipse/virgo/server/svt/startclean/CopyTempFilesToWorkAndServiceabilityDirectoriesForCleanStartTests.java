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

package org.eclipse.virgo.server.svt.startclean;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;


public class CopyTempFilesToWorkAndServiceabilityDirectoriesForCleanStartTests extends AbstractWebTests{

	private static final String APPS_DIR = "./src/test/resources";
	private static final String fileName = "temp.txt";

	protected static void copyTempFileToWorkAndServiceability(String tempDir,
			String fileName, String checkDir) throws IOException {
		FileCopyUtils.copy(new File(tempDir, fileName), new File(checkDir,
				fileName));
	}

	@Test
	public void testCopyTempFileToWorkAndServiceabilityDir() throws Exception {
		copyTempFileToWorkAndServiceability(APPS_DIR, fileName, getWorkDir());
		copyTempFileToWorkAndServiceability(APPS_DIR, fileName,
				getServiceabilityDir());

		File file = new File(getWorkDir(), "temp.txt");
		assertTrue("temp file copied to work dir", file.exists());
		file = new File(getServiceabilityDir(), "temp.txt");
		assertTrue("temp file copied to serviceability dir", file.exists());
	}
}
