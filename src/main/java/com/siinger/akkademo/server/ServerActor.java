package com.siinger.akkademo.server;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageLite;
import com.siinger.akkademo.utils.BeanUtils;
import com.siinger.akkademo.utils.PropertiesUtils;

import akka.actor.UntypedAbstractActor;

/**
 * ClassName: MasterActor <br/>
 * Function: 接受client消息 <br/>
 * date: 2016年8月5日 下午5:33:40 <br/>
 *
 * @author siinger
 * @version 
 * @since JDK 1.7
 */
public class ServerActor extends UntypedAbstractActor {

	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof String){
			logger.info(PropertiesUtils.get("serverId")+">>>>>>>>>>"+message);
		}else if(message instanceof GeneratedMessageLite){
			Object response = BeanUtils.commandDispatcher.dispatch((GeneratedMessageLite)message);
			if(response!=null){
				getSender().tell(response, getSelf());
			}
		}
	}
}
