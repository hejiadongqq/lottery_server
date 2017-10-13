package com.xtone.lottery.vo;

import java.util.Date;

public class Prize {

	/**
	 * 领奖码
	 */
	private String dateCode;
	
	/**
	 * 是否领取
	 */
	private boolean receive = false;
	
	/**
	 * 领奖时间
	 */
	private Date receiveTime;
	
	/**
	 * 中奖时间
	 */
	private Date prizeTime;
	
	/**
	 * 抽奖日
	 */
	private String dayPrize;

	public String getDateCode() {
		return dateCode;
	}

	public void setDateCode(String dateCode) {
		this.dateCode = dateCode;
	}

	public boolean isReceive() {
		return receive;
	}

	public void setReceive(boolean receive) {
		this.receive = receive;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public Date getPrizeTime() {
		return prizeTime;
	}

	public void setPrizeTime(Date prizeTime) {
		this.prizeTime = prizeTime;
	}

	public String getDayPrize() {
		return dayPrize;
	}

	public void setDayPrize(String dayPrize) {
		this.dayPrize = dayPrize;
	}
	
	
}
