package com.onsmsc.db.shovler;

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class ShovlerBean implements InitializingBean, Runnable {
	private static final long DEFAULT_BATCH_SIZE = 200;
	private static final long DEFAULT_RECEIVE_TIMEOUT = 200;
	private static final Logger logger = LoggerFactory.getLogger(ShovlerBean.class);
	public static final long DEFAULT_PAUSE_ON_EXCEPTION = 5000;
	private String destination;
	private JmsTemplate jmsTemplate;
	private JdbcTemplate jdbcTemplate;
	private PreparedStatementBatchStepMessageConverter batchStepMessageConverter;
	private long maxBatchSize = DEFAULT_BATCH_SIZE;
	private boolean running = false;
	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
	private String deadLetterQueue;
	private long pauseOnExceptionWait = DEFAULT_PAUSE_ON_EXCEPTION;
	private Meter messagesProcessed;
	private Meter messagesFailed;
	private Meter dlqMessages;
	private Timer batchTimer;
	private Meter exceptionMeter;
	private MetricRegistry metricRegistry;

	@Override
	public void run() {
		logger.debug("Running");
		while(running) {
			try {
				processInLoop();
			} catch (JMSException jmsException) {
				exceptionMeter.mark();
				pauseOnException(jmsException);
			} catch (JmsException springJmsException) {
				exceptionMeter.mark();
				pauseOnException(springJmsException);
			}
		}
	}

	void processInLoop() throws JMSException {
		jmsTemplate.setReceiveTimeout(receiveTimeout);
		List<Message> receivedMessages = new ArrayList<Message>();
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		boolean timedOut = false;
		while (receivedMessages.size() < maxBatchSize && !timedOut) {
			Message message = jmsTemplate.receive(destination);
			if (null != message) {
				receivedMessages.add(message);
				batchArgs.add(batchStepMessageConverter.toPreparedStatementArgs(message));
			} else {
				timedOut = true;
			}
		}

		Context context = batchTimer.time();
		try {
			int[] updated = jdbcTemplate.batchUpdate(batchStepMessageConverter.getSql(), batchArgs);
			handleUpdateMisses(receivedMessages, updated);
			context.stop();
		} catch (DataAccessException e) {
			context.stop();
			exceptionMeter.mark();
			handleDataAccessException(receivedMessages, batchArgs, e);
		}
		messagesProcessed.mark(batchArgs.size());
	}

	private void handleDataAccessException(
			final List<Message> receivedMessages,
			final List<Object[]> batchArgs,
			final DataAccessException dataAccessException)
			throws JMSException {
		if (dataAccessException.getRootCause() instanceof BatchUpdateException){
			BatchUpdateException batchUpdateException = (BatchUpdateException) dataAccessException.getRootCause();
			int[] updates = batchUpdateException.getUpdateCounts();
			handleUpdateMisses(receivedMessages, updates);
		} else {
			logger.error("Failed to handle {} messages due to exception {} -- will add to DLQ", batchArgs.size(), dataAccessException.getMessage());
			pauseOnException(dataAccessException);
		}
	}

	private void handleUpdateMisses(List<Message> receivedMessages,
			int[] updated) throws JMSException {
		for (int i = 0; i < updated.length; i++) {
			if (successResponse(updated[i])) {
				receivedMessages.get(i).acknowledge();
			} else {
				this.messagesFailed.mark();
				handleDidNotUpdateDatabase(receivedMessages.get(i));
			}
		}
	}

	static boolean successResponse(final int response) {
		return -1 < response || response == Statement.SUCCESS_NO_INFO;
	}

	void handleDidNotUpdateDatabase(final Message message) throws JMSException {
		logger.warn("Failed to update database for message: {}", message);
		message.acknowledge();
		if (null != deadLetterQueue) {
			jmsTemplate.convertAndSend(deadLetterQueue, message);
			dlqMessages.mark();
			logger.info("Put on DLQ: {}", deadLetterQueue);
		} else {
			logger.warn("Message dropped");
		}
	}

	void pauseOnException(final Exception jmsException) {
		logger.warn("Exception when processing -- will pause a bit: " + jmsException.getMessage());
		try {
			Thread.sleep(pauseOnExceptionWait);
		} catch (InterruptedException e) {

		}
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(final String destination) {
		this.destination = destination;
	}

	public long getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(final long maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(final JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public boolean isRunning() {
		return running;
	}

	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(final long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getDeadLetterQueue() {
		return deadLetterQueue;
	}

	public void setDeadLetterQueue(final String deadLetterQueue) {
		this.deadLetterQueue = deadLetterQueue;
	}

	public PreparedStatementBatchStepMessageConverter getBatchStepMessageConverter() {
		return batchStepMessageConverter;
	}

	public void setBatchStepMessageConverter(
			final PreparedStatementBatchStepMessageConverter batchStepMessageConverter) {
		this.batchStepMessageConverter = batchStepMessageConverter;
	}

	public void setPauseOnExceptionWait(final long pauseOnExceptionWait) {
		this.pauseOnExceptionWait = pauseOnExceptionWait;

	}

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	public void setMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
		if (null != this.metricRegistry) {
			messagesProcessed = metricRegistry.meter("processedMessages");
			messagesFailed = metricRegistry.meter("failedMessages");
			dlqMessages = metricRegistry.meter("dlqMessages");
			batchTimer = metricRegistry.timer("batchExecutions");
			exceptionMeter = metricRegistry.meter("exceptions");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Starting");
		Thread thread = Executors
			.defaultThreadFactory()
			.newThread(this);
		thread.setDaemon(false);
		thread.start();
		running = true;
	}
}
