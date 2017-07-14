package com.message;

import java.io.Serializable;

public class Packet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*内容*/
	private final byte[] bytes;
	
	private final Short cmd;
	
	
	public Packet(Short cmd,byte[] bytes) {
		this.cmd = cmd;
		this.bytes = bytes;
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
