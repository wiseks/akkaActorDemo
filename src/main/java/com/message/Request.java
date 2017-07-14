package com.message;

import java.io.Serializable;

import com.google.protobuf.MessageLite;

public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*内容*/
	private final byte[] bytes;
	
	private final Short cmd;
	
	
	public Request(Class<?> clazz,MessageLite message) {
		String name = clazz.getSimpleName();
		this.cmd = Short.valueOf(name.split("_")[1]);;
		this.bytes = message.toByteArray();
	}

	/**
	 * 网络包的数据
	 * @return
	 */
	public byte[] getBytes() {
		return bytes;
	}
	
	
	public Short getCmd() {
		return cmd;
	}

}
