. ./env.app.conf
. ./env.conf
. ./env.sh

####################################
###  $APP_NAME应用不可同时运行2个
####################################
echo "[INFO]: 应用[$APP_NAME]正在初始化..";
if [ ! -d "$LOG_APP_HOME" ]; then
        mkdir -p "$LOG_APP_HOME"
        echo "[INFO]: 创建目录[$LOG_APP_HOME]成功."
fi
v_count=`ps -ef | grep "$RUN_COMMAND" | grep -v 'grep'|wc -l`
if [ $v_count -eq 0 ]; then
    echo "[INFO]: 应用[$APP_NAME]初始化完毕.";
else
		echo "[ERROR]: 已经有应用[$APP_NAME]在运行，无法再次启动!!";
		echo `ps -ef | grep "$RUN_COMMAND" | grep -v 'grep'`
		exit 0
fi

####################################
###  提交命令
####################################
echo "[INFO]: 应用[$APP_NAME]正在启动，时间: "`date +%Y%m%d_%H:%M:%S`
CONFIG_CLASSPATH=$CONFIG_HOME:$CONFIG_APP_HOME:$CONFIG_APP_SPECIAL_CLASSPATH
if [ "$CONFIG_APP_COMMON_HOME" != "" ]; then
	CONFIG_CLASSPATH=$CONFIG_CLASSPATH:$CONFIG_APP_COMMON_HOME
fi
CLASS_PATH=$CONFIG_CLASSPATH:$APP_RUN_HOME/$BIN_NAME:$(echo $APP_RUN_HOME/lib/*.jar|sed 's/ /:/g')
export CLASSPATH=$CLASS_PATH:$CLASSPATH
echo "CLASSPATH=$CLASSPATH"
echo "RUN_COMMAND=$RUN_COMMAND"
nohup $RUN_COMMAND 1>$LOG_APP_HOME/shell-${APP_NAME}-all.log 2>$LOG_APP_HOME/shell-${APP_NAME}-err.log &

sleep 2
echo "[INFO]: 命令已提交." 
sleep 1

####################################
###  判断$APP_NAME是否启动成功
####################################
count=`ps -ef | grep "$RUN_COMMAND" | grep -v 'grep'|wc -l`
if [ $count -eq 0 ]; then
    echo "[ERROR]: 应用[$APP_NAME]启动失败, 请检查!!";
    exit 0
elif [ $count -gt 1 ]; then
	echo "[ERROR]: 应用[$APP_NAME]启动进程数多于1个, 请检查!!"
	exit 0
fi

info="[INFO]: Application[$APP_NAME] is starting"
COUNTER=0
TOTAL=60
_R=${#info}
_C=`tput cols`
_PROCEC=`tput cols`
tput cup $_C 0
printf "$info."
while [ true ]
do
    if [ `grep "service server started" $LOG_APP_HOME/shell-${APP_NAME}-all.log|wc -l` == 1 ]; then
        printf "\n"
        echo "[INFO]: 应用[$APP_NAME]已启动, 时间: "`date +%Y%m%d_%H:%M:%S`
        break;
    fi
    sleep 0.5
    printf "."
    _R=`expr $_R + 1`
    _C=`expr $_C + 1`
    tput cup $_C $_R
    COUNTER=`expr $COUNTER + 1`
    TEMP_COUNT=`expr $COUNTER / 2`
    if [ $TEMP_COUNT -eq $TOTAL ]; then
    	printf "\n"
    	echo "[ERROR]: ip[${LOCAL_IP}]应用[$APP_NAME]在${TOTAL}s内未能启动成功, 请手工检查!!"
    	break;
    fi
done
echo "[INFO]: 查看输出日志目录[$LOG_APP_HOME/]"
