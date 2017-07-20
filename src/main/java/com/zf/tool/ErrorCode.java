package com.zf.tool;

/**
 * 异常/错误码常量类
 * @author ding
 * 20140630
 */
public class ErrorCode {
	public static final int SUCCESS = 0;
	public static final int FAIL = -1;
	public static final int ERR_USER_NOT_EXIST=-2;//效验用户名--用户不存在
	public static final int ERR_USER_DISABLE = -3;//用户被禁用
	public static final int ERR_NO_CONTENTID = -4;//用户被禁用
	public static final int ERR_CAPTCHA_ERROR = -5;// 验证码错误
	public static final int ERR_IPADDR_INVALID = -6;// ip错误
	
	public static final int UNKNOWEXCEPTION= -10;
	
	public static final int ERR_GENERIC_INVALID_PARAM = 100;	//参数不可用
	public static final int ERR_NULL_PARAM = 101;				//参数为空
	public static final int ERR_FORMAT_PARAM = 102;				//参数格式错误
	
	public static final int ERR_NULL_CP = 110;					//无法获取到cp
	public static final int ERR_NULL_SP = 111;					//无法获取到sp
	public static final int ERR_NO_CPCONTENTID = 112;			//无法获取到cpContentId
	
	public static final int ERR_DUPLICATE_ENTITY = 200;			//实体重复
	
	public static final int ERR_ENTITY_REFERENCES = 300;		//实体被引用
	public static final int ERR_ENTITY_NOTFOUND = 301;			//实体不存在
	
	public static final int ERR_PASSWORD_ERROR = 400;			//密码输入错误
	public static final int ERR_PASSWORD_INCONSISTENT = 401;	//两次密码输入不一致
	
	public static final int ERR_UITEMPLATE_IS_ASSOCIATE = -3;
	public static final int ERR_UITEMPLATE_NAME_EXIST = -4;     //ui模板名存在
	public static final int ERR_UITEMPLATE_NAME_ERROR = -5;      //得到ui模板名错误
	
	public static final int ERR_RELATION_CHANNEL =-2;//删除失败，movie关联了channel
	
	
	public static final int SUCCESS_BLS = 1000;
	public static final int FAIL_BLS = 3005;
	
	
}
