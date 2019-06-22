package com.flong.axis2;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;

/***
 * @Description AxisUtils工具类
 * @ClassName AxisUtils
 * @Date 2019年6月22日 上午8:30:14
 * @Author liangjl
 * @Copyright (c) All Rights Reserved, 2019.
 */
public class AxisUtils {

	private static final String PROXY_IP  	= "61.147.124.120";
	private static final Integer PROXY_PORT = 8080;
	
	/***
	 * @Description 调用webservice,这种方式适合引用方式调用。
	 * @Author liangjl
	 * @Date 2019年6月22日 上午8:28:42
	 * @param url  请求连接
	 * @param namespaceUri请求命名连接
	 * @param methodName操作方法
	 * @param paramName   请求参数名称
	 * @param paramValue 请求参数值
	 * @throws AxisFault  参数
	 * @return void 返回类型
	 */
	public static String callReferenceWebService(String url, String namespaceUri, String methodName, String paramName,
			String paramValue) throws AxisFault {
		String retResult = "";
		try {
			Options options = new Options();
			EndpointReference targetEPR = new EndpointReference(url);
			options.setTo(targetEPR);
			// 需要加上这条语句
			options.setAction(namespaceUri + methodName);
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace(namespaceUri, "");
			OMElement method = fac.createOMElement(methodName, omNs);
			//判断请求参数名称和参数值不为空的时候才执行下面的处理。
			if (paramName != null && !paramName.equals("") && paramValue != null && !paramValue.equals("")) {
				OMElement symbol = fac.createOMElement(paramName, omNs);
				symbol.addChild(fac.createOMText(symbol, paramValue));
				method.addChild(symbol);
			}
			method.build();
			OMElement result = sender.sendReceive(method);
			if(result!= null){
				return result.toString();
			}
		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		}
		return retResult;
	}

	/**
	 * @Description 调用webservice,这种方式适合RPC服务调用.
	 * @Author liangjl
	 * @Date 2017年1月5日 下午4:44:43
	 * @param url  请求连接
	 * @param namespaceUri请求命名连接
	 * @param methodName操作方法
	 * @param paramName 请求参数名称
	 * @param paramValue 请求参数值
	 * @return String 返回类型
	 */
	public static String callRpcWebService(String url, String namespaceUri, String methodName, String paramName,
			String paramValue) {
		String result = "";
		try {
			RPCServiceClient client = new RPCServiceClient(); // 创建RPCServiceClient
			Options options = client.getOptions(); // 创建Options
			EndpointReference end = new EndpointReference(url);
			options.setTo(end); // 发送请求连接
			options.setProperty(paramName, String.class);
			// options.setAction(namespaceUri + methodName);
			options.setTimeOutInMilliSeconds(30000);
			// 设置请求参数
			Object[] obj = new Object[] { paramValue };
			// 创建Class
			Class<?>[] classes = new Class[] { String.class };
			// 设置命名空间和操作方法
			QName qname = new QName(namespaceUri, methodName);
			// 调用远程的webservice
			Object[] invokeObjs = client.invokeBlocking(qname, obj, classes);
			if (invokeObjs != null && invokeObjs.length > 0) {
				result = (String) invokeObjs[0];// 获取返回的信息参数
			}
			return result;
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @Description 組裝元素
	 * @Author		liangjl
	 * @Date		2019年6月22日 上午8:44:39
	 * @param namespaceUri  目標命名地址鏈接 如：targetNamespace="http://WebXml.com.cn/"
	 * @param prefix 目標命名地址的前綴 如type="tns:ArrayOfString"的tns的前綴
	 * @param method 問service的方法名稱
	 * @param args 參數名稱
	 * @param vals 參數值
	 * @return OMElement 返回类型 
	 */
	public static OMElement buildElement(String namespaceUri, String prefix, String method, String[] args,
			String[] vals) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(namespaceUri, prefix);
		OMElement data = fac.createOMElement(method, omNs);

		for (int i = 0; i < args.length; i++) {
			OMElement inner = fac.createOMElement(args[i], omNs);
			inner.setText(vals[i]);
			data.addChild(inner);
		}
		return data;
	}
	
	/***
	 * @Description 創建操作對象
	 * @Author		liangjl
	 * @Date		2019年6月22日 上午8:51:49
	 * @param url 請求地址
	 * @param namespaceUri 目標空間名稱地址
	 * @param methodName 請求方法
	 */
	public static Options buildOptions(String url ,String namespaceUri,String methodName){
		EndpointReference targetEPR = new EndpointReference(url);
		Options options = new Options();
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		options.setAction(namespaceUri + methodName);
		options.setTo(targetEPR);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(HTTPConstants.CHUNKED, "false");//设置不受限制.
		//这行可以不用设置，要设置要找一个能代理的ip地址，否则会超时.建议不使用这种处理.
		//options.setProperty(HTTPConstants.PROXY, buildProxy());
		options.setProperty(Constants.Configuration.HTTP_METHOD,HTTPConstants.HTTP_METHOD_POST);
		return options;

	}
	 
	
	/**
	 * @Description 发送并接收数据
	 * @Author		liangjl
	 * @Date		2019年6月22日 上午9:54:06
	 * @param url 请求地址
	 * @param namespaceURI namespaceUri  目標命名地址鏈接 如：targetNamespace="http://WebXml.com.cn/"
	 * @param methodName 请求方法
	 * @param buildElement 组装的参数
	 * @return String 返回类型 
	 */
	public static String sendReceive(String url ,String namespaceURI,String methodName,OMElement buildElement){
		String retMsg = "";
		try {
			ServiceClient sender = new ServiceClient();
			sender.setOptions(AxisUtils.buildOptions( url,namespaceURI,methodName));
		  OMElement ret = sender.sendReceive(buildElement);
		  if(ret!=null ){
			  return ret.toString();
		  }
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		return retMsg;
		
	}
	/**
	 * @Description 代理
	 * @Author		liangjl
	 * @Date		2019年6月22日 上午9:50:03
	 * @@return 参数
	 * @return ProxyProperties 返回类型 
	 * @throws
	 */
	public static ProxyProperties buildProxy() {
		ProxyProperties proxyProperties = new ProxyProperties();
		proxyProperties.setProxyName(PROXY_IP);
		proxyProperties.setProxyPort(PROXY_PORT);
		return proxyProperties;
	}
	

}
