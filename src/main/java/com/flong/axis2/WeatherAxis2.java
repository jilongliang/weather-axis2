package com.flong.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WeatherAxis2 {

	public static void main(String[] args) throws Exception {
		test1();
		//test2();
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