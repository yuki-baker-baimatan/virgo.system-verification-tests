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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.util.io.FileCopyUtils;

public class AddAndRemoveBundlesToRepositoryTests extends AbstractWebTests {

	private static String[] bundleNames = new String[] {
			"com.springsource.freemarker-2.3.15.jar",
			"com.springsource.javax.persistence-1.0.0.jar",
			"com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar",
			"com.springsource.org.apache.commons.pool-1.3.0.jar",
			"com.springsource.org.eclipse.persistence-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.antlr-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.asm-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.jpa-1.0.0.jar",
			"com.springsource.org.h2-1.0.71.jar", "hello.domain.jar",
			"hello.service.api.jar", "hello.service.impl-en.jar","bundleA.jar","bundleB.war","foo.properties","appA.par" };
	private static String[] bundleNames_watched = new String[] {
			"com.springsource.freemarker-2.3.15.jar",
			"com.springsource.javax.persistence-1.0.0.jar",
			"com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar",
			"com.springsource.org.apache.commons.pool-1.3.0.jar",
			"com.springsource.org.eclipse.persistence-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.antlr-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.asm-1.0.0.jar",
			"com.springsource.org.eclipse.persistence.jpa-1.0.0.jar",
			"com.springsource.org.h2-1.0.71.jar" };
	private static String[] bundle = new String[] { "org.springframework.dmServer.testtool.incoho.domain-1.0.0.RELEASE.jar" };
	private static String[] library = new String[] { "org.springframework.spring-library-2.5.4.A.libd" };
	private static final String bundlesDir = "src/test/resources/bundles";
	private static final String librariesDir = "src/test/resources/libraries";
	private static String repositoryPropertiesDir = "src/test/resources/org.eclipse.virgo.repository.properties";
	private static String hostedRepositoryPropertiesDir = "src/test/resources/org.eclipse.virgo.apps.repository.properties";

	public void addBundlesToRepository(String repoDir) throws Exception {
		for (String bundleName : bundle) {
			FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
					repoDir, bundleName));
		}
	}

	public void addLibrariesToRepository(String repoDir) throws Exception {
		for (String libraryName : library) {
			FileCopyUtils.copy(new File(librariesDir, libraryName), new File(
					repoDir, libraryName));
		}
	}

	public void addBundlesAndLibrariesToHostedRepository(String repoDir)
			throws Exception {
		for (String bundleName : bundleNames) {
			FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
					repoDir, bundleName));
		}
		for (String libraryName : library) {
			FileCopyUtils.copy(new File(librariesDir, libraryName), new File(
					repoDir, libraryName));
		}
	}

	public void addBundlesAndLibrariesToWatchedRepository(String repoDir)
			throws Exception {
		for (String bundleName : bundleNames_watched) {
			FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(
					repoDir, bundleName));
		}
		for (String libraryName : library) {
			FileCopyUtils.copy(new File(librariesDir, libraryName), new File(
					repoDir, libraryName));
		}
	}

	public void updateHostedRepoProperties(String hostedRepoPropertiesDir)
			throws IOException {
		FileCopyUtils.copy(new File(hostedRepositoryPropertiesDir), new File(
				hostedRepoPropertiesDir));
	}

	public void updateRepositoryProperties(String repoPropertiesDir)
			throws IOException {
		FileCopyUtils.copy(new File(repositoryPropertiesDir), new File(
				repoPropertiesDir));
	}

	@Test
	public void testAddedBundlesToRepository() throws Exception {
		addBundlesToRepository(getRepositoryUsrDir());
		addBundlesToRepository(getRepositoryExtDir());
		addLibrariesToRepository(getRepositoryUsrDir());
		addLibrariesToRepository(getRepositoryExtDir());
		updateHostedRepoProperties(getHostedRepositoryPropertiesDir());
		addBundlesAndLibrariesToHostedRepository(getHostedRepoDir());
		updateRepositoryProperties(getRepositoryPropertiesDir());
		addBundlesAndLibrariesToWatchedRepository(getWatchedRepoDir());
	}
}
