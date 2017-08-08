package com.zf.cms.vo;

import java.util.List;

public class TvgwOutVo extends BaseOutVo{

	public String MsgType;
	public String ContentId;
	public Integer RequestId;
	public String ProviderId;
	public String DownloadURL;
	public String DownloadUrl;//tvgw v1版本
	public List<DstInfo> DstInfoList;
	
	@Override
	public String toString() {
		return "TvgwOutVo [ResultCode=" + ResultCode + ", MsgType=" + MsgType + ", ContentId=" + ContentId
				+ ", RequestId=" + RequestId + ", ProviderId=" + ProviderId + ", DownloadURL=" + DownloadURL + "]";
	}

	
	
}
