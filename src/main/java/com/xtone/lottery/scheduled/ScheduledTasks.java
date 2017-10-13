package com.xtone.lottery.scheduled;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.xtone.lottery.cfg.Cfg;
import com.xtone.lottery.services.LotteryService;
import com.xtone.lottery.vo.Config;


@Component 
public class ScheduledTasks {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

	@Autowired
	private LotteryService service;
	
	@Autowired
	public Gson gson;
	
	@Autowired  
    StringRedisTemplate stringRedisTemplate;
	
	@Resource(name="stringRedisTemplate")  
    private ValueOperations<String,String> redis;
	
	/**
	 * 切换抽奖日
	 */
    @Scheduled(cron="0 0 0 * * ?") 
    public void switchDayTask() {

        // 间隔2分钟,执行工单上传任务
        logger.info("定时任务1: 切换抽奖日");
        try {
        	Config config = gson.fromJson(redis.get(Cfg.RedisConfig), Config.class);
        	if (config.isSwitch()) {
        		service.switchDay(config.getNowDay()+1);
        		logger.info("切换抽奖日成功！当前抽奖日为"+config.getNowDay()+1);
        		return;
        	}
        	logger.info("不切换抽奖日");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("切换抽奖日异常", e);
		}
    }
    
//    @Scheduled(cron="*/2 0-59 0-18 * * ?") 
//    public void testTask() {
//
//        // 间隔2分钟,执行工单上传任务
//        logger.info("定时任务1: 切换抽奖日");
//        
//    }

}
