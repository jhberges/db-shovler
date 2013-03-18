package com.onsmsc.db.shovler;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

public class DummyMessageConverter extends
		DefaultPreparedStatementBatchStepMessageConverter {

	private static final String MESSAGE_ID = "MESSAGE_ID";
	private static final String PAYLOAD = "PAYLOAD";

	public DummyMessageConverter(final String sql) {
		super(sql);
	}

	@Override
	public Object[] toPreparedStatementArgs(final Message message) throws JMSException {
		MapMessage mapMessage = (MapMessage) message;
		String id = mapMessage.getString(MESSAGE_ID);
		String payload = mapMessage.getString(PAYLOAD);
		return new String[]{id, payload};
	}

}
