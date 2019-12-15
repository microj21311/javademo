package com.wbzf.demo;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http/Https请求的工具类
 */
public class HttpClientUtils {

	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private static SSLConnectionSocketFactory sslConnectionSocketFactory = null;
	private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;// 连接池管理类
	private static SSLContextBuilder sslContextBuilder = null;// 管理Https连接的上下文类

	static {
		try {
			sslContextBuilder = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
					// 信任所有站点 直接返回true
					return true;
				}
			});
			sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory> create().register(HTTP, new PlainConnectionSocketFactory()).register(HTTPS, sslConnectionSocketFactory).build();
			poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registryBuilder);
			poolingHttpClientConnectionManager.setMaxTotal(200);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取连接
	 *
	 * @return
	 * @throws Exception
	 */
	public static CloseableHttpClient getHttpClinet() {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setConnectionManager(poolingHttpClientConnectionManager).setConnectionManagerShared(true).build();
		return httpClient;
	}

	/**
	 * 发送post请求
	 *
	 * @param url
	 *            :请求地址
	 * @param header
	 *            :请求头参数
	 * @param params
	 *            :表单参数 form提交
	 * @param httpEntity
	 *            json/xml参数
	 * @return
	 */
	public static String doPost(String url, Map<String, String> header, Map<String, String> params, HttpEntity httpEntity) {
		String resultStr = "";
		if (url == null || url.isEmpty()) {
			return resultStr;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = HttpClientUtils.getHttpClinet();
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 连接超时
					.setConnectionRequestTimeout(5000)// 请求超时
					.setSocketTimeout(5000).build();// 套接字连接超时
			httpPost.setConfig(requestConfig);
			// 请求头header信息
			if (header != null && header.isEmpty()) {
				for (Map.Entry<String, String> stringStringEntry : header.entrySet()) {
					httpPost.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
				}
			}
			// 请求参数信息
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> paramList = new ArrayList<>();
				for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
					paramList.add(new BasicNameValuePair(stringStringEntry.getKey(), stringStringEntry.getValue()));
				}
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
				httpPost.setEntity(urlEncodedFormEntity);
			}
			// 实体设置
			if (httpEntity != null) {
				httpPost.setEntity(httpEntity);
			}

			// 发起请求
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpResponseEntity = httpResponse.getEntity();
				resultStr = EntityUtils.toString(httpResponseEntity);
			} else {
				StringBuffer stringBuffer = new StringBuffer();
				HeaderIterator headerIterator = httpResponse.headerIterator();
				while (headerIterator.hasNext()) {
					stringBuffer.append("\t" + headerIterator.next());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeConnection(httpClient, httpResponse);
		}
		return resultStr;
	}

	/**
	 * 发起post请求，状态为200时返回true，否则false
	 *
	 * @param url
	 * @param header
	 * @param params
	 * @param httpEntity
	 * @return
	 */
	public static boolean doPostStatus(String url, Map<String, String> header, Map<String, String> params, HttpEntity httpEntity) {
		if (url == null || url.isEmpty()) {
			return false;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = HttpClientUtils.getHttpClinet();
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 连接超时
					.setConnectionRequestTimeout(5000)// 请求超时
					.setSocketTimeout(5000).build();// 套接字连接超时
			httpPost.setConfig(requestConfig);
			// 请求头header信息
			if (header != null && header.isEmpty()) {
				for (Map.Entry<String, String> stringStringEntry : header.entrySet()) {
					httpPost.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
				}
			}
			// 请求参数信息
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> paramList = new ArrayList<>();
				for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
					paramList.add(new BasicNameValuePair(stringStringEntry.getKey(), stringStringEntry.getValue()));
				}
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
				httpPost.setEntity(urlEncodedFormEntity);
			}
			// 实体设置
			if (httpEntity != null) {
				httpPost.setEntity(httpEntity);
			}

			// 发起请求
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeConnection(httpClient, httpResponse);
		}
		return false;
	}

	/**
	 * 发起get请求
	 *
	 * @param url
	 * @param header
	 * @param params
	 * @return
	 */
	public static String doGet(String url, Map<String, String> header, Map<String, String> params) {
		String resultStr = "";
		if (url == null || url.isEmpty()) {
			return resultStr;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = HttpClientUtils.getHttpClinet();
			// 请求参数信息
			if (params != null && !params.isEmpty()) {
				url = url + buildUrl(params);
			}
			HttpGet httpGet = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 连接超时
					.setConnectionRequestTimeout(5000)// 请求超时
					.setSocketTimeout(5000).build();// 套接字连接超时
			httpGet.setConfig(requestConfig);
			if (header != null && header.isEmpty()) {
				for (Map.Entry<String, String> stringStringEntry : header.entrySet()) {
					httpGet.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
				}
			}
			// 发起请求
			httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				resultStr = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
			} else {
				StringBuffer stringBuffer = new StringBuffer();
				HeaderIterator headerIterator = httpResponse.headerIterator();
				while (headerIterator.hasNext()) {
					stringBuffer.append("\t" + headerIterator.next());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeConnection(httpClient, httpResponse);
		}
		return resultStr;
	}

	/**
	 * 发起get请求，状态为200时返回true，否则false
	 *
	 * @param url
	 * @param header
	 * @param params
	 * @return
	 */
	public static boolean doGetStatus(String url, Map<String, String> header, Map<String, String> params) {
		if (url == null || url.isEmpty()) {
			return false;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = HttpClientUtils.getHttpClinet();
			// 请求参数信息
			if (params != null && !params.isEmpty()) {
				url = url + buildUrl(params);
			}
			HttpGet httpGet = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 连接超时
					.setConnectionRequestTimeout(5000)// 请求超时
					.setSocketTimeout(5000).build();// 套接字连接超时
			httpGet.setConfig(requestConfig);
			if (header != null && header.isEmpty()) {
				for (Map.Entry<String, String> stringStringEntry : header.entrySet()) {
					httpGet.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
				}
			}
			// 发起请求
			httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeConnection(httpClient, httpResponse);
		}
		return false;
	}

	/**
	 * 关掉连接释放资源
	 */
	private static void closeConnection(CloseableHttpClient httpClient, CloseableHttpResponse httpResponse) {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (httpResponse != null) {
			try {
				httpResponse.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 构造get请求的参数
	 *
	 * @return
	 */
	private static String buildUrl(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer("?");
		for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
			stringBuffer.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue()).append("&");
		}
		String result = stringBuffer.toString();
		if (result != null && !result.isEmpty()) {
			result = result.substring(0, result.length() - 1);// 去掉结尾的&连接符
		}
		return result;
	}
}
