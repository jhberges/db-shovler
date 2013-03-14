package com.onsmsc.db.shovler;

public abstract class DefaultPreparedStatementBatchStepMessageConverter implements PreparedStatementBatchStepMessageConverter{

	private final String sql;
	public DefaultPreparedStatementBatchStepMessageConverter(final String sql) {
		this.sql = sql;
	}
	
	@Override
	public String getSql() {
		return sql;
	}

}
