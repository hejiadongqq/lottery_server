package com.xtone.lottery.vo;

import java.util.HashMap;
import java.util.Map;

public class Config {

	/**
	 * 现在是第几日
	 */
	private int nowDay = 0;
	
	/**
	 * 每日奖品总数
	 * key为day1，day2，value为奖品数
	 */
	private Map<String, Integer> dayPrize = new HashMap<String, Integer>();
	
	/**
	 * 中奖率
	 */
	private int rate = 75;
	
	private boolean isSwitch = false;
	
	
	public int getNowDay() {
		return nowDay;
	}
	public void setNowDay(int nowDay) {
		this.nowDay = nowDay;
	}
	public Map<String, Integer> getDayPrize() {
		return dayPrize;
	}
	public void setDayPrize(Map<String, Integer> dayPrize) {
		this.dayPrize = dayPrize;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public boolean isSwitch() {
		return isSwitch;
	}
	public void setSwitch(boolean isSwitch) {
		this.isSwitch = isSwitch;
	}
	
	
}
