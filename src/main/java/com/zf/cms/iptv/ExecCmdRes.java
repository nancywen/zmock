
package com.zf.cms.iptv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ExecCmdRes {
	@XmlElement(name = "Result", required = true)
	protected String Result;
	@XmlElement(name = "ErrorDescription", required = true)
	protected String ErrorDescription;

	@XmlTransient
	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	@XmlTransient
	public String getErrorDescription() {
		return ErrorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		ErrorDescription = errorDescription;
	}

}
