package com.zf.cms.vo;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class DstInfo {

	@JsonProperty(value = "DstId")
	private Integer dstId;
	
	@JsonProperty(value = "BandWidths")
	private List<String> bandWidths;
	
	@JsonProperty(value = "DstUrl")
	private String dstUrl;

	public Integer getDstId() {
		return dstId;
	}

	public void setDstId(Integer dstId) {
		this.dstId = dstId;
	}

	public List<String> getBandWidths() {
		return bandWidths;
	}

	public void setBandWidth(List<String> bandWidths) {
		this.bandWidths = bandWidths;
	}

	public String getDstUrl() {
		return dstUrl;
	}

	public void setDstUrl(String dstUrl) {
		this.dstUrl = dstUrl;
	}

}
