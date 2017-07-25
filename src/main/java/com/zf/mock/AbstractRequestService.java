package com.zf.mock;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import com.zf.cms.DistributeReplyService;
import com.zf.cms.vo.TvgwInVo;
import com.zf.dao.domain.MockInfoDao;
import com.zf.dao.domain.MockInfoDao.MockConditionInfo;
import com.zf.operators.OperatorHandlerFactory.UnequalHandler;
import com.zf.service.ConvertExpressionService;
import com.zf.service.MockInfoService;
import com.zf.tool.Constants;
import com.zf.utils.ResponseUtil;

public abstract class AbstractRequestService implements IRequestService{
	
	@Autowired
	private MockInfoService mockInfoService;
	
	@Autowired
	private DistributeReplyService distributeReplyService;
	
	public abstract <T> boolean  checkRequest(T requestInfo, String requestParamTemplate);
	
	public abstract <T> String getParamValueByKey(T requestInfo, String key);
	
	public abstract <T> T getRequestInfo(HttpServletRequest request) throws Exception;
	
	public <T> String process(String path, HttpServletRequest request) throws Exception {
		String[] uriarray = path.split("/");
		int urilength = uriarray.length;
		T requestInfo = this.getRequestInfo(request); 
		MockInfoDao mockInfo = mockInfoService.getMockInfoDao(path, request.getMethod());
		if(mockInfo==null){
			return JSON.toJSONString(ResponseUtil.getFailedResponse(ResponseUtil.ResponseConstants.NOMOCKINFO.getRetCode(), ResponseUtil.ResponseConstants.NOMOCKINFO.getRetMsg()));
		}
		if(!this.checkRequest(requestInfo, mockInfo.getRequestParamTemplate())){
			return JSON.toJSONString(ResponseUtil.getFailedResponse("301", "请求参数不正确!"));
		}
		List<MockConditionInfo> conditions = mockInfo.getResponseCondition();
		if(conditions==null){
			return JSON.toJSONString(ResponseUtil.getFailedResponse(ResponseUtil.ResponseConstants.NORESULT.getRetCode(), ResponseUtil.ResponseConstants.NORESULT.getRetMsg()));
		}
		for (MockConditionInfo mockConditionInfo : conditions) {
			String condition = mockConditionInfo.getResCondition();
			ConvertExpressionService<T> convert = new ConvertExpressionService<T>(requestInfo, this);
			String result = convert.convertExpression(condition);
			if(Boolean.valueOf(result)){
				
				// 这里改造 根据请求地址判断是否需要异步响应
				// tcgs tvgw uri须大于2
				if(urilength >= 2){
					String urisuffix = uriarray[urilength-1];
					String uripreffix = uriarray[urilength-2];
					if(urisuffix.equals(Constants.TCGS_ADDR)){
						return distributeReplyService.processTcgs(uripreffix,requestInfo.toString());
					}else if(urisuffix.equals(Constants.TVGW_ADDR)){
						return distributeReplyService.processTvgw(uripreffix, JSON.parseObject(requestInfo.toString(),TvgwInVo.class));
					}else if(urisuffix.equals(Constants.TVGW_LIVE_ADDR)){
						return distributeReplyService.processTvgwLive(uripreffix, JSON.parseObject(requestInfo.toString(),TvgwInVo.class));
					}else if(urisuffix.endsWith(Constants.TVGW_DRM_ADDR)){
						return distributeReplyService.processDrm(uripreffix, JSON.parseObject(requestInfo.toString(),TvgwInVo.class));
					}
				}
				
				return mockConditionInfo.getResValue();
			}
		}
		return JSON.toJSONString(ResponseUtil.getFailedResponse(ResponseUtil.ResponseConstants.NORESULT.getRetCode(), ResponseUtil.ResponseConstants.NORESULT.getRetMsg()));
	}
	
}
