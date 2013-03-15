package com.onsmsc.db.shovler;

import javax.jms.JMSException;
import javax.jms.Message;

public class DummyMessageConverter extends
		DefaultPreparedStatementBatchStepMessageConverter {

	private static final String MESSAGE_ID = "MESSAGE_ID";
	private static final String PAYLOAD = "PAYLOAD";

	public DummyMessageConverter(String sql) {
		super(sql);
	}

	@Override
	public Object[] toPreparedStatementArgs(Message message) throws JMSException {
		String id = message.getStringProperty(MESSAGE_ID);
		String payload = message.getStringProperty(PAYLOAD);
		return new String[]{id, payload};
	}

}
