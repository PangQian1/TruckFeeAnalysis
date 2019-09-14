package programByDemand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataProcess.CalFeeIndex;

public class SparkSupport {
	
	public static void getInconsistPlate(String javaPath, String scalaPath, String outPath) {
		Map<String, String> javaRes = new HashMap<String, String>();
		Map<String, String> scalaRes = new HashMap<>();
		
		int count = 0;
		try {

			InputStreamReader inStream1 = new InputStreamReader(new FileInputStream(javaPath), "UTF-8");
			BufferedReader reader1 = new BufferedReader(inStream1);
			InputStreamReader inStream2 = new InputStreamReader(new FileInputStream(scalaPath), "UTF-8");
			BufferedReader reader2 = new BufferedReader(inStream2);
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			
			String line2 = "";

			while ((line2 = reader2.readLine()) != null) {
				scalaRes.put(line2, "1");
			}

			String line1 = "";

			while ((line1 = reader1.readLine()) != null) {
				//javaRes.put(line1, "1");
				if(!scalaRes.containsKey(line1)) {
					writer.write(line1 + "\n");
					count++;
				}
			}
			
			reader1.close();	
			reader2.close();
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(count);
		System.out.println("******************数据处理完毕*************");
	}
	
	public static void getRecords(String abnormalRecordPath, String inconsistPlatePath, String outPath) {
		Map<String, String> plateMap = new HashMap<String, String>();
		
		try {

			InputStreamReader inStream1 = new InputStreamReader(new FileInputStream(inconsistPlatePath), "UTF-8");
			BufferedReader reader1 = new BufferedReader(inStream1);
			InputStreamReader inStream2 = new InputStreamReader(new FileInputStream(abnormalRecordPath), "UTF-8");
			BufferedReader reader2 = new BufferedReader(inStream2);
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			
			String line1 = "";

			while ((line1 = reader1.readLine()) != null) {
				plateMap.put(line1, "1");
			}
			
			String line2 = "";
			String[] lineArray;

			while ((line2 = reader2.readLine()) != null) {
				lineArray = line2.split(",");
				if(lineArray.length == 6) {
					String plate = lineArray[5].trim();//车牌号
					if(plateMap.containsKey(plate)) {
						writer.write(line2 + "\n");
					}
					
				}

			}

			
			reader1.close();	
			reader2.close();
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理完毕*************");
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
						
						if(GetAbnormalPlateByTimeConflict.isInTimeInterval(inStation, outStation, curTime_1) || GetAbnormalPlateByTimeConflict.isInTimeInterval(inStation, outStation, curTime_2)) {
							//说明存在冲突
							confRec += timeList.get(i) + "," + timeList.get(j) + ",";
							num++;
							if(num == 3) {
								int l = confRec.length();
								//writer.write(key + "\n");
								writer.write(key + "," + confRec.substring(0, (l-1)) + "\n");
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

	public static void detectTimeConflictAll(String inPath, String outPath) {
		Map<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();//以车牌为ID，以进入站时间作为value
		try {
			
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();;
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 11) {

					String plate = lineArray[9].trim();//车牌号
					String outDate = GetAbnormalPlateByTimeConflict.formateDate(lineArray[0].trim().substring(21, 35));//出站时间
					String inDate = GetAbnormalPlateByTimeConflict.formateDate(lineArray[4]);//入站时间
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
	
	public static void main(String[] args) throws ParseException {
		//getInconsistPlate("I:\\programData\\scala\\存在冲突时间车牌.csv", "I:\\programData\\scala\\a.csv", "I:\\programData\\scala\\不一致车牌.csv");
		//getRecords("/home/pq/truckFeeAnalysis/异常数据/abnormalPlateRec.csv", "/home/pq/scala/不一致车牌.csv", "/home/pq/scala/不一致车牌出行记录.csv");
		detectTimeConflictAll("I:\\programData\\truckFee\\异常数据\\test\\abnormalPlateRec(3万).csv", "I:\\programData\\truckFee\\异常数据\\test\\存在冲突时间车牌.csv");
	}

}
