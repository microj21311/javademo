package com.wbzf.demo;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * 订单创建
 *
 */
public class App {
	public static void main(String[] args) {
		// -----BEGIN PUBLIC KEY-----
		// MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCy5CulBZvqtUqL1w5iaO6lJibW
		// CU3yuIes+K65cxZVDscu0i6KPju1ktOPCVxSsWybMEejXsX0xn1Vyx6Aglnp+cd2
		// c6xpIAwg9e6N9G95R+tlh5efhDB3f+RoUXzOmqpqtjs0KdUDIbsJ68W4OMRIDL6A
		// C8ae1GgDbw6Areb7kwIDAQAB
		// -----END PUBLIC KEY-----
		RSAEncrypt rsa = new RSAEncrypt();
		Map<String, String> p = new HashMap<>();
		p.put("merchantId", "800000");
		String url = "https://user.wbzf.info:4431/merchant-api/api/open/createOrder";
		// JAVA使用PEM密钥只需把首尾两行去掉，然后换行去掉改为同一行就行
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCy5CulBZvqtUqL1w5iaO6lJibWCU3yuIes+K65cxZVDscu0i6KPju1ktOPCVxSsWybMEejXsX0xn1Vyx6Aglnp+cd2c6xpIAwg9e6N9G95R+tlh5efhDB3f+RoUXzOmqpqtjs0KdUDIbsJ68W4OMRIDL6AC8ae1GgDbw6Areb7kwIDAQAB";
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("merchantOrderNo", "C22230224662222");
		jsonMap.put("amount", 1000);
		jsonMap.put("type", 0);
		jsonMap.put("notifyUrl", "http://www.baidu.com/callback");
		jsonMap.put("remark", "XXXX");
		jsonMap.put("ip", "45.58.55.55");
		String data = rsa.encryptByPublicKey(publicKey, JSON.toJSONString(jsonMap));
		p.put("data", data);
		String result = HttpClientUtils.doPost(url, null, p, null);
		System.out.println(result);
		JSONObject resultJson = JSON.parseObject(result);
		if (resultJson.getIntValue("code") == 0) {
			System.out.println(rsa.decryptByPublicKey(publicKey, resultJson.getString("data")));
		}
	}
}
