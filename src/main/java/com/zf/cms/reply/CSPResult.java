package com.zf.cms.reply;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class CSPResult {
	@XmlElement(name = "Result", required = true)
	private int Result;
	@XmlElement(name = "ErrorDescription", required = true)
	private String ErrorDescription;

	@XmlTransient
	public int getResult() {
		return Result;
	}

	public void setResult(int result) {
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
