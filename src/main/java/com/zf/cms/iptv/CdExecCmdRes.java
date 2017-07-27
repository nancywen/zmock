//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2017.01.11 时间 12:06:07 PM CST 
//

package com.zf.cms.iptv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class CdExecCmdRes {
	@XmlElement(name = "ResultCode", required = true)
	protected String ResultCode;
	@XmlElement(name = "ErrorDescription", required = true)
	protected String ErrorDescription;

	@XmlTransient
	public String getResultCode() {
		return ResultCode;
	}

	public void setResultCode(String resultCode) {
		ResultCode = resultCode;
	}

	@XmlTransient
	public String getErrorDescription() {
		return ErrorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		ErrorDescription = errorDescription;
	}
}
