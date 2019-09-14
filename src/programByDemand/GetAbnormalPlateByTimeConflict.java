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
	private static String abnormalDataPath = "/home/pq/truckFeeAnalysis/�쳣����/abnormalPlateRec.csv";
	private static String abnormalPlatePath = "/home/pq/truckFeeAnalysis/�쳣����/��������ѳ�3��.csv";
	private static String abnormalPlateResPath = "/home/pq/truckFeeAnalysis/�쳣����/���ڳ�ͻʱ�䳵��.csv";
	
	private static String abnormalDataPath_1 = "I:\\programData\\truckFee\\�쳣����\\test\\abnormalPlateRec(3��).csv";
	private static String abnormalPlatePath_1 = "I:\\programData\\truckFee\\�쳣����\\��������ѳ�3��.csv";
	private static String abnormalRecResPath_1 = "I:\\programData\\truckFee\\�쳣����\\���ڳ�ͻʱ���¼.csv";
	private static String feeStatisticsByWeekResSortedPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted.csv"; 
	private static String feeStatisticsByWeekResSortedNewPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted_new.csv";
	private static String abnormalPlateResPath_1 = "I:\\programData\\truckFee\\�쳣����\\test\\���ڳ�ͻʱ�䳵��.csv";
	
	public static Map<String, String> getAbnormalPlateMap(String path) {
		Map<String, String> plateMap = new HashMap<>();//������������ѽ���3��ĳ���
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
		// д�ļ�
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			//writer.write("���ױ��,����ʱ��,���׽��,��ڳ������,��ڽ���ʱ��,ͨ�п�ID,�û������,OBU���,���ʵ���շѳ���,����ʵ���շѳ���,����ʶ����\n");
			writer.write("���ױ��,���׽��,��ڳ������,��ڽ���ʱ��,ͨ�п�ID,����ʵ���շѳ���\n");
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
	 * ���ݸ����������볬��3��ĳ������ݣ���2018��6-8�·ݳ����շ�������ɸѡ�����⳵���ĳ��м�¼
	 * @param inPath 2018������շ�����
	 * @param outPath ���⳵���ĳ����շѼ�¼
	 * @param abnormalPlatePath	�����볬3������г���
	 */
	public static void getAbnormalPlateByTimeConflict(String inPath, String outPath, String abnormalPlatePath) {
		Map<String, ArrayList<String>> abnormalRecMap = new HashMap<String, ArrayList<String>>();//�Գ�Ϊ��λ�����Դ���쳣���Ƶ�����ͨ�м�¼
		Map<String, String> plateMap = getAbnormalPlateMap(abnormalPlatePath);//������������ѽ���3��ĳ���
		System.out.println(plateMap.size());
		
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//���δ���ÿһ���ļ���
				if(list.get(i).length() != 7 || Integer.parseInt(list.get(i).substring(6)) < 6) {
					continue;
				}
				
				String path = inPath + "/" + list.get(i);
				File fileIn=new File(path);
				List<String> listIn=Arrays.asList(fileIn.list());
				
				for(int j = 0;j < listIn.size(); j++){
					//���δ���ÿһ���ļ�
					String pathIn=path+"/"+listIn.get(j);
					
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;
	
					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String plate = lineArray[12].trim();//���ƺ�
							String exVehType = lineArray[15].trim();//�����շѳ���
							String date = CalFeeIndex.formatDate(lineArray[1].trim().substring(21, 29));//����
							
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
		System.out.println("******************���ݴ������*************");
	}

	
	/**
	 * ������ʦҪ�󣬴������볬��3��ĳ������䵽���г��ƣ������г��ƽ���ʱ���ͻ���
	 * ���ݸ����������볬��3��ĳ������ݣ���2018��6-8�·ݳ����շ�������ɸѡ�����⳵���ĳ��м�¼
	 * @param inPath 2018������շ�����
	 * @param outPath ���⳵���ĳ����շѼ�¼
	 * @param abnormalPlatePath	�����볬3������г���
	 */
	public static void getAbnormalPlateByTimeConflictAll(String inPath, String outPath) {
		Map<String, ArrayList<String>> abnormalRecMap = new HashMap<String, ArrayList<String>>();//�Գ�Ϊ��λ�����Դ���쳣���Ƶ�����ͨ�м�¼
		
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//���δ���ÿһ���ļ���
				if(list.get(i).length() != 7 || Integer.parseInt(list.get(i).substring(6)) < 7) {
					continue;
				}
				
				String path = inPath + "/" + list.get(i);
				File fileIn=new File(path);
				List<String> listIn=Arrays.asList(fileIn.list());
				
				for(int j = 0;j < listIn.size(); j++){
					//���δ���ÿһ���ļ�
					String pathIn=path+"/"+listIn.get(j);
					
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;
	
					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String plate = lineArray[12].trim();//���ƺ�
							String exVehType = lineArray[15].trim();//�����շѳ���
							String date = CalFeeIndex.formatDate(lineArray[1].trim().substring(21, 29));//����
							
							if(exVehType == null || exVehType.equals("") || Integer.parseInt(exVehType) <= 11 || !CalFeeIndex.isInPeriod(date)) {
								continue;
							}
							
							//��Ҫ�Գ��ƽ���ɸѡ���������׽��кܶ����Ĺ�������Ҫɸ����Ŀ�공�ƣ�Ĭ�ϳ��ƣ��쳣����
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
		System.out.println("******************���ݴ������*************");
	}
	
	public static boolean isInTimeInterval(String inStation, String outStation, String curTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean flag = false;
		try {
			Date inDate = sdf.parse(inStation);
			Date outDate = sdf.parse(outStation);
			Date curDate = sdf.parse(curTime);
			
			//�������ʱ���λΪ��
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
			//˵������վʱ�� 2018-06-10T19:16:05
			s = s.replace("T", " ");
			//System.out.println(s);
			res = s;
		}else {
			//˵���ǳ�վʱ�� 20180610200538
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
			//writer.write("����,��ͻ��¼1_��,��ͻ��¼1_��,��ͻ��¼2_��,��ͻ��¼2_��,��ͻ��¼1_��,��ͻ��¼1_��,��ͻ��¼2_��,��ͻ��¼2_��,��ͻ��¼1_��,��ͻ��¼1_��,��ͻ��¼2_��,��ͻ��¼2_��\n");

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
							//˵�����ڳ�ͻ
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
	 * ������һ��ɸѡ�����������ܶ��3������г������м�¼�����ͬһ���Ƴ��м�¼�Ƿ���ڳ���ʱ���ͻ�����ݣ�����ͻ���ݳ������������ж�Ϊ�쳣������ȡ������
	 * @param inPath �����볬3������г��Ƶĳ��м�¼
	 * @param outPath	����ļ����ݣ�����ĳ��м�¼����ʱ���ͻ�����г��ƣ�ÿ�����ƺ�����������ڳ�ͻ�ĳ���վʱ������
	 */
	public static void detectTimeConflict(String inPath, String outPath) {
		Map<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();//�Գ���ΪID���Խ���վʱ����Ϊvalue
		try {
			
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();
			String[] lineArray;
			
			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 11) {
					String plate = lineArray[9].trim();//���ƺ�
					String outDate = formateDate(lineArray[0].trim().substring(21, 35));//��վʱ��
					String inDate = formateDate(lineArray[4]);//��վʱ��
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
			System.out.println("��������" + timeMap.size());
			reader.close();	
			System.out.println(inPath + " read finish!");
			
			getConflictPlate(timeMap, outPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************���ݴ������*************");
	}
	
	
	/**
	 * ������ʦҪ�󣬴������볬��3��ĳ������䵽���г��ƣ������г��ƽ���ʱ���ͻ���
	 * ������һ��ɸѡ�����������ܶ��3������г������м�¼�����ͬһ���Ƴ��м�¼�Ƿ���ڳ���ʱ���ͻ�����ݣ�����ͻ���ݳ������������ж�Ϊ�쳣������ȡ������
	 * @param inPath �����볬3������г��Ƶĳ��м�¼
	 * @param outPath	����ļ����ݣ�����ĳ��м�¼����ʱ���ͻ�����г��ƣ�ÿ�����ƺ�����������ڳ�ͻ�ĳ���վʱ������
	 */
	public static void detectTimeConflictAll(String inPath, String outPath) {
		Map<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();//�Գ���ΪID���Խ���վʱ����Ϊvalue
		try {
			
			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
	
				lineArray = line.split(",");
				if(lineArray.length == 6) {

					String plate = lineArray[5].trim();//���ƺ�
					String outDate = formateDate(lineArray[0].trim().substring(21, 35));//��վʱ��
					String inDate = formateDate(lineArray[3]);//��վʱ��
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
		System.out.println("******************���ݴ������*************");
	}
	
	
	/**
	 * ������һ����ȡ���Ĵ��ڳ�ͻ����ʱ��ĳ�����Ϣ������feeStatisticsByWeekResSorted.csv�ļ���ɸ����������쳣��¼��
	 * @param inPath feeStatisticsByWeekResSorted.csv�ļ�·��
	 * @param outPath feeStatisticsByWeekResSorted_new.csv�ļ�·���������º���ļ�
	 * @param abnormalPlateFilePath �洢���ڳ�ͻ����ʱ��ĳ�����Ϣ�ļ�
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

					String plate = lineArray[0].trim();//���ƺ�
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
		System.out.println("******************���ݴ������*************");
	}
	
	public static void main(String[] args) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
		System.out.println(df.format(new Date()) + "���ݿ�ʼ����");// new Date()Ϊ��ȡ��ǰϵͳʱ��
		//����2018��6-8�·ݳ��ڽ������ݣ����������Ѷ��3��ĳ��ƣ�ɸѡ���쳣�����ڸ��ڼ�ĳ��м�¼
		//getAbnormalPlateByTimeConflict(exTransDataPath, abnormalDataPath, abnormalPlatePath);
		//getAbnormalPlateByTimeConflictAll(exTransDataPath, abnormalDataPath);
		
		//������һ��ɸѡ���ĳ��м�¼�����ͬһ���Ƴ��м�¼�Ƿ���ڳ���ʱ���ͻ�����ݣ�����ͻ���ݳ������������ж�Ϊ�쳣������ȡ������
		//detectTimeConflict(abnormalDataPath_1, abnormalPlateResPath_1);
		detectTimeConflictAll(abnormalDataPath, abnormalPlateResPath);
		
		//������һ����ȡ���Ĵ��ڳ�ͻ����ʱ��ĳ�����Ϣ������feeStatisticsByWeekResSorted.csv�ļ���ɸ����������쳣��¼��
		//updateWeekFeeSorted(feeStatisticsByWeekResSortedPath, feeStatisticsByWeekResSortedNewPath, abnormalPlateResPath);
		//updateWeekFeeSorted(feeStatisticsByWeekResSortedPath, feeStatisticsByWeekResSortedNewPath, abnormalPlateResPath_1);
		
		
		//test
//		Map<String, String> plateMap = getAbnormalPlateMap(abnormalPlatePath_1);//������������ѽ���3��ĳ���
//		System.out.println(plateMap.size());
//		if(plateMap.containsKey("³BY0336_1")) {
//			System.out.println("³BY0336_1");
//		}
//		if(plateMap.containsKey("��J53226_1")) {
//			System.out.println("��J53226_1");
//		}
//		
//		Map<String, String> plateMap2 = getAbnormalPlateMap(abnormalPlateResPath);
//		System.out.println(plateMap2.size());
//		if(plateMap2.containsKey("��KJ8103_1")) {
//			System.out.println("��KJ8103_1");
//		}
		
		System.out.println(df.format(new Date()) + "���ݴ������");// new Date()Ϊ��ȡ��ǰϵͳʱ��
	}

}
