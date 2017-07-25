package com.zf.cms.vo;

import java.util.List;

public class TvgwInVo {

	public String ContentId;
	public String ProviderId;
	public String MsgType;
	
	// media
	public Integer RequestId;
	public String FileURL;
	public Integer DrmType;

	
	// channel
	public String DstUrlList;
	public String ChannelType;
	public Integer DestDRMType;
	public ChannelInfoVo ChannelInfo;
	
	//vod drm
	public List<DstInfo> DstInfoList;
	public Integer ContentType;
	public String FileDstURL;
	
	
	public static class ChannelInfoVo {
		public List<String> SrcUrlList;
		public List<String> FileDstURL;
		@Override
		public String toString() {
			return "ChannelInfoVo [SrcUrlList=" + SrcUrlList + ", FileDstURL=" + FileDstURL + "]";
		}
		
		
	}

	@Override
	public String toString() {
		return "TvgwInVo [ContentId=" + ContentId + ", ProviderId=" + ProviderId + ", MsgType=" + MsgType
				+ ", RequestId=" + RequestId + ", FileURL=" + FileURL + ", DrmType=" + DrmType + ", DstUrlList="
				+ DstUrlList + ", ChannelType=" + ChannelType + ", DestDRMType=" + DestDRMType + ", ChannelInfo="
				+ ChannelInfo + "]";
	}

}
