package dataProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class IndexStatistical {
	
	private static String feeStatisticsResPath = "I:\\programData\\truckFee\\feeStatisticsRes.csv";
	private static String indexStatPath = "I:\\programData\\truckFee\\指标统计中间结果\\指标统计结果csv\\indexStat";
	
	public static void writeData(String path, Map<Integer, Integer> dataMap, int index) {
		// 写文件
		path += "_" + getName(index) + ".csv";
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("消费金额区间," + getName(index) + "\n");
			
			for (Integer key : dataMap.keySet()) {
				writer.write(key + "," + dataMap.get(key));
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(path + "  writed !");
	}
	
	public static String getName(int index) {
		if(index == 1)return "dayAve";
		if(index == 2)return "weekAve";
		if(index == 3)return "monAve";
		if(index == 4)return "dayMax";
		if(index == 5)return "weekMax";
		if(index == 6)return "monMax";
		return "error";
	}
	
	public static Map<Integer, Integer> indexStat(String inPath, String outPath, int index) {
		
		Map<Integer, Integer> dataMap = new HashMap<Integer, Integer>();//消费区间，车辆数
		int interval = 1000;//大指标
		if(index == 1 || index == 4) {
			interval = 100;
		}
		
		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");

			String line = reader.readLine();
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 7) {
					
					int dayAve = (Integer.parseInt(lineArray[index])/interval)*interval + interval;//日均
if(index == 4 && dayAve > 30000)System.out.println(lineArray[0]);

					if(dataMap.containsKey(dayAve)) {
						int count = dataMap.get(dayAve);
						count ++;
						dataMap.put(dayAve, count);
					}else {
						dataMap.put(dayAve, 1);
					}				
				}
			}
			reader.close();	
			System.out.println(inPath + " read finish!");
			
			writeData(outPath, dataMap, index);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataMap;
	}

	public static void main(String[] args) {
//		for(int i = 1; i < 7; i++) {
//			indexStat(feeStatisticsResPath, indexStatPath, i);
//		}	
		
		indexStat(feeStatisticsResPath, indexStatPath, 4);
	}

}
