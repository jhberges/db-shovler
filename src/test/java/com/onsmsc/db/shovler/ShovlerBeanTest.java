package com.onsmsc.db.shovler;

import static org.junit.Assert.assertTrue;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
	@Mock
	private Message message;
	@Mock
	private PreparedStatementBatchStepMessageConverter batchStepMessageConverter;
	@After
	public void after() throws Exception {
		Mockito.verifyNoMoreInteractions(message);
	}
	@Test
	public void pauseOnException() throws Exception {
		shovlerBean.setPauseOnExceptionWait(200);
		long t0 = System.currentTimeMillis();
		shovlerBean.pauseOnException(new JMSException("IRRELLEVANT_REASON"));
		assertTrue(200 < System.currentTimeMillis() - t0);
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
}
