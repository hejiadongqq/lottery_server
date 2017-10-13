package com.xtone.lottery.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.xtone.lottery.cfg.Cfg;
import com.xtone.lottery.vo.Config;
import com.xtone.lottery.vo.Prize;
import com.xtone.lottery.vo.ResultMsg;
import com.xtone.lottery.vo.User;

@Service
public class LotteryService {

	public static Logger logger = LoggerFactory.getLogger(LotteryService.class);

	@Autowired
	public Gson gson;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Resource(name = "stringRedisTemplate")
	private ValueOperations<String, String> redis;

	@Autowired
	private RedisTemplate redisTemplate;

	public ResultMsg lottery() throws Exception {
		String userId = UUID.randomUUID().toString();

		return lottery(userId);
	}
	
	public ResultMsg getPrizeWinner() throws Exception {
		logger.info("获取当日中奖名单");
		Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
		if (config == null) {
			throw new Exception("系统读取配置信息错误");
		}
		int dayNow = config.getNowDay();
		logger.info("现在为抽奖第" + dayNow + "日");
		
		Set prizeWinner = redisTemplate.opsForSet().members(Cfg.RedisPrizeWinner + ":day" + dayNow);
		
//		Set<User> prizeWinner = gson.fromJson(, User.class);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", prizeWinner);
		
		return makeResult(map);
	}
	
	public ResultMsg residuePrize() throws Exception {
		logger.info("获取剩余奖品数");
		Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
		if (config == null) {
			throw new Exception("系统读取配置信息错误");
		}
		
//		Set<User> prizeWinner = gson.fromJson(, User.class);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", residuePrize(config));
		
		// 获取昨日剩余奖品数
		
		return makeResult(map);
	}
	
//	private long yesterDay(String) {
//		// 昨日剩余奖品数
//		long yesterdayPrize = 0;
//		// 获取前一日的
//		if (dayNow > 1 && redisTemplate.hasKey(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1))) {
//			yesterdayPrize = Long.valueOf(redis.get(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1)));
//		}
//	}
	
