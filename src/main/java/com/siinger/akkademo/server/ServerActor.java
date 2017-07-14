package com.siinger.akkademo.server;

import org.apache.log4j.Logger;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.message.Request;
import com.message.Response;
import com.siinger.akkademo.utils.ActorCommand;
import com.siinger.akkademo.utils.BeanUtils;
import com.siinger.akkademo.utils.PropertiesUtils;

import akka.actor.UntypedActor;

/**
 * ClassName: MasterActor <br/>
 * Function: 接受client消息 <br/>
 * date: 2016年8月5日 下午5:33:40 <br/>
 *
 * @author siinger
 * @version 
 * @since JDK 1.7
 */
public class ServerActor extends UntypedActor {

	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void onReceive(Object message) throws Exception {
		if (message == ActorCommand.HeartBeat) {
			logger.info(getSender().path().address().host().get() + " agent is start");
			getSender().tell(ActorCommand.HeartBeat_OK, getSelf());
		} else if (message == ActorCommand.DO_SOMETHING_OK) {
			logger.info(getSender().path().address().host().get() + " do something ok!");
			getContext().stop(getSelf());
		}else if(message instanceof String){
			logger.info(PropertiesUtils.get("serverId")+">>>>>>>>>>"+message);
		}else if(message instanceof Request){
			Request packet = (Request)message;
			short cmd = packet.getCmd();
			MessageLite packetMessage = BeanUtils.protobufMapping.message(cmd);
			if(packetMessage!=null){
				MessageLite msg = packetMessage.getParserForType().parseFrom(packet.getBytes());
				Object response = BeanUtils.commandDispatcher.dispatch(msg);
				if(response!=null){
					getSender().tell(response, getSelf());
				}
			}
		}else if(message instanceof Response){
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
	}
}
