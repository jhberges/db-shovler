package com.onsmsc.db.shovler;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class ShovlerBean {
	private static final long DEFAULT_BATCH_SIZE = 200;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private String jmsPassword;
	private String jmsUsername;
	private Session session;
	private Destination destination;
	private MessageConsumer consumer;
	private final long maxBatchSize = DEFAULT_BATCH_SIZE;
	public void process() {
		while(running) {
			try {
				ensureInitialized();
				processInLoop();
			} catch (JMSException jmsException) {
				pauseOnException(jmsException);
			}
		}
	}

	private void processInLoop() {
		// TODO Auto-generated method stub

	}

	private void pauseOnException(JMSException jmsException) {
		// TODO Auto-generated method stub

	}

	private void ensureInitialized() throws JMSException {
		connection = connectionFactory.createConnection(jmsUsername, jmsPassword);
		session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
		consumer = session.createConsumer(destination);
		connection.start();
	}
}
