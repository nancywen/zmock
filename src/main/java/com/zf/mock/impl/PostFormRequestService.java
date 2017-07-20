package com.zf.mock.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.zf.mock.AbstractRequestService;
import com.zf.mock.enums.RequestType;
import com.zf.utils.CommonUtils;

@Service
public class PostFormRequestService extends AbstractRequestService {

	@Override
	public RequestType getRequestType() {
		return RequestType.POSTFORM;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> boolean checkRequest(T requestInfo, String requestParamTemplate) {
		Map<String, String> reqInfo = (Map<String, String>) requestInfo;
		String[] temps = requestParamTemplate.split("\\&");
		for (String temp : temps) {
			String[] tempArr = temp.split("\\=");
			if (!reqInfo.containsKey(tempArr[0])) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> String getParamValueByKey(T requestInfo, String key) {
		// 判断key 是否是/开头
		if (key.startsWith("/")) {
			key = key.substring(1, key.length());
		}
		Map<String, String> reqInfo = (Map<String, String>) requestInfo;
		if (reqInfo.containsKey(key)) {
			return reqInfo.get(key);
		}
		return key;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRequestInfo(HttpServletRequest request) throws Exception {
		return (T) CommonUtils.getRequestParams(request);
	}

}
