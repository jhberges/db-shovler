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
	private InserterBean inserterBean;
	private final long maxBatchSize = DEFAULT_BATCH_SIZE;
	private final boolean running = true;
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

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public String getJmsPassword() {
		return jmsPassword;
	}

	public void setJmsPassword(String jmsPassword) {
		this.jmsPassword = jmsPassword;
	}

	public String getJmsUsername() {
		return jmsUsername;
	}

	public void setJmsUsername(String jmsUsername) {
		this.jmsUsername = jmsUsername;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public long getMaxBatchSize() {
		return maxBatchSize;
	}

	public InserterBean getInserterBean() {
		return inserterBean;
	}

	public void setInserterBean(InserterBean inserterBean) {
		this.inserterBean = inserterBean;
	}
}
