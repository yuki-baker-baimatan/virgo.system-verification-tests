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

package org.eclipse.virgo.server.svt.configadmin;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;

public class AddPropertyFilesForConfigAdminTests extends AbstractWebTests {

	private static final String APPS_DIR = "src/test/resources";
	private static final String propertyFileName = "com.springsource.test.properties";

	@Test
	public void copyApplicationsToPickup() throws IOException,
			InterruptedException {
		FileCopyUtils.copy(new File(APPS_DIR, propertyFileName), new File(
				getConfigDir(), propertyFileName));
	}

}
