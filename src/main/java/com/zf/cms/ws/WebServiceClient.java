/*package com.zf.cms.ws;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.zf.cms.iptv.ExecCmd;


*//**
 * test
 * @author makefu
 * @date 2016年5月10日
 *
 *//*
public class WebServiceClient {
	
	private static final Logger logger = Logger.getLogger(WebServiceClient.class);

	private static final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

	// 执行指令请求 地址
	private static final String EXEC_CMD_REQ_URI = "http://localhost:8500";

	// send to an explicit URI
	static void customSendAndReceive() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ExecCmd.class);

		Marshaller marshaller = context.createMarshaller();

		ExecCmd request = new ExecCmd();
		request.setLSPID("1");
		request.setCSPID("2");
		request.setCorrelateID("3");
		request.setCmdFileURL("4");
		
		StringWriter sw = new StringWriter();
		marshaller.marshal(request, sw);

		StreamSource source = new StreamSource(new StringReader(sw.toString()));
		sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		webServiceTemplate.sendSourceAndReceiveToResult(EXEC_CMD_REQ_URI, source, result);
		
		logger.info("result>>>\n" + sw.toString());
	}

	public static String customSendAndReceive(String uri, String xml) throws JAXBException {
		StreamSource source = new StreamSource(new StringReader(xml));
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		webServiceTemplate.sendSourceAndReceiveToResult(uri, source, result);
		return sw.toString();
	}

	public static <T> String customSendAndReceive(String uri, T t) throws JAXBException {
		logger.info("send>>>\n" + uri);
		JAXBContext context = JAXBContext.newInstance(t.getClass());
		Marshaller marshaller = context.createMarshaller();

		StringWriter sw = new StringWriter();
		marshaller.marshal(t, sw);

		StreamSource source = new StreamSource(new StringReader(sw.toString()));
		sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		webServiceTemplate.sendSourceAndReceiveToResult(uri, source, result);
		
		logger.info("result>>>\n" + sw);
		return sw.toString();
	}

}
*/