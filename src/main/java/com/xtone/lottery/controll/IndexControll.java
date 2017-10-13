package com.xtone.lottery.controll;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtone.lottery.services.LotteryService;
import com.xtone.lottery.vo.ResultMsg;

@RestController
@RequestMapping(value="lottery")
public class IndexControll {

	public static Logger logger = LoggerFactory.getLogger(IndexControll.class);
	
	@Autowired
	private LotteryService sercice;
	
	public final static String[] agent = { "MicroMessenger" };

	public static boolean checkAgentIsMobile(String ua) {
		boolean flag = false;
		if(StringUtils.isEmpty(ua)) {
			ua = "";
		}
		ua = ua.toLowerCase();
		if (!ua.contains("Windows NT") || (ua.contains("Windows NT") && ua.contains("compatible; MSIE 9.0;"))) {
			// 排除 苹果桌面系统
			if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
				for (String item : agent) {
					if (ua.contains(item.toLowerCase())) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}
	
	@RequestMapping(value="lottery_by_id")
	public ResultMsg lotteryById(HttpServletResponse resp, String userId) {
		modifyHeader(resp);
		if (StringUtils.isEmpty(userId)) {
			return ResultMsg.fall("userId不能为空!");
		}
		
		try {
			return sercice.lottery(userId);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("抽奖失败!"+e.getMessage());
		}
	}
	
	
	@RequestMapping
	public ResultMsg lottery(HttpServletResponse resp, @RequestHeader("User-Agent") String userAgent) {
		modifyHeader(resp);
		
		try {
			isWeiXin(userAgent);
			return sercice.lottery();
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("抽奖失败!"+e.getMessage());
		}
	}
	
	@RequestMapping(value="setup")
	public ResultMsg setup(HttpServletResponse resp,@RequestParam(required=false) String nowDay
			,@RequestParam(required=false) String rate) {
		modifyHeader(resp);
		try {
			return sercice.setup(nowDay, rate);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("更新配置异常!"+e.getMessage());
		}
	}
	
	@RequestMapping(value="switch")
	public ResultMsg switchDay(HttpServletResponse resp, int switchDay) {
		modifyHeader(resp);
		try {
			sercice.switchDay(switchDay);
			return ResultMsg.ok("");
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("切换抽奖日错误："+e.getMessage());
		}
	}
	
	@RequestMapping(value="show")
	public ResultMsg show(HttpServletResponse resp) {
		modifyHeader(resp);
		try {
			return sercice.show();
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("显示配置信息错误："+e.getMessage());
		}
	}
	
	@RequestMapping(value="queryDateCode")
	public ResultMsg queryDateCode(HttpServletResponse resp, String dateCode) {
		modifyHeader(resp);
		try {
			return sercice.queryDateCode(dateCode);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("查询中奖码："+e.getMessage());
		}
	}
	
	/**
	 * 获取当天所有中奖的名单
	 * @param resp
	 * @return
	 */
	@RequestMapping(value="get_prize_winner")
	public ResultMsg getPrizeWinner(HttpServletResponse resp) {
		modifyHeader(resp);
		try {
			return sercice.getPrizeWinner();
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("获取中奖名单错误："+e.getMessage());
		}
	}
	
	/**
	 * 兑奖
	 * @param resp
	 * @param dateCode
	 * 兑奖码
	 * @return
	 */
	@RequestMapping(value="redeem")
	public ResultMsg redeem(HttpServletResponse resp, String dateCode) {
		modifyHeader(resp);
		try {
			return sercice.redeem(dateCode);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("兑奖错误："+e.getMessage());
		}
	}
	
	private void modifyHeader(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
	}
	
	private void isWeiXin(String userAgent) throws Exception {
		logger.info("userAgent:--->"+userAgent);
		if(!checkAgentIsMobile(userAgent)) {
//			throw new Exception("非法请求，拒绝处理!");
		}
	}
	
	/**
	 * 初始化
	 * @param resp
	 * @return
	 */
	@RequestMapping(value="init")
	public ResultMsg init(HttpServletResponse resp, String pwd) {
		modifyHeader(resp);
		try {
			if ("hjd".equals(pwd)) {
				return sercice.init();
			}
			return ResultMsg.fall("error");
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("显示配置信息错误："+e.getMessage());
		}
	}
	
	@RequestMapping(value="residuePrize")
	public ResultMsg residuePrize(HttpServletResponse resp) {
		modifyHeader(resp);
		try {
			return sercice.residuePrize();
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMsg.fall("获取剩余奖品数异常："+e.getMessage());
		}
	}
}
