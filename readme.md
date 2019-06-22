# 1、前言
* 两年前在深圳出差开发一个金融项目，在工作过程中刚好用到基于普元的EOS与SOA架构（WebService）的时候在对接一个外部系统招商银行的项目用到WebService,然而现在项目又接触到WebService故整理出来.仅供参考学习.
  
* 其实SOA的架构在工作中遇到少之又少，没必要花太多时间去学习，掌握基本使用或会调用其他系统提供的接口即可。
  
* 此工程主要通过axis2方式去调用天气预报的WebService的接口

# 2、Axis2的几种代码实现
* 工程需要的jar配置
```
<dependency>
	<groupId>org.apache.axis2</groupId>
	<artifactId>axis2-transport-http</artifactId>
	<version>1.7.7</version>
</dependency>
<dependency>
	<groupId>org.apache.axis2</groupId>
	<artifactId>axis2-transport-local</artifactId>
	<version>1.7.7</version>
</dependency>
<dependency>
	<groupId>org.apache.axis2</groupId>
	<artifactId>axis2-adb</artifactId>
	<version>1.7.7</version>
</dependency>
<dependency>
	<groupId>org.jsoup</groupId>
	<artifactId>jsoup</artifactId>
	<version>1.8.1</version>
</dependency>
```

* AxisUtils工具类 
```
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
 * 
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

```
* 代码实现,这里使用Jsoup进行解析xml的内容数据，对Jsoup可以通过自身去学习或者推荐使用dom4j
* 也可以参考我之前写的代码![深入理解Jsoup解析器API与实际运用](https://www.jianshu.com/p/e036ba0b3acc)
```
package com.flong.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WeatherAxis2 {

	public static void main(String[] args) throws Exception {
		test1();
		test2();
	}

	//wsdl文件
	public static void test1()throws Exception{
		String url = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl";
		String namespaceURI = "http://WebXml.com.cn/";
		getSupportProvince(url, namespaceURI);
		//getWeatherByCityName(url, namespaceURI, "广州");
	} 
	
	/**
	 * @Description 这种处理方法不需要wsdl，只需要请求路径然后指定前缀目标命名空间路径，方法发生参数名称和参数值就可以读取数据
	 */
	public static void test2()throws Exception{
		String url = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx";
		String namespaceURI = "http://WebXml.com.cn/";
		String methodName = "getWeatherbyCityName";
		OMElement element = AxisUtils.buildElement(namespaceURI,"tns",methodName,new String[]{"theCityName"},new String[]{"广州"});
		String result = AxisUtils.sendReceive( url,namespaceURI,methodName, element);
		printElement(result,"getWeatherbyCityNameResult");
	}
	
	
	// 省
	public static void getSupportProvince(String url, String namespaceURI) throws AxisFault {
		String retResult = AxisUtils.callReferenceWebService(url, namespaceURI, "getSupportProvince", "","");
		printElement(retResult,"getSupportProvinceResult");
	}
 
	
	// 查询广州天气
	public static void getWeatherByCityName(String url, String namespaceURI, String cityName) throws AxisFault {
		String retResult = AxisUtils.callReferenceWebService(url, namespaceURI, "getWeatherbyCityName", "theCityName",
				cityName);
		printElement(retResult,"getWeatherbyCityNameResult");
	}
	
	//使用Jsoup进行解析xml的内容数据
	private static void printElement(String retResult,String methodName ){
		
		if (retResult != null && !retResult.equals("")) {
			
			Document parse = Jsoup.parse(retResult);
			Elements rootNode = parse.select(methodName);
			
			//使用jdk1.8新特性的forEach循环输出结果
			rootNode.forEach(obj ->{
				Elements stringNodes = obj.select("string");
				stringNodes.forEach(o -> {
					//输出内容
					System.out.println(o.text());
					
				});
			});
		}
	}
}
```


# 3、推荐学习参文章
* https://blog.csdn.net/a1000005a/article/details/4770052
* https://blog.csdn.net/wangyu2016/article/details/76022928
* http://blog.csdn.net/xiang520jl/article/details/15504175

> ![Axis2与Struts2+Spring的整合](https://github.com/jilongliang/JL_NAPP)
 