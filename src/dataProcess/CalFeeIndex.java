package dataProcess;

import java.awt.dnd.DragGestureEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLNonTransientConnectionException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.monitor.Monitor;

public class CalFeeIndex {
	
	private static String feeByDayPath = "I:\\programData\\truckFee\\2018(6-8)_feeByDay.csv";
	//private static String feeByDayPath = "I:\\programData\\truckFee\\test.csv";
	private static String feeStatisticsResPath = "I:\\programData\\truckFee\\feeStatisticsRes.csv";
	//private static String feeStatisticsResPath = "I:\\programData\\truckFee\\res.csv";
	
	public static String formatDate(String date) {
		StringBuilder resBuilder = new StringBuilder(date);
		resBuilder.insert(6, "-").insert(4, "-");
		String res = resBuilder.toString();
		return res;
	}
	
	//�ж�ʱ���Ƿ���2018��6-8�·���
	public static Boolean isInPeriod(String date) {
		Boolean flag = false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String initDay = "2018-06-01";
		
		try {
			Date curDate = sdf.parse(date);
			Date initDate = sdf.parse(initDay);
			
			//һ��Ϊ604800��,6-8�·ݹ�7948800��
			long timeInterval = (curDate.getTime() - initDate.getTime())/(long)1000;
			
			if(timeInterval >= 0 && timeInterval < 7948800) {
				flag = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return flag;

	}
	
	public static int getWeek(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String init = "2018-05-28";
		int indexOfWeek = -1;

		try {
			Date curDate = sdf.parse(date);
			Date initDate = sdf.parse(init);
			
			//һ��Ϊ604800��
			long timeInterval = (curDate.getTime() - initDate.getTime())/(long)1000;
			indexOfWeek = (int)(timeInterval/(long)604800);
		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(indexOfWeek < 0 || indexOfWeek > 13) {
			indexOfWeek = -1;
		}
		return indexOfWeek;		
	}
	
	public static int getMon(String date) {
		String mon[] = date.split("-");
		int indexOfMon = -1;
		
		if(mon[1].equals("06")) indexOfMon = 0;
		if(mon[1].equals("07")) indexOfMon = 1;
		if(mon[1].equals("08")) indexOfMon = 2;
		
		return indexOfMon;		
	}
	
	public static long getMaxFee(long[] arr) {		
		long max = 0;
		for(int i = 0; i < arr.length; i++) {
			if(arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * ����ÿ�������վ����ܾ����¾����ѣ��Լ���������ѣ���������ѣ���������ѡ�
	 * @param inPath 2018(6-8)_feeByDay.csv�ļ� �ֶΣ��ٳ��ƺţ������ڣ����������ܽ��
	 * @param outPath feeStatisticsRes.csv�ļ�  �ֶΣ��ٳ��ƺţ����վ������ܾ������¾���������գ�������ܣ��������
	 */
	public static void calFeeIndex(String inPath, String outPath){
		DecimalFormat df = new DecimalFormat("###");//����Ϊ����0λ

		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("���ƺ�,�վ�,�ܾ�,�¾�,�����,�����,�����\n");

			String line = reader.readLine();
			String[] lineArray;
			String curPlate = "";
			long maxDay = 0;
			long maxWeek = 0;
			long maxMon = 0;
			long sumFee = 0;//�����µ������ѽ��
			long sumFeeRem61 = 0;	
			
			long[] feeByWeek = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			long[] feeByMon = {0,0,0};

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 3) {
					
					String plate = lineArray[0].trim();//���ƺ�
					String date = formatDate(lineArray[1]);//����
					long fee = Long.parseLong(lineArray[2]);//�������ܽ��
					
					if(fee == 0) {//�޵����ѽ��Ϊ0�ļ�¼
						continue;
					}
					
					if(curPlate.equals(plate)) {//�о�ͬһ����
						
						if(maxDay < fee) maxDay = fee;//�����
						if(isInPeriod(date)) {//���Լ����վ����ܾ����¾�
							sumFee += fee;
							if(!date.equals("2018-06-01")) sumFeeRem61 += fee;
						}
						
						
						int indexOfWeek = getWeek(date);//�����
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
						int indexOfMon = getMon(date);//�����
						if(indexOfMon != -1) {
							feeByMon[indexOfMon] += fee;
						}
					}else {
						//������ǵ�һ�ν�����Ҫ����һ��������д���ļ�
						if(!curPlate.equals("") && sumFee != 0) {
							maxWeek = getMaxFee(feeByWeek);
							maxMon = getMaxFee(feeByMon);
							String dayAve = df.format(sumFee/(double)100/92);
							String monAve = df.format(sumFee/(double)100/3);
							String weekAve = df.format(sumFeeRem61/(double)100/13);
							//System.out.println(sumFee + " " + sumFeeRem61);
							writer.write(curPlate+","+dayAve+","+weekAve+","+monAve+","+df.format(maxDay/(double)100)+","
										 +df.format(maxWeek/(double)100)+","+df.format(maxMon/(double)100)+"\n");
						}
						
						curPlate = plate;
						maxDay = fee;
						maxWeek = 0;
						maxMon = 0;
						sumFee = 0;//�����µ������ѽ��
						sumFeeRem61 = 0;	
						
						for(int i = 0; i < feeByWeek.length; i++){
							feeByWeek[i] = 0;
						}
						for(int i = 0; i < feeByMon.length; i++){
							feeByMon[i] = 0;
						}
						
						if(isInPeriod(date)){
							sumFee += fee;
							if(!date.equals("2018-06-01")) sumFeeRem61 += fee;
						}
						
						int indexOfWeek = getWeek(date);
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
						int indexOfMon = getMon(date);
						if(indexOfMon != -1) {
							feeByMon[indexOfMon] += fee;
						}
						
					}					
				}
			}
			
			if(sumFee != 0) {
				maxWeek = getMaxFee(feeByWeek);
				maxMon = getMaxFee(feeByMon);
				String dayAve = df.format(sumFee/(double)100/92);
				String monAve = df.format(sumFee/(double)100/3);
				String weekAve = df.format(sumFeeRem61/(double)100/13);
				//System.out.println(sumFee + " " + sumFeeRem61);
				writer.write(curPlate+","+dayAve+","+weekAve+","+monAve+","+df.format(maxDay/(double)100)+","
							 +df.format(maxWeek/(double)100)+","+df.format(maxMon/(double)100)+"\n");
			}
			
			reader.close();	
			System.out.println(inPath + " read finish!");
			writer.flush();
			writer.close();
			System.out.println(outPath + " write finish !");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************���ݼ������*************");
	}
	
	//��ȡƽ��ֵ
	public static double getAverage(long[] arr){
        long sum = 0;
        int len = arr.length;
        int num = 0;
        for(int i = 1;i < len-1;i++){
        	if(arr[i] != 0) {
                sum += arr[i];
                num++;
        	}
        }
        if(num == 0) {
        	return 0;
        }
        double ave = (double)sum / num;
        return ave;
    }
	
	//��ȡ��׼��
    public static double getVariance(long[] arr, double ave){
        double sum = 0;
        int len = arr.length;
        int num = 0;
        for(int i = 1;i < len-1;i++){
        	if(arr[i] != 0) {
        		sum += ((double)arr[i] - ave) * (arr[i] - ave);
        		num++;
        	}
        }
        
        if(num == 0 || num == 1) {
        	return 0;
        }
        
        double var = (sum / num);
        
        return Math.sqrt(var);
    }
    
    //��ȡ��������߶�
	public static long getMaxFeeByWeek(long[] arr) {		
		long max = 0;
		for(int i = 1; i < (arr.length-1); i++) {
			if(arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * ����ÿ����6-8�·���ͨ�з��úʹ������Լ�ÿ�����ѽ��ܾ����ܷ�������ܾ����ܷ�����Ե���������Ϊ0���������������㣩
	 * @param inPath 2018(6-8)_feeByVeh.csv�ļ� ����ֶΣ��ٳ��ƺţ��ڳ��ͣ������ڣ����շѽ��  
	 * @param outPath ����ļ������ƺ�,��ͨ�зѣ�ͨ�д���,��1�ܷ���,��2�ܷ���,��3�ܷ���,��4�ܷ���,��5�ܷ���,��6�ܷ���,��7�ܷ���,��8�ܷ���,��9�ܷ���,��10�ܷ���,��11�ܷ���,��12�ܷ���,�ܾ�,�ܷ���
	 */
	public static void calFeeByWeek(String inPath, String outPath) {
		DecimalFormat df2 = new DecimalFormat("###.##");//����Ϊ����2λ

		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("���ƺ�,��ͨ�з�,ͨ�д���,"
					+ "��1�ܷ���,��2�ܷ���,��3�ܷ���,��4�ܷ���,��5�ܷ���,��6�ܷ���,��7�ܷ���,��8�ܷ���,��9�ܷ���,��10�ܷ���,��11�ܷ���,��12�ܷ���,�ܾ�,�ܱ�׼��,�����\n");

			String line = reader.readLine();
			String[] lineArray;
			String curPlate = "";
			int sumTrip = 0;//�Գ�Ϊ��λ��������д���
			long sumFee = 0;//�����µ������ѽ��
			
			long[] feeByWeek = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 4) {
					
					String plate = lineArray[0].trim();//���ƺ�
					String date = formatDate(lineArray[2]);//����
					long fee = Long.parseLong(lineArray[3]);//���ѽ��
					
					if(fee == 0) {//�޵����ѽ��Ϊ0�ļ�¼
						continue;
					}
					
					if(curPlate.equals(plate)) {//�о�ͬһ����						
						
						if(isInPeriod(date)){
							sumFee += fee;
							sumTrip++;
						}
						
						int indexOfWeek = getWeek(date);//����ÿ���ܵķ���
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
					}else {
						//������ǵ�һ�ν�����Ҫ����һ��������д���ļ�
						if(!curPlate.equals("") && sumFee != 0) {
							writer.write(curPlate + "," + sumFee + "," + sumTrip + ",");
							for(int i = 1; i < feeByWeek.length-1; i++){
								writer.write(feeByWeek[i] + ",");
							}
							double ave = getAverage(feeByWeek);
							double var = getVariance(feeByWeek, ave);
							long max = getMaxFeeByWeek(feeByWeek);
							writer.write(df2.format(ave) + "," + df2.format(var) + "," + max);
							writer.write("\n");
						}
						curPlate = plate;
						sumFee = 0;//�����µ������ѽ��
						sumTrip = 0;
						
						for(int i = 0; i < feeByWeek.length; i++){
							feeByWeek[i] = 0;
						}
						
						if(isInPeriod(date)){
							sumFee += fee;
							sumTrip++;
						}
						
						int indexOfWeek = getWeek(date);
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
					}					
				}
			}
			
			if(sumFee != 0) {
				writer.write(curPlate + "," + sumFee + "," + sumTrip + ",");
				for(int i = 1; i < feeByWeek.length-1; i++){
					writer.write(feeByWeek[i] + ",");
				}
				double ave = getAverage(feeByWeek);
				double var = getVariance(feeByWeek, ave);
				long max = getMaxFeeByWeek(feeByWeek);
				writer.write(df2.format(ave) + "," + df2.format(var) + "," + max);
				writer.write("\n");
			}
			
			reader.close();	
			System.out.println(inPath + " read finish!");
			writer.flush();
			writer.close();
			System.out.println(outPath + " write finish !");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************���ݼ������*************");
	}
	
	
	public static void main(String[] args) {
		//7-26�����⣺�����Ƿ���С���㣬�Լ���λ��Ԫ���߷ֵ����⣺������С���㣬��λ��Ԫ
		calFeeIndex(feeByDayPath, feeStatisticsResPath);
	}

}
