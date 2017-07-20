package com.zf.utils;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zf.tool.Constants;

public class C2SoapUtil {
	
	private static final Logger log = Logger.getLogger(C2SoapUtil.class);
	@Autowired
	public static boolean sendMessage(String xmlURL, String correlateID, String copId, int cmdResult, String lspId) {
		// 增加支持多个回馈路径，以copId为前缀 ---需配置c2反馈地址
		String sendMsg2CSPURL = PropUtil.getString(Constants.C2_REPLYURL);//injectionTaskService.findReplyUrl(copId, InjectionConstants.Protocol_C2);
		
		if (sendMsg2CSPURL == null || "".equals(sendMsg2CSPURL.trim())) {
			log.error("sendMsg2CSPURL is null or empty");
			return false;
		}

		try {
			// 创建连接
			// ==================================================
			SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = soapConnFactory.createConnection();

			// 创建消息对象
			// ===========================================
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");

			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader("SOAPAction", "");

			// 创建soap消息主体==========================================
			SOAPPart soapPart = message.getSOAPPart();// 创建soap部分
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPBody body = envelope.getBody();

			// =====================================
			String operation = "ResultNotify";
			log.info("c2OperationName:" + operation);

			SOAPElement bodyElement = body.addChildElement(envelope.createName(operation, "iptv", "iptv"));
			bodyElement.addChildElement("CSPID").addTextNode(copId);
			bodyElement.addChildElement("LSPID").addTextNode(lspId);
			bodyElement.addChildElement("CorrelateID").addTextNode(correlateID);
			bodyElement.addChildElement("CmdResult").addTextNode(cmdResult + "");
			bodyElement.addChildElement("ResultFileURL").addTextNode(xmlURL);

			// Save the message
			message.saveChanges();
			// 打印客户端发出的soap报文，做验证测试
			log.info("send message to CSP:CSPID=" + copId + ",LSPID=" + lspId + ",CorrelateID=" + correlateID
					+ ",CmdResult=" + cmdResult + ",ResultFileURL=" + xmlURL);

			/*
			 * 实际的消息是使用 call()方法发送的，该方法接收消息本身和目的地作为参数，并返回第二个 SOAPMessage 作为响应。
			 * call方法的message对象为发送的soap报文，url为mule配置的inbound端口地址。
			 */
			URL url = Util.getUrl(sendMsg2CSPURL);
			log.info("URL is:" + url);

			// 响应消息
			// ===========================================================================
			SOAPMessage reply = connection.call(message, url);
			// reply.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "gb2312");

			Source source = reply.getSOAPPart().getContent();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream myOutStr = new ByteArrayOutputStream();
			StreamResult res = new StreamResult();
			res.setOutputStream(myOutStr);
			transformer.transform(source, res);
			String result = myOutStr.toString("UTF-8");
			// 打印服务端返回的soap报文供测试
			log.info("recieve message from CSP,:CSPID=" + copId + ",LSPID=" + lspId + ",CorrelateID=" + correlateID
					+ ",result" + result);
			// Close the connection 关闭连接 ==============
			connection.close();

			// 将相应的消息转换为doc对象
			// Document doc = reply.getSOAPPart().getEnvelope().getBody()
			// .extractContentAsDocument();
			// String resultMsg = doc.getElementsByTagName("ErrorDescription")
			// .item(0).getTextContent();

			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
}
