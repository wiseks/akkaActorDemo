package com.rpg.logic.player.handler;

import com.message.MessageMsg.UserInfoMsg_23001;
import com.message.PacketMsg.UserInfoMsg_23002;
import com.message.Response;
import com.rpg.framework.annotation.MessageHandler;
import com.rpg.framework.annotation.MessageMapping;

@MessageHandler
public class PlayerHandler {

	@MessageMapping(UserInfoMsg_23001.class)
	public Response userInfo(UserInfoMsg_23001 info){
		System.out.println("receiveMsg:"+info.getName());
		UserInfoMsg_23002.Builder userInfo = UserInfoMsg_23002.newBuilder();
		userInfo.setId(2);
		userInfo.setName("jack");
		Response res = new Response(UserInfoMsg_23002.class,userInfo.build());
		return res;
	}
}
