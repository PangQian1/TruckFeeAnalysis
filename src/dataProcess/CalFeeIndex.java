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
	
	//判断时间是否在2018年6-8月份中
	public static Boolean isInPeriod(String date) {
		Boolean flag = false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String initDay = "2018-06-01";
		
		try {
			Date curDate = sdf.parse(date);
			Date initDate = sdf.parse(initDay);
			
			//一周为604800秒,6-8月份共7948800秒
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
			
			//一周为604800秒
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
	 * 计算每辆车的日均，周均，月均消费，以及最大日消费，最大周消费，最大月消费。
	 * @param inPath 2018(6-8)_feeByDay.csv文件 字段：①车牌号；②日期；③日消费总金额
	 * @param outPath feeStatisticsRes.csv文件  字段：①车牌号；②日均；③周均；④月均；⑤最大日；⑥最大周；⑦最大月
	 */
	public static void calFeeIndex(String inPath, String outPath){
		DecimalFormat df = new DecimalFormat("###");//这样为保持0位

		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("车牌号,日均,周均,月均,最大日,最大周,最大月\n");

			String line = reader.readLine();
			String[] lineArray;
			String curPlate = "";
			long maxDay = 0;
			long maxWeek = 0;
			long maxMon = 0;
			long sumFee = 0;//三个月的总消费金额
			long sumFeeRem61 = 0;	
			
			long[] feeByWeek = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			long[] feeByMon = {0,0,0};

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 3) {
					
					String plate = lineArray[0].trim();//车牌号
					String date = formatDate(lineArray[1]);//日期
					long fee = Long.parseLong(lineArray[2]);//日消费总金额
					
					if(fee == 0) {//剔掉消费金额为0的记录
						continue;
					}
					
					if(curPlate.equals(plate)) {//研究同一车牌
						
						if(maxDay < fee) maxDay = fee;//最大日
						if(isInPeriod(date)) {//用以计算日均，周均，月均
							sumFee += fee;
							if(!date.equals("2018-06-01")) sumFeeRem61 += fee;
						}
						
						
						int indexOfWeek = getWeek(date);//最大周
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
						int indexOfMon = getMon(date);//最大月
						if(indexOfMon != -1) {
							feeByMon[indexOfMon] += fee;
						}
					}else {
						//如果不是第一次进，需要将上一步计算结果写入文件
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
						sumFee = 0;//三个月的总消费金额
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
		System.out.println("******************数据计算完毕*************");
	}
	
	//获取平均值
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
	
	//获取标准差
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
    
    //获取周消费最高额
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
	 * 计算每辆车6-8月份总通行费用和次数，以及每周消费金额，周均，周方差（其中周均和周方差忽略掉了周消费为0的情况，不计入计算）
	 * @param inPath 2018(6-8)_feeByVeh.csv文件 结果字段：①车牌号；②车型；③日期；④收费金额  
	 * @param outPath 结果文件：车牌号,总通行费，通行次数,第1周费用,第2周费用,第3周费用,第4周费用,第5周费用,第6周费用,第7周费用,第8周费用,第9周费用,第10周费用,第11周费用,第12周费用,周均,周方差
	 */
	public static void calFeeByWeek(String inPath, String outPath) {
		DecimalFormat df2 = new DecimalFormat("###.##");//这样为保持2位

		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("车牌号,总通行费,通行次数,"
					+ "第1周费用,第2周费用,第3周费用,第4周费用,第5周费用,第6周费用,第7周费用,第8周费用,第9周费用,第10周费用,第11周费用,第12周费用,周均,周标准差,周最大\n");

			String line = reader.readLine();
			String[] lineArray;
			String curPlate = "";
			int sumTrip = 0;//以车为单位，计算出行次数
			long sumFee = 0;//三个月的总消费金额
			
			long[] feeByWeek = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 4) {
					
					String plate = lineArray[0].trim();//车牌号
					String date = formatDate(lineArray[2]);//日期
					long fee = Long.parseLong(lineArray[3]);//消费金额
					
					if(fee == 0) {//剔掉消费金额为0的记录
						continue;
					}
					
					if(curPlate.equals(plate)) {//研究同一车牌						
						
						if(isInPeriod(date)){
							sumFee += fee;
							sumTrip++;
						}
						
						int indexOfWeek = getWeek(date);//计算每个周的费用
						if(indexOfWeek != -1) {
							feeByWeek[indexOfWeek] += fee;
						}
						
					}else {
						//如果不是第一次进，需要将上一步计算结果写入文件
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
						sumFee = 0;//三个月的总消费金额
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
		System.out.println("******************数据计算完毕*************");
	}
	
	
	public static void main(String[] args) {
		//7-26号问题：考虑是否保留小数点，以及单位是元或者分的问题：不保留小数点，单位是元
		calFeeIndex(feeByDayPath, feeStatisticsResPath);
	}

}
