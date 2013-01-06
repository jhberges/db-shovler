package com.onsmsc.db.shovler;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProcessorBean implements MessageListener {
	private final Logger logger = LoggerFactory.getLogger(MessageProcessorBean.class);
	public void onMessage(Message message) {
		// TODO Auto-generated method stub

	}

}
