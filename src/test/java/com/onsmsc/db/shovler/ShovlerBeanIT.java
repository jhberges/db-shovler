package com.onsmsc.db.shovler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/integration/applicationContext.xml"})
public class ShovlerBeanIT {
	private final Logger logger = LoggerFactory.getLogger(ShovlerBeanIT.class);
	@Autowired
	private ActiveMQConnectionFactory connectionFactory;
	@Autowired
	private DataSource datasource;
	@Test
	public void testDelivery() throws Exception {
		assertNotNull(connectionFactory);
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		Map<String, String> map = new HashMap <String, String>();
		map.put("MESSAGE_ID", "MY_MESSAGE_ID");
		map.put("PAYLOAD", "THE PAYLOAD");
		logger.info("Sending message");
		jmsTemplate.convertAndSend("SHOVLER_DESTINATION", map);
		logger.info("Message sent!");
		Thread.sleep(50000);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
		int count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM LOGGER WHERE MESSAGE_ID='MY_MESSAGE_ID'");
		assertEquals(1, count);
	}
}
