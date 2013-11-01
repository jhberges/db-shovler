package com.onsmsc.db.shovler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Statement;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

@RunWith(MockitoJUnitRunner.class)
public class ShovlerBeanTest {
	@Mock
	private JmsTemplate jmsTemplate;
	@Mock
	private JdbcTemplate jdbcTemplate;
	@Mock
	private MetricRegistry metricRegistry;
	@Mock
	private HealthCheckRegistry healthCheckRegistry;
	@InjectMocks
	private ShovlerBean shovlerBean;
	@Mock
	private Message message;
	@Mock
	private PreparedStatementBatchStepMessageConverter batchStepMessageConverter;

	@Before
	public void before() throws Exception {
		shovlerBean.setMetricRegistry(null);
		shovlerBean.initMetrics();
	}

	@After
	public void after() throws Exception {
		Mockito.verifyNoMoreInteractions(message, metricRegistry, healthCheckRegistry);
	}
	@Test
	public void pauseOnException() throws Exception {
		shovlerBean.setPauseOnExceptionWait(200);
		long t0 = System.currentTimeMillis();
		shovlerBean.pauseOnException(new JMSException("IRRELLEVANT_REASON"));
		long diff = System.currentTimeMillis() - t0;
		assertTrue("Expected >= 200, was " + diff, 200 <= diff);
	}
	@Test
	public void handleDidNotUpdateDatabaseWhenNoDlqAssigned() throws Exception {
		shovlerBean.handleDidNotUpdateDatabase(message);
		Mockito.verify(message).acknowledge();
		Mockito.verifyNoMoreInteractions(jmsTemplate);
	}

	@Test
	public void handleDidNotUpdateDatabaseWhenDlqAssigned() throws Exception {
		shovlerBean.setDeadLetterQueue("IRRELLEVANT_DLQ_NAME");
		shovlerBean.handleDidNotUpdateDatabase(message);
		Mockito.verify(message)
			.acknowledge();
		Mockito.verify(jmsTemplate)
			.convertAndSend(Mockito.eq("IRRELLEVANT_DLQ_NAME"), Mockito.any(Message.class));
		Mockito.verifyNoMoreInteractions(jmsTemplate);
	}

	@Test
	public void processInLoop() throws Exception {
		shovlerBean.setDestination("IRRELLEVANT_SOURCE_DESTINATION");
		Mockito.when(jmsTemplate.receive(Mockito.anyString()))
			.thenReturn(message, null);
		Mockito.when(batchStepMessageConverter.toPreparedStatementArgs(Mockito.any(Message.class)))
			.thenReturn(new Object[]{Integer.valueOf(2)});
		shovlerBean.setBatchStepMessageConverter(batchStepMessageConverter);
		Mockito.when(jdbcTemplate.batchUpdate(Mockito.anyString(), Mockito.anyList()))
			.thenReturn(new int[]{1});
		shovlerBean.processInLoop();
		Mockito.verify(jmsTemplate).setReceiveTimeout(Mockito.eq(shovlerBean.getReceiveTimeout()));
		Mockito.verify(jmsTemplate, Mockito.times(2)).receive(Mockito.eq(shovlerBean.getDestination()));
		Mockito.verify(batchStepMessageConverter).getSql();
		Mockito.verify(jdbcTemplate).batchUpdate(Mockito.anyString(), Mockito.anyList());
		Mockito.verify(batchStepMessageConverter).toPreparedStatementArgs(Mockito.any(Message.class));
		Mockito.verify(message).acknowledge();

		Mockito.verifyNoMoreInteractions(jmsTemplate, jdbcTemplate, batchStepMessageConverter);
	}

	@Test
	public void successResponseWhenNegative() throws Exception {
		assertFalse(ShovlerBean.successResponse(-1));
	}

	@Test
	public void successResponseWhenNO_INFO() throws Exception {
		assertTrue(ShovlerBean.successResponse(Statement.SUCCESS_NO_INFO));
	}

	@Test
	public void successResponseWhenOneRowUdated() throws Exception {
		assertTrue(ShovlerBean.successResponse(1));
	}

	@Test
	public void setMetricRegistry() {
		shovlerBean.setMetricRegistry(metricRegistry);
		shovlerBean.initMetrics();
		Mockito.verify(metricRegistry, Mockito.times(4)).meter(Mockito.anyString());
		Mockito.verify(metricRegistry, Mockito.times(1)).timer(Mockito.anyString());
	}
}
