package programByDemand;

import java.awt.geom.FlatteningPathIterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import dataProcess.CalFeeIndex;
import dataProcess.DataPrePro;

public class GetAbnormalPlateByTimeConflict {
	
	private static String exTransDataPath = "/home/highwaytransaction/extransaction";
	private static String abnormalDataPath = "/home/pq/truckFeeAnalysis/异常数据/abnormalPlateRec.csv";
	private static String abnormalPlatePath = "/home/pq/truckFeeAnalysis/异常数据/日最高消费超3万.csv";
	private static String abnormalPlateResPath = "/home/pq/truckFeeAnalysis/异常数据/存在冲突时间车牌.csv";
	
	private static String abnormalDataPath_1 = "I:\\programData\\truckFee\\异常数据\\test\\abnormalPlateRec(3万).csv";
	private static String abnormalPlatePath_1 = "I:\\programData\\truckFee\\异常数据\\日最高消费超3万.csv";
	private static String abnormalRecResPath_1 = "I:\\programData\\truckFee\\异常数据\\存在冲突时间记录.csv";
	private static String feeStatisticsByWeekResSortedPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted.csv"; 
	private static String feeStatisticsByWeekResSortedNewPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted_new.csv";
	private static String abnormalPlateResPath_1 = "I:\\programData\\truckFee\\异常数据\\test\\存在冲突时间车牌.csv";
	
	public static Map<String, String> getAbnormalPlateMap(String path) {
		Map<String, String> plateMap = new HashMap<>();//存放所有日消费金额超过3万的车牌
		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				plateMap.put(line, "");
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return plateMap;
	}
	
