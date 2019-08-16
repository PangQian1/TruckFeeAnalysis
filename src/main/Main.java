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
		//1������Ԥ������ȡ��Ҫ���ݣ����ʹ���11����ÿ�¶�Ӧһ���ļ�
		//����ֶΣ��ٽ���ʱ�䣻�ڽ��׽��۳����շѳ��ͣ��ܳ���
		//DataPrePro.dataPrePro(exTransDataPath, preProDataPath);
		
		//2���Գ�Ϊ���㵥λ������ÿ�����������ѽ��
		//����ֶΣ��ٳ��ƺţ������ڣ����������ܽ��
		DataPrePro.calFeeByDay(preProDataPath, feeByDayPath);
		
		//3������ÿ�������վ����ܾ����¾����ѣ��Լ���������ѣ���������ѣ���������ѡ�
		//����ļ����ٳ��ƺţ����վ������ܾ������¾���������գ�������ܣ��������
		CalFeeIndex.calFeeIndex(feeByDayFilePath, feeStatisticsResPath);
		
		//��������ʦҪ��������֯����ļ���1�����м�¼��Ҫ�ϲ������������������ܽ�������ͨ�м�¼���г�������2�����������Ѷ��ͨ�д�����ÿ�ܵľ��廨���Լ��ܾ����ܷ��
		
		//2.2���Գ�Ϊ���㵥λ��ɸ�����ϸ����ݣ���������֯��¼����ͬһ�����ļ�¼����һ��
		//����ֶΣ��ٳ��ƺţ��ڳ��ͣ������ڣ����շѽ��
		DataPrePro.calFeeByVeh(preProDataPath, feeByDayPath);
		
		//3.2������ÿ����6-8�·���ͨ�з��úʹ������Լ�ÿ�����ѽ��ܾ����ܷ�������ܾ����ܷ�����Ե���������Ϊ0���������������㣩
		//����ļ������ƺ�,��ͨ�зѣ�ͨ�д���,��1�ܷ���,��2�ܷ���,��3�ܷ���,��4�ܷ���,��5�ܷ���,��6�ܷ���,��7�ܷ���,��8�ܷ���,��9�ܷ���,��10�ܷ���,��11�ܷ���,��12�ܷ���,�ܾ�,�ܷ���
		CalFeeIndex.calFeeByWeek(feeByVehPath, feeStatisticsByWeekResPath);
	}
}
