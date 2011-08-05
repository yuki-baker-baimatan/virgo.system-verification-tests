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

import java.io.File;


import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.junit.Test;
import static org.junit.Assert.assertFalse;


public class VerificationOfRemovalOfWorkAndServiceabilityDirectoriesUponCleanStartTests
		extends AbstractWebTests {

	@Test
	public void testRemovalOfWorkAndServiceabilityDir() throws Exception {

		File file1 = new File(getWorkDir(), "temp.txt");
		assertFalse("work dir is not cleaned up properly", file1.exists());
		File file2= new File(getServiceabilityDir(), "temp.txt");
		assertFalse("serviceability dir is not cleaned up properly", file2
				.exists());
	}

}
