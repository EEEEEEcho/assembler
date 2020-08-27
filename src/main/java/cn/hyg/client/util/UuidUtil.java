package cn.hyg.client.util;

import java.util.UUID;

public class UuidUtil {

	public static String get32UUID() {
		return UUID.randomUUID().toString().trim().replaceAll("-", "");
	}

	public static String get32UUIDFull() {
		return UUID.randomUUID().toString().trim();
	}

}

