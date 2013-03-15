package com.onsmsc.db.shovler;

import static org.junit.Assert.assertTrue;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;

public class ShovlerBeanTest {
	private ShovlerBean shovlerBean;
	@Before
	public void before() {
		shovlerBean = new ShovlerBean();
	}
	@Test
	public void pauseOnException() throws Exception {
		long t0 = System.currentTimeMillis();
		shovlerBean.pauseOnException(new JMSException("IRRELLEVANT_REASON"));
		assertTrue(ShovlerBean.PAUSE_ON_EXCEPTION < System.currentTimeMillis() - t0);
	}
}
