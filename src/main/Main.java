package main;

import dataProcess.CalFeeIndex;
import dataProcess.DataPrePro;

public class Main {
	
	private static String exTransDataPath = "/home/highwaytransaction/extransaction";
	private static String preProDataPath = "/home/pq/truckFeeAnalysis/preProData";
	private static String feeByDayPath = "/home/pq/truckFeeAnalysis";
	private static String feeByDayFilePath = "/home/pq/truckFeeAnalysis/2018(6-8)_feeByDay.csv";
	private static String feeStatisticsResPath = "/home/pq/truckFeeAnalysis/feeStatisticsRes.csv";
	private static String feeByVehPath = "/home/pq/truckFeeAnalysis/2018(6-8)_feeByVeh.csv";
	private static String feeStatisticsByWeekResPath = "/home/pq/truckFeeAnalysis/feeStatisticsByWeekRes.csv";
	
	public static void main(String[] args){
		//1、数据预处理：提取需要数据（车型大于11），每月对应一个文件
		//结果字段：①交易时间；②交易金额；③出口收费车型；④车牌
		//DataPrePro.dataPrePro(exTransDataPath, preProDataPath);
		
		//2、以车为计算单位，计算每辆车的日消费金额
		//结果字段：①车牌号；②日期；③日消费总金额
		DataPrePro.calFeeByDay(preProDataPath, feeByDayPath);
		
		//3、计算每辆车的日均，周均，月均消费，以及最大日消费，最大周消费，最大月消费。
		//结果文件：①车牌号；②日均；③周均；④月均；⑤最大日；⑥最大周；⑦最大月
		CalFeeIndex.calFeeIndex(feeByDayFilePath, feeStatisticsResPath);
		
		//按照诸老师要求重新组织输出文件：1、出行记录不要合并（即不计算日消费总金额，将所有通行记录罗列出来）；2、新增总消费额，总通行次数，每周的具体花费以及周均，周方差。
		
		//2.2、以车为计算单位，筛掉不合格数据，并重新组织记录，将同一辆车的记录放在一起
		//结果字段：①车牌号；②车型；③日期；④收费金额
		DataPrePro.calFeeByVeh(preProDataPath, feeByDayPath);
		
		//3.2、计算每辆车6-8月份总通行费用和次数，以及每周消费金额，周均，周方差（其中周均和周方差忽略掉了周消费为0的情况，不计入计算）
		//结果文件：车牌号,总通行费，通行次数,第1周费用,第2周费用,第3周费用,第4周费用,第5周费用,第6周费用,第7周费用,第8周费用,第9周费用,第10周费用,第11周费用,第12周费用,周均,周方差
		CalFeeIndex.calFeeByWeek(feeByVehPath, feeStatisticsByWeekResPath);
	}
}
