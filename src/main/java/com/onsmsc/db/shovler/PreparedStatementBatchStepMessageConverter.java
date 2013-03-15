package com.onsmsc.db.shovler;

import javax.jms.JMSException;
import javax.jms.Message;

public interface PreparedStatementBatchStepMessageConverter {

	public Object[] toPreparedStatementArgs(final Message message) throws JMSException;

	public String getSql();

}
