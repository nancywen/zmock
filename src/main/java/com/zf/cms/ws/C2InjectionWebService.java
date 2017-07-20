package com.zf.cms.ws;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zf.cms.reply.CSPResult;
import com.zf.tool.ErrorCode;
import com.zf.utils.C2SoapUtil;

/**
 * C2注入接口
 * 
 * @author Faker
 *
 */
@Service
@WebService(targetNamespace = "iptv", serviceName = "c2Injection")
@SOAPBinding(parameterStyle = ParameterStyle.WRAPPED)
@Component
public class C2InjectionWebService {
	
	private static final Logger log = LoggerFactory.getLogger(C2InjectionWebService.class);
	
	public static final ScheduledExecutorService c2ES = Executors.newScheduledThreadPool(100);
	
	public static final Integer c2WaitTime = 10;

	/**
	 * 接收C2接口消息
	 * 
	 * @param cspID
	 * @param lspID
	 * @param correlateID
	 * @param cmdFileURL
	 * @return
	 */
	@WebMethod(operationName = "ExecCmd")
	@WebResult(name = "ExecCmdReturn")
	public CSPResult ExecCmdRequest(@WebParam(name = "CSPID") final String cspId,
			@WebParam(name = "LSPID") final String lspId,
			@WebParam(name = "CorrelateID") final String correlateId,
			@WebParam(name = "CmdFileURL") String cmdFileURL) {

		log.info(String
				.format("received message from CSP: CSPID={}, LSPID={}, CorrelateID={}, CmdFileURL={} ",
						cspId, lspId, correlateId, cmdFileURL));
		CSPResult rsp = new CSPResult();
		rsp.setResult(ErrorCode.SUCCESS);
		rsp.setErrorDescription("ok");
		if (cspId == null || cspId.isEmpty() || lspId == null
				|| lspId.isEmpty() || correlateId == null
				|| correlateId.isEmpty() || cmdFileURL == null
				|| cmdFileURL.isEmpty()) {
			log.info(String.format("response return: ErrorCode.FAIL=#0.",
					ErrorCode.FAIL));

			rsp.setResult(ErrorCode.FAIL);
			rsp.setErrorDescription("Parameter is null!");
			return rsp;
		}
		
		// 延迟异步反馈
		c2ES.schedule(new Runnable() {

			@Override
			public void run() {
				
				try {				
					//xmlURL, correlateID, copId, cmdResult, lspId
					C2SoapUtil.sendMessage("",correlateId, cspId, ErrorCode.SUCCESS, lspId);
					log.info("c2 reply - send ok");
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
			}
		}, c2WaitTime, TimeUnit.SECONDS);

		return rsp;
	}
}
