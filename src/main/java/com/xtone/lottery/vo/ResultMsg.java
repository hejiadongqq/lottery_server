package com.xtone.lottery.vo;

public class ResultMsg {

	private String code;
	private String msg;
	private Object data;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	public static ResultMsg ok(Object data) {
		ResultMsg rm = new ResultMsg();
        rm.setCode("00");
        rm.setMsg("成功");
        rm.setData(data);
        return rm;
    }
	
	public static ResultMsg fall(String msg) {
		ResultMsg rm = new ResultMsg();
        rm.setCode("01");
        rm.setMsg(msg);
        rm.setData("");
        return rm;
    }
	
}
