package com.onsmsc.db.shovler.metrics;

import javax.jms.Connection;

import org.springframework.jms.core.JmsTemplate;

import com.codahale.metrics.health.HealthCheck;

public class JmsHealthCheck extends HealthCheck {

	private final JmsTemplate jmsTemplate;

	public JmsHealthCheck(final JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected Result check() throws Exception {
		Connection connection = null;
		try {
			connection = jmsTemplate.getConnectionFactory().createConnection();
			return Result.healthy();
		} catch (Exception e) {
			throw e;
		} finally {
			connection.close();
		}
	}

}
