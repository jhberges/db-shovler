package com.onsmsc.db.shovler;

import static org.junit.Assert.assertTrue;

import javax.jms.JMSException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

@RunWith(MockitoJUnitRunner.class)
public class ShovlerBeanTest {
	@Mock
	private JmsTemplate jmsTemplate;
	@Mock
	private JdbcTemplate jdbcTemplate;
	@InjectMocks
	private ShovlerBean shovlerBean;
	@Test
	public void pauseOnException() throws Exception {
		long t0 = System.currentTimeMillis();
		shovlerBean.pauseOnException(new JMSException("IRRELLEVANT_REASON"));
		assertTrue(ShovlerBean.PAUSE_ON_EXCEPTION < System.currentTimeMillis() - t0);
	}
}
