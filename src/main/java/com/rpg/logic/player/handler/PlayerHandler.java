package com.rpg.logic.player.handler;

import com.message.MessageMsg.UserInfoMsg_23001;
import com.rpg.framework.annotation.MessageHandler;
import com.rpg.framework.annotation.MessageMapping;

@MessageHandler
public class PlayerHandler {

	@MessageMapping(UserInfoMsg_23001.class)
	public void userInfo(UserInfoMsg_23001 info){
		System.out.println(info.getClass());
	}
}
