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

package org.eclipse.virgo.server.svt.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.virgo.server.svt.AbstractWebTests;
import org.eclipse.virgo.server.svt.UrlWaitLatch;
import org.junit.Test;
import com.dumbster.smtp.SimpleSmtpServer;

public class WarEmailDeployAndUndeployTests extends AbstractWebTests {

	private static final String APP_EMAIL_SHARED_LIBS_URI = "./apps/email-shared-libs-war.war";
	private static final String APP_EMAIL_STANDARD_WAR_URI = "./apps/email-standard-war.war";

	protected static final int SMTP_PORT = 2525;
	SimpleSmtpServer server = null;
	private static String type1 = null;
	private static String name1 = null;
	private static String version1 = null;
	
	private static String type2 = null;
	private static String name2 = null;
	private static String version2 = null;
	
	private static String[] signature = null;
	private static Object[] params = null;

	@Test
	public void testEmailSharedLibsWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_EMAIL_SHARED_LIBS_URI).toURI()
				.toString() };
		CompositeData compositeData = deploy(signature, params);
		name1 = compositeData.get("symbolicName").toString();
		type1 = compositeData.get("type").toString();
		version1 = compositeData.get("version").toString();
		waitForMBeanRegister(type1, name1, version1, HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/email-shared-libs-war/send?subject=Hola,%20Mundo");
		server = SimpleSmtpServer.start(SMTP_PORT);

		assertEquals("Verifying initial number of e-mails sent.", 0, server
				.getReceivedEmailSize());
		UrlWaitLatch
				.waitFor("http://localhost:8080/email-shared-libs-war/send?subject=Hola,%20Mundo");

		assertEquals("Verifying final number of e-mails sent.", 1, server
				.getReceivedEmailSize());
		server.stop();
		assertArtifactExists(type1, name1, version1);
		assertArtifactState(type1, name1, version1, "ACTIVE");
	}

	@Test
	public void testEmailSharedLibsWarUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { name1, version1 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(type1, name1, version1, HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/email-shared-libs-war");
		assertArtifactNotExists(type1, name1, version1);
	}

	@Test
	public void testEmailStandardWarDeploy() throws Exception {
		signature = new String[] { String.class.getName() };
		params = new Object[] { new File(APP_EMAIL_STANDARD_WAR_URI).toURI()
				.toString() };
		CompositeData compositeData = deploy(signature, params);
		name2 = compositeData.get("symbolicName").toString();
		type2 = compositeData.get("type").toString();
		version2 = compositeData.get("version").toString();
		waitForMBeanRegister(type2, name2, version2, HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitFor("http://localhost:8080/email-standard-war/send?subject=Hola,%20Mundo");
		server = SimpleSmtpServer.start(SMTP_PORT);

		assertEquals("Verifying initial number of e-mails sent.", 0, server
				.getReceivedEmailSize());
		UrlWaitLatch
				.waitFor("http://localhost:8080/email-standard-war/send?subject=Hola,%20Mundo");
		assertEquals("Verifying final number of e-mails sent.", 1, server
				.getReceivedEmailSize());
		server.stop();
		assertArtifactExists(type2, name2, version2);
		assertArtifactState(type2, name2, version2, "ACTIVE");
	}

	@Test
	public void testEmailStandardWarUndeploy() throws Exception {
		signature = new String[] { String.class.getName(),
				String.class.getName() };
		params = new Object[] { name2, version2 };
		getMBeanServerConnection()
				.invoke(
						new ObjectName(
								"org.eclipse.virgo.kernel:category=Control,type=Deployer"),
						"undeploy", params, signature);
		waitForMBeanDeRegister(type2, name2, version2, HALF_SECOND, TWO_MINUTES);
		UrlWaitLatch
				.waitForNotExistence("http://localhost:8080/email-standard-war");
		assertArtifactNotExists(type2, name2, version2);
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
}
