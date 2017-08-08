package com.zf.cms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.zf.cms.vo.BaseOutVo;
import com.zf.cms.vo.DstInfo;
import com.zf.cms.vo.TvgwInVo;
import com.zf.cms.vo.TvgwOutVo;
import com.zf.cms.vo.TvgwOutVo2;
import com.zf.tool.Constants;
import com.zf.utils.FTPUtil;
import com.zf.utils.MyHttpClient;
import com.zf.utils.PropUtil;
import com.zf.utils.StringUtils;

@Service
public class DistributeReplyService {

	Logger logger = LoggerFactory.getLogger(DistributeReplyService.class);

	public static final ScheduledExecutorService tvgwES = Executors.newScheduledThreadPool(100);

	public static final Integer defaultWaitTime = 10;
	
	/**
	 * tcgs接口
	 * @param inputVo
	 * @return
	 */
	public String processTcgs(final String preffix,String inputVo) {
		logger.info("tcgs - inputVo = {}", inputVo);

		String id = "";
		try {
			Document doc = DocumentHelper.parseText(inputVo);
			Element Root = doc.getRootElement();
			id = Root.element("id").getText();
			logger.info(id);
		} catch (DocumentException e) {
			logger.error(e.getMessage(), e);
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<task_ack>");
		sb.append("    <id>").append(id).append("</id>");
		sb.append("    <errcode>0</errcode>");
		sb.append("    <errmsg>success</errmsg>");
		sb.append("    <dir>http://127.0.0.1/tcgs</dir>");
		sb.append("</task_ack>");

		DistributeReplyService.tvgwES.schedule(new Runnable() {

			@Override
			public void run() {
				
				try {			
					String tcgsBackurl = PropUtil.getString(preffix + Constants.TCGS_BACKURL);
					MyHttpClient.post(tcgsBackurl, sb.toString());
					logger.info("tcgs - send ok");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				
			}
		}, PropUtil.getInteger("waitTime")==null?defaultWaitTime: PropUtil.getInteger("waitTime"), TimeUnit.SECONDS);

		return JSON.toJSONString(new BaseOutVo());
	}

	/**
	 * tvgw接口
	 * @param inputVo
	 * @return
	 */
	public String processTvgw(final String preffix,TvgwInVo inputVo) {
		logger.info("接口[tvgw] - inputVo = {}", inputVo);

		final TvgwOutVo outVo = new TvgwOutVo();
		outVo.ContentId = inputVo.ContentId;
		outVo.RequestId = inputVo.RequestId;
		outVo.MsgType = inputVo.MsgType;
		outVo.ProviderId = inputVo.ProviderId;
		outVo.DownloadURL = inputVo.FileURL + "/tvgw";

		DistributeReplyService.tvgwES.schedule(new Runnable() {

			@Override
			public void run() {
				try {
					String tvgwBackurl = PropUtil.getString(preffix + Constants.TVGW_BACKURL);
					MyHttpClient.post(tvgwBackurl, JSON.toJSONString(outVo));
					logger.info("tvgw - send ok");

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, PropUtil.getInteger("waitTime")==null?defaultWaitTime: PropUtil.getInteger("waitTime"), TimeUnit.SECONDS);

		return JSON.toJSONString(new BaseOutVo());
	}

	
	/**
	 * tvgwLive接口
	 * @param inputVo
	 * @return
	 */
	public String processTvgwLive(final String preffix,TvgwInVo inputVo) {
		logger.info("接口[tvgwLive] - inputVo = {}", inputVo);

		final TvgwOutVo outVo = new TvgwOutVo();
		outVo.DownloadURL = inputVo.ChannelInfo.SrcUrlList.get(0) + "/tvgw";//tvgw v1版本
		// tvgw v2接口支持多路输入 码率分离
		outVo.DstInfoList = new ArrayList<DstInfo>();
		DstInfo dst = new DstInfo();
		List<String> bandwidth = new ArrayList<String>();
		bandwidth.add("64000");
		bandwidth.add("256000");
		bandwidth.add("640000");
		bandwidth.add("1024000");
		dst.setDstId(3);// 3 OTT&IPTV
		dst.setDstUrl(inputVo.FileURL + "/main3.m3u8");
		dst.setBandWidth(bandwidth);
		//outVo.DstInfoList.add(dst); 没有iptv&ott域了
		DstInfo dstInfoIptv = new DstInfo();
		dstInfoIptv.setDstId(1);// 3 OTT&IPTV
		dstInfoIptv.setDstUrl(inputVo.FileURL + "/main1.m3u8");
		dstInfoIptv.setBandWidth(bandwidth);
		outVo.DstInfoList.add(dstInfoIptv);
		
		DstInfo dstInfoOtt = new DstInfo();
		dstInfoOtt.setDstId(2);// 3 OTT&IPTV
		dstInfoOtt.setDstUrl(inputVo.FileURL + "/main2.m3u8");
		dstInfoOtt.setBandWidth(bandwidth);
		outVo.DstInfoList.add(dstInfoOtt);
		
		String channelOut = StringUtils.obj2Json(outVo);
		logger.info(">>>>>接口[tvgwLive] - outputVo = {}",channelOut);
		return channelOut;
	}

	/**
	 * vod的drm  tvgw_drm
	 * 
	 * @param inputVo
	 * @return
	 */
	public String processDrm(final String preffix,TvgwInVo inputVo) {
		logger.info("接口[tvgw_drm] - inputVo = {}", inputVo);

		final TvgwOutVo outVo = new TvgwOutVo();
		outVo.ContentId = inputVo.ContentId;
		outVo.RequestId = inputVo.RequestId;
		outVo.MsgType = inputVo.MsgType;
		outVo.ProviderId = inputVo.ProviderId;
		// outVo.DownloadURL = inputVo.FileURL + "/tvgw";
		outVo.DstInfoList = new ArrayList<DstInfo>();
		DstInfo dst = new DstInfo();
		List<String> bandwidth = new ArrayList<String>();
		bandwidth.add("64000");
		bandwidth.add("256000");
		bandwidth.add("640000");
		bandwidth.add("1024000");
		dst.setDstId(3);// 3 OTT&IPTV
		dst.setDstUrl(inputVo.FileURL + "/main.m3u8");
		dst.setBandWidth(bandwidth);
		//outVo.DstInfoList.add(dst); 没有iptv&ott域了
		DstInfo dstInfoIptv = new DstInfo();
		dstInfoIptv.setDstId(1);// 3 OTT&IPTV
		dstInfoIptv.setDstUrl(inputVo.FileURL + "/main1.m3u8");
		dstInfoIptv.setBandWidth(bandwidth);
		outVo.DstInfoList.add(dstInfoIptv);
		
		DstInfo dstInfoOtt = new DstInfo();
		dstInfoOtt.setDstId(2);// 3 OTT&IPTV
		dstInfoOtt.setDstUrl(inputVo.FileURL + "/main2.m3u8");
		dstInfoOtt.setBandWidth(bandwidth);
		outVo.DstInfoList.add(dstInfoOtt);
		boolean isFileUrlCorrected = true;
		String fileUlr = inputVo.FileURL;

		// 是ftp地址则需要效验地址是否正确--fileUlr不能为空
		if (fileUlr.contains("ftp")) {
			Map<String, String> ftpPathMap = FTPUtil.parseFtpUrl(fileUlr,logger);
			if (ftpPathMap != null && (StringUtils.isEmpty(ftpPathMap.get(FTPUtil.FTP_HOST))
					|| StringUtils.isEmpty(ftpPathMap.get(FTPUtil.FTP_PWD))
					|| StringUtils.isEmpty(ftpPathMap.get(FTPUtil.FTP_USERNAME)))) {
				isFileUrlCorrected = false;
			}
		}

		if (!isFileUrlCorrected) {
			outVo.ResultCode = -1;
		}
		DistributeReplyService.tvgwES.schedule(new Runnable() {

			@Override
			public void run() {
				try {
					String tvgwBackurl = PropUtil.getString(preffix + Constants.TVGW_BACKURL);
					MyHttpClient.post(tvgwBackurl, StringUtils.obj2Json(outVo));
					logger.info("drm - send ok");

				} catch (Exception e) 
				{
					logger.error(e.getMessage(), e);
				}
			}
		}, PropUtil.getInteger("waitTime")==null?defaultWaitTime: PropUtil.getInteger("waitTime"), TimeUnit.SECONDS);

		return JSON.toJSONString(new BaseOutVo());
	}
}