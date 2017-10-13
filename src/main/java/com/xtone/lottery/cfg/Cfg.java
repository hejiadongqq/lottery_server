package com.xtone.lottery.cfg;

public class Cfg {

	public static String RedisType = "lottery";
	
	/**
	 * 已抽过奖的用户
	 */
	public static String RedisAlready = RedisType+":already";
	
	/**
	 * 配置信息
	 */
	public static String RedisConfig = RedisType+":config";
	/**
	 * 中奖列表
	 */
	public static String RedisPrizeWinner = RedisType+":prize_winner";
	
	/**
	 * 昨日剩余奖品数
	 */
	public static String RedisYesterdayPrize = RedisType+":yesterday_prize";
	
	
}