	/**
	 * 获取剩余奖品总数
	 * @param config
	 * @return
	 * @throws Exception
	 */
	private long residuePrize(Config config) throws Exception {
		long result = 0;
		// 现在是第几日
		int dayNow = config.getNowDay();
		logger.info("现在为抽奖第" + dayNow + "日");

		// 获取当日奖品总数
		if (config.getDayPrize() == null) {
			throw new Exception("没有配置总奖品数");
		}
		long prizeCount = config.getDayPrize() != null
				? config.getDayPrize().get("day" + dayNow) != null ? config.getDayPrize().get("day" + dayNow) : 0
				: 0;
		if (prizeCount <= 0) {
			logger.error("配置的奖品总数为0，抽奖者设置为未中奖");
			throw new Exception("已过抽奖时间");
		}
		logger.info("系统配置的今日奖品总数" + prizeCount);

		// 昨日剩余奖品数
		long yesterdayPrize = 0;
		// 获取前一日的
		if (dayNow > 1 && redisTemplate.hasKey(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1))) {
			yesterdayPrize = Long.valueOf(redis.get(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1)));
		}
		logger.info("昨日剩余的奖品总数" + yesterdayPrize);
		prizeCount = prizeCount + yesterdayPrize;
		logger.info("今日奖品总数(加上昨天的奖品数" + yesterdayPrize + ")共:" + prizeCount);

		config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
		long prizeWinner = 0;// 获取今日已经中奖总数
		double rate = 0;// 中奖率

		synchronized (config) {
			// 获取中奖总列表
			prizeWinner = redisTemplate.opsForSet().size(Cfg.RedisPrizeWinner + ":day" + dayNow);
			if (config.getRate() != 0) {
				rate = config.getRate();
			}
		}

		result = prizeCount - prizeWinner;
		logger.info("今日中奖总数为:" + prizeWinner + ",中奖率为" + rate + "%, 当前还剩" + (result) + "个奖品");

		return result;
	}

	public ResultMsg lottery(String userId) throws Exception {
		logger.info("userId:" + userId + "，开始检测!");

		if (!redisTemplate.hasKey(Cfg.RedisAlready) || !redisTemplate.opsForSet().isMember(Cfg.RedisAlready, userId)) {
			logger.info("userId:" + userId + "，未抽奖，开始抽奖!");

			Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
			if (config == null) {
				throw new Exception("系统读取配置信息错误");
			}

			// 现在是第几日
			int dayNow = config.getNowDay();
			logger.info("现在为抽奖第" + dayNow + "日");

			// 获取当日奖品总数
			if (config.getDayPrize() == null) {
				throw new Exception("没有配置总奖品数");
			}
			long prizeCount = config.getDayPrize() != null
					? config.getDayPrize().get("day" + dayNow) != null ? config.getDayPrize().get("day" + dayNow) : 0
					: 0;
			if (prizeCount <= 0) {
				logger.error("配置的奖品总数为0，抽奖者设置为未中奖");
				throw new Exception("已过抽奖时间");
			}
			logger.info("系统配置的今日奖品总数" + prizeCount);

			// 昨日剩余奖品数
			long yesterdayPrize = 0;
			// 获取前一日的
			if (dayNow > 1 && redisTemplate.hasKey(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1))) {
				yesterdayPrize = Long.valueOf(redis.get(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1)));
			}
			logger.info("昨日剩余的奖品总数" + yesterdayPrize);
			prizeCount = prizeCount + yesterdayPrize;
			logger.info("今日奖品总数(加上昨天的奖品数" + yesterdayPrize + ")共:" + prizeCount);

			config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
			long prizeWinner = 0;// 获取今日已经中奖总数
			double rate = 0;// 中奖率

			synchronized (config) {
				// 获取中奖总列表
				prizeWinner = redisTemplate.opsForSet().size(Cfg.RedisPrizeWinner + ":day" + dayNow);
				if (config.getRate() != 0) {
					rate = config.getRate();
				}
			}

			logger.info("今日中奖总数为:" + prizeWinner + ",中奖率为" + rate + "%, 当前还剩" + (prizeCount - prizeWinner) + "个奖品");

			User user = new User();
			user.setTime(new Date());
			user.setUserId(userId);

			if (prizeWinner >= prizeCount) {
				logger.warn("今日中奖总数已经大于或等于当日的奖品总数，不再抽奖");
				return makeResult(makeMap(false, dayNow, user));
			}

			// 抽奖
			boolean success = lotteryRandom(rate);
			return makeResult(makeMap(success, dayNow, user));
		}
		logger.info("userId:" + userId + "，已抽奖过了!");
		return ResultMsg.fall("您已抽过奖了，请不要重复抽奖!");
	}
	
	/**
	 * 产生唯一的中奖码
	 * @return
	 * @throws Exception
	 */
	private String checkDateCodeExists() throws Exception {
		String dateCode = randomCommon();
		for(int i=0;i<10;i++) {
			if (i==10) throw new Exception("中奖码重复太多，无法产生新的中奖码");
			if (redisTemplate.hasKey(Cfg.RedisPrizeWinner + ":"+dateCode)) {
				// 存在，重新生成
				dateCode = randomCommon();
			} else {
				break;
			}
		}
		return dateCode;
	}

	private Map<String, Object> makeMap(boolean success, int dayNow, User user) throws Exception {
		logger.info("userId:" + user.getUserId() + "|抽奖结果:" + success);

		if (success) {

			// 检测生成的中奖码是否已经使用过
			String dateCode = checkDateCodeExists();
			
			Prize p = new Prize();
			p.setDateCode(dateCode);// 产生唯一领奖码
			p.setReceive(false);// 设置领奖状态为 未领奖
			p.setDayPrize("day"+dayNow);// 中奖日
			p.setPrizeTime(user.getTime());// 中奖时间
			user.setPrize(p);
			
			// 存储兑奖记录索引
			redisTemplate.opsForSet().add(Cfg.RedisPrizeWinner + ":day" + dayNow, gson.toJson(user));// 记录当天中奖者
			redis.set(Cfg.RedisPrizeWinner + ":"+p.getDateCode(), gson.toJson(user));// 记录中奖者总列表用于搜索
//			
		}
		
		// 记录抽奖记录
		redisTemplate.opsForSet().add(Cfg.RedisAlready + ":day" + dayNow, user);// 按天设置已抽奖标识
		redisTemplate.opsForSet().add(Cfg.RedisAlready, user.getUserId());// 设置总的已抽奖标识
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", success);// 抽奖状态 true，中奖 false为未中奖
		map.put("sn", user.getUserId());// 抽奖的用户id，唯一id
		map.put("user", user);// 抽奖的一些状态信息

		return map;
	}

	private ResultMsg makeResult(Map<String, Object> map) {
		ResultMsg r = ResultMsg.ok(map);
		return r;
	}
	
	/**
	 * 抽奖,中奖为真，未中奖返回假
	 * 
	 * @param rate
	 * @return
	 */
	private boolean lotteryRandom(double rate) {
		int max = 100;
		int min = 1;
		Random random = new Random();

		int s = random.nextInt(max) % (max - min + 1) + min;
		logger.info("random:" + s);
		if (s <= rate)
			return true;
		return false;
	}

	/**
	 * 更新配置信息
	 * 
	 * @param config
	 * @return
	 */
	public ResultMsg setup(String dayNow, String rate) throws Exception {
		Config configSys = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
		boolean update = false;
		
		int rateNum = Integer.parseInt(rate);
		if (!StringUtils.isEmpty(dayNow)) {
			configSys.setNowDay(Integer.valueOf(dayNow));
			logger.info("更新dayNow为"+dayNow);
			update = true;
		}
		if (!StringUtils.isEmpty(rate)) {
			if (rateNum < 0 || rateNum > 100)
				throw new Exception("中奖几率不能小于0，大于100，请重新配置!");
			configSys.setRate(rateNum);
			logger.info("更新中奖率rate为"+rateNum);
			update = true;
		}

		if (update) {
			synchronized (configSys) {
				redis.set(Cfg.RedisConfig, gson.toJson(configSys));
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("config", gson.fromJson(redis.get(Cfg.RedisConfig), Config.class));
			return makeResult(map);
		}

		return ResultMsg.fall("无更新");
	}
	
	public void switchDay(int switchDay) throws Exception {
		logger.info("开始切换抽奖日 to " + switchDay);
		if (switchDay > 0) {

			Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
			if (config == null) {
				throw new Exception("系统读取配置信息错误");
			}
			Map<String, Integer> map = config.getDayPrize();
			if (map == null) {
				throw new Exception("奖品数配置信息未检测到");
			}

			// 检测切换到指定日的奖品数是否已指定
			if (!map.containsKey("day" + switchDay)) {
				throw new Exception("指定的抽奖日day" + switchDay + "，奖品数配置未加测到");
			}

			// 现在是第几日
			int dayNow = config.getNowDay();
			logger.info("现在为抽奖第" + dayNow + "日");

			if (switchDay == dayNow) {
				throw new Exception("切换日与当前日不能相同！");
			}

			if (!(switchDay == (dayNow + 1)) && switchDay != 1) {
				throw new Exception("只能有小到大逐日切换日期，不能跨日切换" + switchDay + " |" + dayNow);
			}

			// 切换日大于当前日期，则需要
			
			if (switchDay > 1) {
				if (!map.containsKey("day" + dayNow)) {
					throw new Exception("当日的中奖品数未配置！" + dayNow);
				}

				// 获取现在的奖品数 = 系统配置的当日奖品数 - 当日的中奖人数
				int dayNowPrizeCount = map.get("day" + dayNow);
				logger.info("第" + dayNow + "日（当日）, 总奖品数" + dayNowPrizeCount);
				
				// 获取当前日期的前一天的 剩余奖品数
				long yesterdayPrize = 0l;
				if (redisTemplate.hasKey(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1))) {
					yesterdayPrize = Long.valueOf(redis.get(Cfg.RedisYesterdayPrize + ":day" + (dayNow - 1)));
				}
				logger.info("前一日剩余的奖品总数" + yesterdayPrize);

				// 获取中奖人数
				long dayNowPrizeWinner = 0l;
				if (redisTemplate.hasKey(Cfg.RedisPrizeWinner + ":day" + dayNow)) {
					dayNowPrizeWinner = redisTemplate.opsForSet().size(Cfg.RedisPrizeWinner + ":day" + dayNow);
				}
				logger.info("第" + dayNow + "日（当日）, 总中奖人数" + dayNowPrizeWinner);
				
				long dayCountPrize = (dayNowPrizeCount + yesterdayPrize) - dayNowPrizeWinner;
				logger.info("本日剩余奖品数为 (昨日剩余奖品数"+yesterdayPrize+" + 今日奖品数"+dayNowPrizeCount
						+") - 今日中奖人数"+dayNowPrizeWinner +"=" + dayCountPrize);
				if (dayCountPrize < 0)
					dayCountPrize = 0l;

				logger.info("第" + dayNow + "日（当日）, 剩余奖品数" + dayCountPrize);
				// 保存剩余奖品数
				redis.set(Cfg.RedisYesterdayPrize + ":day" + (switchDay - 1), dayCountPrize + "");
			} else {
				redis.set(Cfg.RedisYesterdayPrize + ":day1", "0");
			}

			// 切换当前日
			config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
			synchronized (config) {
				config.setNowDay(switchDay);
				redis.set(Cfg.RedisConfig, gson.toJson(config));
			}

			return;
		}
		throw new Exception("切换失败 抽奖日 不能小于0 ");
	}

	public ResultMsg show() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("config", gson.fromJson(redis.get(Cfg.RedisConfig), Config.class));
		return makeResult(map);
	}

	private static String randomCommon() {
		int[] randoms = randomCommon(0, 9, 8);
		String random = "";
		for (int r : randoms) {
			random += r;
		}
		return random;
	}

	/**
	 * 随机指定范围内N个不重复的数 最简单最基本的方法
	 * 
	 * @param min
	 *            指定范围最小值
	 * @param max
	 *            指定范围最大值
	 * @param n
	 *            随机数个数
	 */
	public static int[] randomCommon(int min, int max, int n) {
		if (n > (max - min + 1) || max < min) {
			return null;
		}
		int[] result = new int[n];
		int count = 0;
		while (count < n) {
			int num = (int) (Math.random() * (max - min)) + min;
			boolean flag = true;
			for (int j = 0; j < n; j++) {
				if (num == result[j]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				result[count] = num;
				count++;
			}
		}
		return result;
	}

	/**
	 * 初始化配置信息
	 * 
	 * @throws Exception
	 */
	public ResultMsg init() throws Exception {
		logger.info("初始化redis配置信息");

		if (redisTemplate.hasKey(Cfg.RedisConfig)) {
			throw new Exception("配置文件已存在不需要初始化");
		}
		delAll("lottery");
		Config config = new Config();
		config.setNowDay(1);
		config.setSwitch(true);
		config.setRate(75);
		Map<String, Integer> dayPrize = new HashMap<String, Integer>();
		dayPrize.put("day1", 150);
		dayPrize.put("day2", 150);
		config.setDayPrize(dayPrize);

		redis.set(Cfg.RedisConfig, gson.toJson(config));
		logger.info("初始化成功！！！");

		return ResultMsg.ok("");
	}
	
	public ResultMsg redeem(String dateCode) throws Exception {
		logger.info("领奖"+dateCode);
		Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
		if (config == null) {
			throw new Exception("系统读取配置信息错误");
		}
		if (StringUtils.isEmpty(dateCode)) {
			throw new Exception("领奖码不能为空");
		}
		if (!redisTemplate.hasKey(Cfg.RedisPrizeWinner + ":"+dateCode)) {
			throw new Exception("领奖码不存在!");
		}

		String strUser = redis.get(Cfg.RedisPrizeWinner + ":"+dateCode);
		User user = gson.fromJson(strUser, User.class);
		
		if (user.getPrize().isReceive()) {
			throw new Exception("领奖码已领取"+dateCode);
		}
		
		String day = user.getPrize().getDayPrize();
		redisTemplate.opsForSet().remove(Cfg.RedisPrizeWinner + ":"+day, strUser);
		
		// 设置为已领奖状态
		user.getPrize().setReceive(true);user.getPrize().setReceiveTime(new Date());
		redis.set(Cfg.RedisPrizeWinner + ":"+dateCode, gson.toJson(user));// 更新领奖总索引记录
		redisTemplate.opsForSet().add(Cfg.RedisPrizeWinner + ":"+day, gson.toJson(user));
		
		logger.info("更新领奖码状态成功"+dateCode);
		
		long residuePrizeNum = residuePrize(config);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("residuePrize", residuePrizeNum);
		return makeResult(map);
	}
	
	private boolean queryCode(String dateCode) throws Exception {
		if (StringUtils.isEmpty(dateCode)) {
			throw new Exception("领奖码不能为空");
		}

		String strUser = redis.get(Cfg.RedisPrizeWinner + ":"+dateCode);
		boolean resu = false;
		User user = gson.fromJson(strUser, User.class);
		if (user!=null) {
			if (!user.getPrize().isReceive()) {
				resu = true;
			}
		}
		
		return resu;
	}
	
	public ResultMsg queryDateCode(String dateCode) throws Exception {
		logger.info("领奖"+dateCode);
		boolean resu = queryCode(dateCode);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", resu);
		return makeResult(map);
	}
	
	/**
	 * 删除所有key下值
	 * 
	 * @param key
	 */
	private void delAll(String key) {
		Set<String> set = redisTemplate.keys(key + "*");
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String keyStr = it.next();
			logger.info("del key ---->" + keyStr);
			redisTemplate.delete(keyStr);
		}
	}

}
