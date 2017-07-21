package com.siinger.akkademo.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rpg.framework.dispatch.CommandDispatcher;

public class BeanUtils {

	//public static ProtobufMapping protobufMapping = new ProtobufMapping();
	public static CommandDispatcher commandDispatcher = new CommandDispatcher();
	
	public static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
	public static void init(){
//		protobufMapping.initialize();
		commandDispatcher.init(applicationContext);
	}
}
