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
	
	//������������ѽ������ÿ����¼����
	public static void sortByMaxFee(String inPath, String outPath) {
		Map<String, ArrayList<String>> vehStatDataMap = new HashMap<String, ArrayList<String>>();//����������ѽ��ΪID����¼�������ݹ��ɵ�listΪvalue
		int[] maxFee = new int[12000000];//��¼���м�¼�����������
		int index = 0;//��¼�ڼ�������
		
		try {
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);
			System.out.println(inPath + " start reading !");

			String line = reader.readLine();
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 18) {
					
					String fee = lineArray[17];//���������
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
		System.out.println("******************���ݶ�ȡ���*************");
	}
	
	public static void writeDataBySeq(Map<String, ArrayList<String>> dataMap, int[] arr, String path) {
		
		//���ȶ���������
		Arrays.sort(arr);
		
		int len = arr.length;
		
		// д�ļ�
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("���ƺ�,��ͨ�з�,ͨ�д���,"
					+ "��1�ܷ���,��2�ܷ���,��3�ܷ���,��4�ܷ���,��5�ܷ���,��6�ܷ���,��7�ܷ���,��8�ܷ���,��9�ܷ���,��10�ܷ���,��11�ܷ���,��12�ܷ���,�ܾ�,�ܱ�׼��,�����\n");

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