	public static void writeData(String path, Map<String, ArrayList<String>> dataMap) {
		// 写文件
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			//writer.write("交易编号,记账时间,交易金额,入口车道编号,入口交易时间,通行卡ID,用户卡编号,OBU编号,入口实际收费车牌,出口实际收费车牌,出口识别车牌\n");
			writer.write("交易编号,交易金额,入口车道编号,入口交易时间,通行卡ID,出口实际收费车牌\n");
			for (String key : dataMap.keySet()) {
				ArrayList<String> recList = dataMap.get(key);
				
				for(String rec : recList) {
					writer.write(rec + "\n");
				}
				writer.flush();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(path + "  writed !");
	}
	
	/**
	 * 根据给定的日收入超过3万的车牌数据，从2018年6-8月份出口收费数据中筛选有问题车辆的出行记录
	 * @param inPath 2018年出口收费数据
	 * @param outPath 问题车辆的出口收费记录
	 * @param abnormalPlatePath	日收入超3万的所有车牌
	 */
	public static void getAbnormalPlateByTimeConflict(String inPath, String outPath, String abnormalPlatePath) {
		Map<String, ArrayList<String>> abnormalRecMap = new HashMap<String, ArrayList<String>>();//以车为单位，用以存放异常车牌的所有通行记录
		Map<String, String> plateMap = getAbnormalPlateMap(abnormalPlatePath);//存放所有日消费金额超过3万的车牌
		System.out.println(plateMap.size());
		
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//依次处理每一个文件夹
				if(list.get(i).length() != 7 || Integer.parseInt(list.get(i).substring(6)) < 6) {
					continue;
				}
				
				String path = inPath + "/" + list.get(i);
				File fileIn=new File(path);
				List<String> listIn=Arrays.asList(fileIn.list());
				
				for(int j = 0;j < listIn.size(); j++){
					//依次处理每一个文件
					String pathIn=path+"/"+listIn.get(j);
					
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;
	
					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String plate = lineArray[12].trim();//车牌号
							String exVehType = lineArray[15].trim();//出口收费车型
							String date = CalFeeIndex.formatDate(lineArray[1].trim().substring(21, 29));//日期
							
							if(exVehType == null || exVehType.equals("") || Integer.parseInt(exVehType) <= 11 || !CalFeeIndex.isInPeriod(date)) {
								continue;
							}
							
							if(plateMap.containsKey(plate)) {
								
								String rec = lineArray[1]+","+lineArray[2]+","+lineArray[3]+","+lineArray[5]+","+lineArray[6]
										+","+lineArray[7]+","+lineArray[8]+","+lineArray[10]+","+lineArray[11]+","+lineArray[12]
										+","+lineArray[13];
								
								if(abnormalRecMap.containsKey(plate)) {
									ArrayList<String> recList = abnormalRecMap.get(plate);
									recList.add(rec);
									abnormalRecMap.put(plate, recList);
								}else {
									ArrayList<String> recList = new ArrayList<>();
									recList.add(rec);
									abnormalRecMap.put(plate, recList);
								}

							}else {
								continue;
							}
	
						}
					}
					reader.close();	
					System.out.println(pathIn + " read finish!");
				}
			}
			writeData(outPath, abnormalRecMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
	}

	
	/**
	 * 按诸老师要求，从日收入超过3万的车牌扩充到所有车牌，对所有车牌进行时间冲突检测
	 * 根据给定的日收入超过3万的车牌数据，从2018年6-8月份出口收费数据中筛选有问题车辆的出行记录
	 * @param inPath 2018年出口收费数据
	 * @param outPath 问题车辆的出口收费记录
	 * @param abnormalPlatePath	日收入超3万的所有车牌
	 */
	public static void getAbnormalPlateByTimeConflictAll(String inPath, String outPath) {
		Map<String, ArrayList<String>> abnormalRecMap = new HashMap<String, ArrayList<String>>();//以车为单位，用以存放异常车牌的所有通行记录
		
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//依次处理每一个文件夹
				if(list.get(i).length() != 7 || Integer.parseInt(list.get(i).substring(6)) < 7) {
					continue;
				}
				
				String path = inPath + "/" + list.get(i);
				File fileIn=new File(path);
				List<String> listIn=Arrays.asList(fileIn.list());
				
				for(int j = 0;j < listIn.size(); j++){
					//依次处理每一个文件
					String pathIn=path+"/"+listIn.get(j);
					
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;
	
					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String plate = lineArray[12].trim();//车牌号
							String exVehType = lineArray[15].trim();//出口收费车型
							String date = CalFeeIndex.formatDate(lineArray[1].trim().substring(21, 29));//日期
							
							if(exVehType == null || exVehType.equals("") || Integer.parseInt(exVehType) <= 11 || !CalFeeIndex.isInPeriod(date)) {
								continue;
							}
							
							//需要对车牌进行筛选，否则容易进行很多额外的工作。需要筛掉的目标车牌：默认车牌，异常车牌
							if((!DataPrePro.isLetterDigitOrChineseUnderLine(plate)) || !DataPrePro.isNormalPlate(plate)) {
								continue;
							}
								
							String rec = lineArray[1]+","+lineArray[3]+","+lineArray[5]+","+lineArray[6]+","+lineArray[7]+","+lineArray[12];
							
							if(abnormalRecMap.containsKey(plate)) {
								ArrayList<String> recList = abnormalRecMap.get(plate);
								recList.add(rec);
								abnormalRecMap.put(plate, recList);
							}else {
								ArrayList<String> recList = new ArrayList<>();
								recList.add(rec);
								abnormalRecMap.put(plate, recList);
							}
						}
					}
					reader.close();	
					System.out.println(pathIn + " read finish!");
				}
			}
			writeData(outPath, abnormalRecMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
	}
	
	public static boolean isInTimeInterval(String inStation, String outStation, String curTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean flag = false;
		try {
			Date inDate = sdf.parse(inStation);
			Date outDate = sdf.parse(outStation);
			Date curDate = sdf.parse(curTime);
			
			//计算出的时间差额单位为秒
			long ab_cur_in = (curDate.getTime() - inDate.getTime())/(long)1000;
			long ab_out_cur = (outDate.getTime() - curDate.getTime())/(long)1000;
			
			if(ab_cur_in >= 0 && ab_out_cur >= 0) {
				flag = true;
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return flag;
	}
	
	public static String formateDate(String s) {
		String res = "";
		if(s.indexOf("T") != -1) {
			//说明是入站时间 2018-06-10T19:16:05
			s = s.replace("T", " ");
			//System.out.println(s);
			res = s;
		}else {
			//说明是出站时间 20180610200538
			StringBuilder resBuilder = new StringBuilder(s);
			resBuilder.insert(12, ":").insert(10, ":").insert(8, " ").insert(6, "-").insert(4, "-");
			res = resBuilder.toString();
		}
		return res;
	}
	
	public static void getConflictPlate(Map<String, ArrayList<String>> dataMap, String path) { 
		
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			//writer.write("车牌,冲突记录1_入,冲突记录1_出,冲突记录2_入,冲突记录2_出,冲突记录1_入,冲突记录1_出,冲突记录2_入,冲突记录2_出,冲突记录1_入,冲突记录1_出,冲突记录2_入,冲突记录2_出\n");

			for (String key : dataMap.keySet()) {
				String confRec = "";
				int num = 0;
				boolean flag = false;
				
				ArrayList<String> timeList = dataMap.get(key);
				int len = timeList.size();
				for(int i = 0; i < (len-1); i++) {
					String inStation = timeList.get(i).split(",")[0];
					String outStation = timeList.get(i).split(",")[1];
					
					for(int j = i + 1; j < len; j++) {
						String curTime_1 = timeList.get(j).split(",")[0];
						String curTime_2 = timeList.get(j).split(",")[1];
						
						if(isInTimeInterval(inStation, outStation, curTime_1) || isInTimeInterval(inStation, outStation, curTime_2)) {
							//说明存在冲突
							confRec += timeList.get(i) + "," + timeList.get(j) + ",";
							num++;
							if(num == 3) {
								int l = confRec.length();
								writer.write(key + "\n");
								//writer.write(key + "," + confRec.substring(0, (l-1)) + "\n");
								flag = true;
								num = 0;
								confRec = "";
								break;
							}
						}
					}
					if(flag) {
						flag = false;
						break;
					}
				}

			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 根据上一步筛选出的日消费总额超过3万的所有车辆出行记录，检测同一车牌出行记录是否存在出行时间冲突的数据，若冲突数据超过三条，被判断为异常数据提取出来。
	 * @param inPath 日收入超3万的所有车牌的出行记录
	 * @param outPath	输出文件内容：输入的出行记录存在时间冲突的所有车牌，每条车牌后跟了三条存在冲突的出入站时间数据
	 */
	public static void detectTimeConflict(String inPath, String outPath) {
		Map<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();//以车牌为ID，以进入站时间作为value
		try {
			
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();
			String[] lineArray;
			
			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 11) {
					String plate = lineArray[9].trim();//车牌号
					String outDate = formateDate(lineArray[0].trim().substring(21, 35));//出站时间
					String inDate = formateDate(lineArray[4]);//入站时间
					String dt = inDate.substring(0,10);
					if(!CalFeeIndex.isInPeriod(dt)) {
						continue;
					}
					
					if(timeMap.containsKey(plate)) {
						ArrayList<String> timeList = timeMap.get(plate);
						timeList.add(inDate + "," + outDate);
						timeMap.put(plate, timeList);
					}else {
						ArrayList<String> timeList = new ArrayList<>();
						timeList.add(inDate + "," + outDate);
						timeMap.put(plate, timeList);
					}
				}
			}
			System.out.println("车牌总数" + timeMap.size());
			reader.close();	
			System.out.println(inPath + " read finish!");
			
			getConflictPlate(timeMap, outPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
	}
	
	
	/**
	 * 按诸老师要求，从日收入超过3万的车牌扩充到所有车牌，对所有车牌进行时间冲突检测
	 * 根据上一步筛选出的日消费总额超过3万的所有车辆出行记录，检测同一车牌出行记录是否存在出行时间冲突的数据，若冲突数据超过三条，被判断为异常数据提取出来。
	 * @param inPath 日收入超3万的所有车牌的出行记录
	 * @param outPath	输出文件内容：输入的出行记录存在时间冲突的所有车牌，每条车牌后跟了三条存在冲突的出入站时间数据
	 */
	public static void detectTimeConflictAll(String inPath, String outPath) {
		Map<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();//以车牌为ID，以进入站时间作为value
		try {
			
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 6) {

					String plate = lineArray[5].trim();//车牌号
					String outDate = formateDate(lineArray[0].trim().substring(21, 35));//出站时间
					String inDate = formateDate(lineArray[3]);//入站时间
					String dt = inDate.substring(0,10);
					if(!CalFeeIndex.isInPeriod(dt)) {
						continue;
					}
					
					if(timeMap.containsKey(plate)) {
						ArrayList<String> timeList = timeMap.get(plate);
						timeList.add(inDate + "," + outDate);
						timeMap.put(plate, timeList);
					}else {
						ArrayList<String> timeList = new ArrayList<>();
						timeList.add(inDate + "," + outDate);
						timeMap.put(plate, timeList);
					}
				}
			}
			reader.close();	
			System.out.println(inPath + " read finish!");
			
			getConflictPlate(timeMap, outPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
	}
	
	
	/**
	 * 根据上一步提取到的存在冲突出行时间的车牌信息，更新feeStatisticsByWeekResSorted.csv文件，筛掉查出来的异常记录。
	 * @param inPath feeStatisticsByWeekResSorted.csv文件路径
	 * @param outPath feeStatisticsByWeekResSorted_new.csv文件路径，即更新后的文件
	 * @param abnormalPlateFilePath 存储存在冲突出行时间的车牌信息文件
	 */
	public static void updateWeekFeeSorted(String inPath, String outPath, String abnormalPlateFilePath) {
		Map<String, String> plateMap = getAbnormalPlateMap(abnormalPlateFilePath);
		
		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);

			String line = reader.readLine();
			writer.write(line + "\n");
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 18) {

					String plate = lineArray[0].trim();//车牌号
					if(plateMap.containsKey(plate)) {
						continue;
					}
					writer.write(line + "\n");
				}
			}
			reader.close();	
			writer.flush();
			writer.close();
			System.out.println(inPath + " read finish!");

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
	}
	
	public static void main(String[] args) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.out.println(df.format(new Date()) + "数据开始处理");// new Date()为获取当前系统时间
		//给定2018年6-8月份出口交易数据，给出日消费额超过3万的车牌，筛选出异常车牌在该期间的出行记录
		//getAbnormalPlateByTimeConflict(exTransDataPath, abnormalDataPath, abnormalPlatePath);
		//getAbnormalPlateByTimeConflictAll(exTransDataPath, abnormalDataPath);
		
		//根据上一步筛选出的出行记录，检测同一车牌出行记录是否存在出行时间冲突的数据，若冲突数据超过三条，被判断为异常数据提取出来。
		//detectTimeConflict(abnormalDataPath_1, abnormalPlateResPath_1);
		detectTimeConflictAll(abnormalDataPath, abnormalPlateResPath);
		
		//根据上一步提取到的存在冲突出行时间的车牌信息，更新feeStatisticsByWeekResSorted.csv文件，筛掉查出来的异常记录。
		//updateWeekFeeSorted(feeStatisticsByWeekResSortedPath, feeStatisticsByWeekResSortedNewPath, abnormalPlateResPath);
		//updateWeekFeeSorted(feeStatisticsByWeekResSortedPath, feeStatisticsByWeekResSortedNewPath, abnormalPlateResPath_1);
		
		
		//test
//		Map<String, String> plateMap = getAbnormalPlateMap(abnormalPlatePath_1);//存放所有日消费金额超过3万的车牌
//		System.out.println(plateMap.size());
//		if(plateMap.containsKey("鲁BY0336_1")) {
//			System.out.println("鲁BY0336_1");
//		}
//		if(plateMap.containsKey("川J53226_1")) {
//			System.out.println("川J53226_1");
//		}
//		
//		Map<String, String> plateMap2 = getAbnormalPlateMap(abnormalPlateResPath);
//		System.out.println(plateMap2.size());
//		if(plateMap2.containsKey("皖KJ8103_1")) {
//			System.out.println("皖KJ8103_1");
//		}
		
		System.out.println(df.format(new Date()) + "数据处理结束");// new Date()为获取当前系统时间
	}

}
