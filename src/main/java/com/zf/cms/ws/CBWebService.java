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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.zf.cms.iptv.CdExecCmdRes;
import com.zf.cms.iptv.ContentDeployResult;
import com.zf.tool.ErrorCode;
import com.zf.utils.PropUtil;
import com.zf.utils.SoapUtil;
import com.zf.utils.StringUtils;


@Service
@WebService(targetNamespace = "iptv", serviceName = "ContentServiceMng")
@SOAPBinding(parameterStyle = ParameterStyle.WRAPPED)
@Component
public class CBWebService {
	
	private static final Logger logger = LoggerFactory.getLogger(CBWebService.class);
	
	
	public static final ScheduledExecutorService cbES = Executors.newScheduledThreadPool(100);
	
	private static final String NAMESPACE_URI = "iptv";
	
	Integer c2WaitTime = 3;
	
	Integer cdWaitTime = 3;

	
	/**
	 * cd
	 * @param cspId
	 * @param lspId
	 * @param correlateId
	 * @param cmdFileURL
	 * @return
	 */
	@WebMethod(operationName = "ContentServiceMngReq")
	@WebResult(name = "CdExecCmdRes")
	public CdExecCmdRes contentDeployReq(@WebParam(name = "CMSID") final String cspId,
			@WebParam(name = "SOPID") final String lspId, @WebParam(name = "CorrelateID") final String correlateId,
			@WebParam(name = "ContentMngXMLURL") String cmdFileURL){
		
		logger.info(String.format("received message from CSP: CSPID={}, LSPID={}, CorrelateID={}, CmdFileURL={} ", cspId,
				lspId, correlateId, cmdFileURL));
		CdExecCmdRes response = new CdExecCmdRes();
		response.setErrorDescription("ok");
		
		response.setResultCode("0");
		if (StringUtils.isEmpty(cspId) || StringUtils.isEmpty(lspId) || StringUtils.isEmpty(correlateId)
				|| StringUtils.isEmpty(cmdFileURL)) {
			logger.info(String.format("response return: ErrorCode.FAIL=#0.", ErrorCode.FAIL));

			response.setResultCode(""+ErrorCode.FAIL);
			response.setErrorDescription("Parameter is null!");
		}
		
		CBWebService.cbES.schedule(new Runnable() {
			public void run() {
				try {
					SoapUtil.sendCBMessage("", correlateId, cspId, ErrorCode.SUCCESS, lspId);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, PropUtil.getInteger("waitTime") == null ? cdWaitTime : PropUtil.getInteger("waitTime"), TimeUnit.SECONDS);
		
		logger.info("response = " + JSON.toJSONString(response));
		
		return response;
	}
}
	