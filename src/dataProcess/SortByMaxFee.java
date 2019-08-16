package dataProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SortByMaxFee {
	
	private static String feeStatisticsByWeekResPath = "I:\\programData\\truckFee\\feeStatisticsByWeekRes.csv"; 
	private static String feeStatisticsByWeekResSortedPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted.csv"; 
	
	//按照最大周消费金额来对每条记录排序
	public static void sortByMaxFee(String inPath, String outPath) {
		Map<String, ArrayList<String>> vehStatDataMap = new HashMap<String, ArrayList<String>>();//以最大周消费金额为ID，记录具体内容构成的list为value
		int[] maxFee = new int[12000000];//记录所有记录的最大周消费
		int index = 0;//记录第几条数据
		
		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");

			String line = reader.readLine();
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 18) {
					
					String fee = lineArray[17];//最大周消费
					maxFee[index] = Integer.parseInt(fee);
					index++;
					
					if(vehStatDataMap.containsKey(fee)) {
						ArrayList<String> recList = vehStatDataMap.get(fee);
						recList.add(line);
						vehStatDataMap.put(fee+"", recList);
					}else {
						ArrayList<String> recList = new ArrayList<>();
						recList.add(line);
						vehStatDataMap.put(fee+"", recList);
					}
					
				}
			}
			
			int[] maxFeeAll = new int[index];
			for(int i = 0; i < index; i++) {
				maxFeeAll[i] = maxFee[i];
			}
			
			writeDataBySeq(vehStatDataMap, maxFeeAll, outPath);
			
			reader.close();	
			System.out.println(inPath + " read finish!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据读取完毕*************");
	}
	
	public static void writeDataBySeq(Map<String, ArrayList<String>> dataMap, int[] arr, String path) {
		
		//首先对数组排序
		Arrays.sort(arr);
		
		int len = arr.length;
		
		// 写文件
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("车牌号,总通行费,通行次数,"
					+ "第1周费用,第2周费用,第3周费用,第4周费用,第5周费用,第6周费用,第7周费用,第8周费用,第9周费用,第10周费用,第11周费用,第12周费用,周均,周标准差,周最大\n");

			int cur = -1;
			for(int i = (len-1); i >= 0; i-- ) {
				if(arr[i] == cur) {
					continue;
				}
				cur = arr[i];
				ArrayList<String> feeList = dataMap.get(arr[i]+"");
				
				for(String rec : feeList) {
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
	

	public static void main(String[] args) {
		sortByMaxFee(feeStatisticsByWeekResPath, feeStatisticsByWeekResSortedPath);
	}

}
