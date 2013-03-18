package com.onsmsc.db.shovler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

public class ShovlerBean implements Lifecycle, Runnable {
	private static final long DEFAULT_BATCH_SIZE = 200;
	private static final long DEFAULT_RECEIVE_TIMEOUT = 200;
	private static final Logger logger = LoggerFactory.getLogger(ShovlerBean.class);
	public static final long DEFAULT_PAUSE_ON_EXCEPTION = 5000;
	private String destination;
	private JmsTemplate jmsTemplate;
	private JdbcTemplate jdbcTemplate;
	private PreparedStatementBatchStepMessageConverter batchStepMessageConverter;
	private final long maxBatchSize = DEFAULT_BATCH_SIZE;
	private boolean running = false;
	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
	private String deadLetterQueue;
	private long pauseOnExceptionWait = DEFAULT_PAUSE_ON_EXCEPTION;
	@Override
	public void run() {
		logger.debug("Running");
		while(running) {
			try {
				processInLoop();
			} catch (JMSException jmsException) {
				pauseOnException(jmsException);
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

		int[] updated = jdbcTemplate.batchUpdate(batchStepMessageConverter.getSql(), batchArgs);
		for (int i = 0; i < updated.length; i++) {
			if (1 == updated[i]) {
				receivedMessages.get(i).acknowledge();
			} else {
				handleDidNotUpdateDatabase(receivedMessages.get(i));
			}
		}
	}

	void handleDidNotUpdateDatabase(final Message message) throws JMSException {
		logger.warn("Failed to update database for message: {}", message);
		message.acknowledge();
		if (null != deadLetterQueue) {
			jmsTemplate.convertAndSend(deadLetterQueue, message);
			logger.info("Put on DLQ: {}", deadLetterQueue);
		} else {
			logger.warn("Message dropped");
		}
	}

	void pauseOnException(final JMSException jmsException) {
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


	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(final JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Override
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

	@Override
	public void start() {
		logger.info("Starting");
		Thread thread = Executors
			.defaultThreadFactory()
			.newThread(this);
		thread.setDaemon(false);
		thread.start();
		running = true;
	}

	@Override
	public void stop() {
		running = false;
	}
}
