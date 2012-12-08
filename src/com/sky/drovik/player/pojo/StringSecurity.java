package com.sky.drovik.player.pojo;

import org.apache.commons.codec.binary.Base64;

public class StringSecurity {

	private StringSecurity() {
		
	}
	
	public static String encodeName(byte []b) {
		return Base64.encodeBase64String(b);
	}
	
	public static byte[] decodeName(String name) {
		return Base64.decodeBase64(name.getBytes());
	}
	
}
