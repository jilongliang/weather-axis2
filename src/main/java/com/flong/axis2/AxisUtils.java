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
 * @Description AxisUtils������
 * @ClassName AxisUtils
 * @Date 2019��6��22�� ����8:30:14
 * @Author liangjl
 * @Copyright (c) All Rights Reserved, 2019.
 */
public class AxisUtils {

	private static final String PROXY_IP  	= "61.147.124.120";
	private static final Integer PROXY_PORT = 8080;
	
	/***
	 * @Description ����webservice,���ַ�ʽ�ʺ����÷�ʽ���á�
	 * @Author liangjl
	 * @Date 2019��6��22�� ����8:28:42
	 * @param url  ��������
	 * @param namespaceUri������������
	 * @param methodName��������
	 * @param paramName   �����������
	 * @param paramValue �������ֵ
	 * @throws AxisFault  ����
	 * @return void ��������
	 */
	public static String callReferenceWebService(String url, String namespaceUri, String methodName, String paramName,
			String paramValue) throws AxisFault {
		String retResult = "";
		try {
			Options options = new Options();
			EndpointReference targetEPR = new EndpointReference(url);
			options.setTo(targetEPR);
			// ��Ҫ�����������
			options.setAction(namespaceUri + methodName);
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace(namespaceUri, "");
			OMElement method = fac.createOMElement(methodName, omNs);
			//�ж�����������ƺͲ���ֵ��Ϊ�յ�ʱ���ִ������Ĵ�����
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
	 * @Description ����webservice,���ַ�ʽ�ʺ�RPC�������.
	 * @Author liangjl
	 * @Date 2017��1��5�� ����4:44:43
	 * @param url  ��������
	 * @param namespaceUri������������
	 * @param methodName��������
	 * @param paramName �����������
	 * @param paramValue �������ֵ
	 * @return String ��������
	 */
	public static String callRpcWebService(String url, String namespaceUri, String methodName, String paramName,
			String paramValue) {
		String result = "";
		try {
			RPCServiceClient client = new RPCServiceClient(); // ����RPCServiceClient
			Options options = client.getOptions(); // ����Options
			EndpointReference end = new EndpointReference(url);
			options.setTo(end); // ������������
			options.setProperty(paramName, String.class);
			// options.setAction(namespaceUri + methodName);
			options.setTimeOutInMilliSeconds(30000);
			// �����������
			Object[] obj = new Object[] { paramValue };
			// ����Class
			Class<?>[] classes = new Class[] { String.class };
			// ���������ռ�Ͳ�������
			QName qname = new QName(namespaceUri, methodName);
			// ����Զ�̵�webservice
			Object[] invokeObjs = client.invokeBlocking(qname, obj, classes);
			if (invokeObjs != null && invokeObjs.length > 0) {
				result = (String) invokeObjs[0];// ��ȡ���ص���Ϣ����
			}
			return result;
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @Description �M�bԪ��
	 * @Author		liangjl
	 * @Date		2019��6��22�� ����8:44:39
	 * @param namespaceUri  Ŀ��������ַ朽� �磺targetNamespace="http://WebXml.com.cn/"
	 * @param prefix Ŀ��������ַ��ǰ�Y ��type="tns:ArrayOfString"��tns��ǰ�Y
	 * @param method ��service�ķ������Q
	 * @param args �������Q
	 * @param vals ����ֵ
	 * @return OMElement �������� 
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
	 * @Description ������������
	 * @Author		liangjl
	 * @Date		2019��6��22�� ����8:51:49
	 * @param url Ո���ַ
	 * @param namespaceUri Ŀ�˿��g���Q��ַ
	 * @param methodName Ո�󷽷�
	 */
	public static Options buildOptions(String url ,String namespaceUri,String methodName){
		EndpointReference targetEPR = new EndpointReference(url);
		Options options = new Options();
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		options.setAction(namespaceUri + methodName);
		options.setTo(targetEPR);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(HTTPConstants.CHUNKED, "false");//���ò�������.
		//���п��Բ������ã�Ҫ����Ҫ��һ���ܴ�����ip��ַ������ᳬʱ.���鲻ʹ�����ִ���.
		//options.setProperty(HTTPConstants.PROXY, buildProxy());
		options.setProperty(Constants.Configuration.HTTP_METHOD,HTTPConstants.HTTP_METHOD_POST);
		return options;

	}
	 
	
	/**
	 * @Description ���Ͳ���������
	 * @Author		liangjl
	 * @Date		2019��6��22�� ����9:54:06
	 * @param url �����ַ
	 * @param namespaceURI namespaceUri  Ŀ��������ַ朽� �磺targetNamespace="http://WebXml.com.cn/"
	 * @param methodName ���󷽷�
	 * @param buildElement ��װ�Ĳ���
	 * @return String �������� 
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
	 * @Description ����
	 * @Author		liangjl
	 * @Date		2019��6��22�� ����9:50:03
	 * @@return ����
	 * @return ProxyProperties �������� 
	 * @throws
	 */
	public static ProxyProperties buildProxy() {
		ProxyProperties proxyProperties = new ProxyProperties();
		proxyProperties.setProxyName(PROXY_IP);
		proxyProperties.setProxyPort(PROXY_PORT);
		return proxyProperties;
	}
	

}