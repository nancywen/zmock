
package com.zf.cms.iptv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ContentDeployResult {
	@XmlElement(name = "CMSID", required = true)
	protected String CMSID;
	@XmlElement(name = "SOPID", required = true)
	protected String SOPID;
	@XmlElement(name = "CorrelateID", required = true)
	protected String CorrelateID;
	@XmlElement(name = "ResultFileURL", required = true)
	protected String ResultFileURL;
	@XmlElement(name = "ResultCode", required = true)
	protected String ResultCode;

	@XmlTransient
	public String getCMSID() {
		return CMSID;
	}

	public void setCMSID(String cMSID) {
		CMSID = cMSID;
	}

	@XmlTransient
	public String getSOPID() {
		return SOPID;
	}

	public void setSOPID(String sOPID) {
		SOPID = sOPID;
	}

	@XmlTransient
	public String getCorrelateID() {
		return CorrelateID;
	}

	public void setCorrelateID(String correlateID) {
		CorrelateID = correlateID;
	}

	@XmlTransient
	public String getResultFileURL() {
		return ResultFileURL;
	}

	public void setResultFileURL(String resultFileURL) {
		ResultFileURL = resultFileURL;
	}

	@XmlTransient
	public String getResultCode() {
		return ResultCode;
	}

	public void setResultCode(String resultCode) {
		ResultCode = resultCode;
	}
	

}
