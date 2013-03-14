package com.onsmsc.db.shovler;

import javax.jms.Message;

public interface PreparedStatementBatchStepMessageConverter {

	public Object[] toPreparedStatementArgs(final Message message) ;

	public String getSql();

}
