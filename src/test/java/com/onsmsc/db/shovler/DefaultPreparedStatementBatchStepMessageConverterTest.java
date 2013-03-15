package com.onsmsc.db.shovler;

import static org.junit.Assert.assertEquals;

import javax.jms.Message;

import org.junit.Test;

public class DefaultPreparedStatementBatchStepMessageConverterTest {

	@Test
	public void getSql() {
		final String sql = "IRRELLEVANT_SQL";

		DefaultPreparedStatementBatchStepMessageConverter converter = new DefaultPreparedStatementBatchStepMessageConverter(sql) {

			@Override
			public Object[] toPreparedStatementArgs(Message message) {
				return null;
			}
		};
		assertEquals(sql, converter.getSql());
	}

}
