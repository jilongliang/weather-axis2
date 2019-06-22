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

	//wsdl�ļ�
	public static void test1()throws Exception{
		String url = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl";
		String namespaceURI = "http://WebXml.com.cn/";
		getSupportProvince(url, namespaceURI);
		//getWeatherByCityName(url, namespaceURI, "����");
	} 
	
	/**
	 * @Description ���ִ���������Ҫwsdl��ֻ��Ҫ����·��Ȼ��ָ��ǰ׺Ŀ�������ռ�·�������������������ƺͲ���ֵ�Ϳ��Զ�ȡ����
	 */
	public static void test2()throws Exception{
		String url = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx";
		String namespaceURI = "http://WebXml.com.cn/";
		String methodName = "getWeatherbyCityName";
		OMElement element = AxisUtils.buildElement(namespaceURI,"tns",methodName,new String[]{"theCityName"},new String[]{"����"});
		String result = AxisUtils.sendReceive( url,namespaceURI,methodName, element);
		printElement(result,"getWeatherbyCityNameResult");
	}
	
	
	// ʡ
	public static void getSupportProvince(String url, String namespaceURI) throws AxisFault {
		String retResult = AxisUtils.callReferenceWebService(url, namespaceURI, "getSupportProvince", "","");
		printElement(retResult,"getSupportProvinceResult");
	}
 
	
	// ��ѯ��������
	public static void getWeatherByCityName(String url, String namespaceURI, String cityName) throws AxisFault {
		String retResult = AxisUtils.callReferenceWebService(url, namespaceURI, "getWeatherbyCityName", "theCityName",
				cityName);
		printElement(retResult,"getWeatherbyCityNameResult");
	}
	
	//ʹ��Jsoup���н���xml����������
	private static void printElement(String retResult,String methodName ){
		
		if (retResult != null && !retResult.equals("")) {
			
			Document parse = Jsoup.parse(retResult);
			Elements rootNode = parse.select(methodName);
			rootNode.forEach(obj ->{
				Elements stringNodes = obj.select("string");
				stringNodes.forEach(o -> {
					//�������
					System.out.println(o.text());
					
				});
			});
		}
		
	}
}