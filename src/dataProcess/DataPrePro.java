package dataProcess;

import java.awt.geom.FlatteningPathIterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataPrePro {
	
	private static String preProDataPath = "I:\\programData\\truckFee\\preProData";
	private static String feeByDayPath = "I:\\programData\\truckFee";
	
	/**
	 * �ж��ַ����Ƿ������ģ�26����СдӢ����ĸ�Լ��»��߹���
	 * @param str ���ж��ַ���
	 * @return 
	 */
	public static boolean isLetterDigitOrChineseUnderLine(String str) {
		  String regex = "^[_a-z0-9A-Z\u4e00-\u9fa5]+$";//������Ҫ��ֱ���޸�������ʽ�ͺ�
		  return str.matches(regex);
		 }
	
	
    /**
	     * �ж��ַ������Ƿ��������
     * @param str
	     * ��У���ַ���
     * @return �Ƿ�Ϊ����
     * @warn ����У���Ƿ�Ϊ���ı�����
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    
	public static boolean isNormalPlate(String plate) {//�жϳ����Ƿ�����������
		boolean flag = true;
		
		//��_0���޳���_0����������1_5����ʶ����_0   ����δִ�У����ƾ�ʶ������_9
		if(plate.length() <= 3 || plate.indexOf("��") != -1 || plate.indexOf("����") != -1 
				|| plate.indexOf("��") != -1 || plate.indexOf("��") != -1) {
			return false;
		}
		
		//9����ȷ��������ɫ
		int len = plate.length();
//		String color = plate.substring(len-1, len);
//		if(color.equals("9")) {
//			flag = false;
//		}
		
		//��000000_0����111111_0
		String num_1 = plate.substring(1,len-2);
		if(num_1.equals("000000") || num_1.equals("0000000") || num_1.equals("111111") || num_1.equals("1111111") || 
				num_1.equals("123456") || num_1.equals("1234567")) {
			flag = false;
		}
		
		//��A00000_0����A12345_0����A11111_0
		String num_2 = plate.substring(2, len-2);
		if(num_2.equals("00000") || num_2.equals("000000") || num_2.equals("11111") || num_2.equals("111111") ||
				num_2.equals("12345") || num_2.equals("123456")) {
			flag = false;
		}
		
		//ĬA000_0
		String chin = plate.substring(0, 1);
		if(chin.equals("Ĭ")) {
			flag = false;
		}
		
		//5555555_1��WPH4Z4A_0
		if(!isContainChinese(plate)) {
			flag = false;
		}
		
		return flag;
	}
	
	/**
	 * ����ȫ�����ٳ��ڽ�������2018��6-8�·��շ����ݽ�������ɸѡ��ɸѡ���������ʹ���11������ֶΣ��ٳ��ƺ��룻�ڳ����շѳ��ͣ��۽���ʱ�䣻�ܽ��׽�ÿ���·ݶ�Ӧһ���ļ�
	 * @param inPath 2018��6-8�·��շ�����
	 * @param outPath ����ֶΣ��ٳ��ƺ��룻�ڳ����շѳ��ͣ��۽���ʱ�䣻�ܽ��׽�ÿ���·ݶ�Ӧһ���ļ�
	 */
	public static void dataPrePro(String inPath, String outPath) {
		
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
				
				String pathOut = outPath + "/" + list.get(i);
				pathOut +=  ".csv";
				OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(pathOut), "utf-8");
				BufferedWriter writer = new BufferedWriter(writerStream);
				writer.write("���ƺ�,����,����,�շѽ��\n");
				
				for(int j = 0;j < listIn.size(); j++){
					//���δ���ÿһ���ļ�
					String pathIn=path+"/"+listIn.get(j);
					
					// ��һ�м�¼дһ�м�¼
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;

					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String exVehType = lineArray[15].trim();//�����շѳ���
							String plate = lineArray[12].trim();//���ƺ�
							
							if(exVehType != null && exVehType != "" && Integer.parseInt(exVehType) > 11 
									&& plate != null && plate != ""
									&& lineArray[1].length() == 37) {
					
								String dealDate = lineArray[1].trim().substring(21, 29);//����ʱ��
								String fee = lineArray[3].trim();//���׽��
								
								String res = plate + "," + exVehType + "," + dealDate + "," + fee + "\n";
						
								writer.write(res);
								
							}
							
						}
					}
					reader.close();	
					System.out.println(pathIn + " read finish!");
				}
				writer.flush();
				writer.close();
				System.out.println(pathOut + " write finish!");

			}
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************����Ԥ�������*************");
	}
	
	/**
	 * �Գ�Ϊ���㵥λ������ÿ�����������ѽ���ͬһ�����ļ�¼����һ��ɸ���쳣�����Լ�Ĭ�ϳ��ƣ�����ֶΣ��ٳ��ƺţ������ڣ����������ܽ��
	 * @param inPath �ֶΣ��ٳ��ƺ��룻�ڳ����շѳ��ͣ��۽���ʱ�䣻�ܽ��׽�ÿ���·ݶ�Ӧһ���ļ�������һ���Ľ���ļ�
	 * @param outPath 2018(6-8)_feeByDay.csv�ļ�������ֶΣ��ٳ��ƺţ������ڣ����������ܽ��
	 */
	public static void calFeeByDay(String inPath, String outPath) {
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();//��һ��map�Գ��ƺ�ΪID���ڶ���map������ΪID��valueΪ����
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//���δ���ÿһ���ļ�				
				String path = inPath + "/" + list.get(i);

				InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
				BufferedReader reader = new BufferedReader(inStream);

				String line = reader.readLine();
				String[] lineArray;

				while ((line = reader.readLine()) != null) {
		
					lineArray = line.split(",");
					if(lineArray.length == 4) {

						//String exVehType = lineArray[1].trim();//�����շѳ���
						String plate = lineArray[0].trim();//���ƺ�
						String date = lineArray[2];//����
						String fee = lineArray[3];//����
						int len = plate.length();
						
						if((!isLetterDigitOrChineseUnderLine(plate)) || !isNormalPlate(plate)) {
							continue;
						}
						
						if(dataMap.containsKey(plate)) {
							Map<String, String> feeByDayMap = dataMap.get(plate);
							if(feeByDayMap.containsKey(date)) {
								long ori = Long.parseLong(feeByDayMap.get(date));
								long sum = ori + Long.parseLong(fee);
								feeByDayMap.put(date, sum + "");
							}else {
								feeByDayMap.put(date, fee);
							}	
							
						}else {
							Map<String, String> feeByDayMap = new HashMap<>();
							feeByDayMap.put(date, fee);
							dataMap.put(plate, feeByDayMap);
						}
					}
				}
				reader.close();	
				System.out.println(path + " read finish!");
			}
		
			writeData(outPath + "/" + "2018(6-8)_feeByDay.csv", dataMap);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************����Ԥ�������*************");
		
	}

	public static void writeData(String path, Map<String, Map<String, String>> dataMap) {
		// д�ļ�
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("���ƺ�,����,�շѽ��\n");
			for (String key : dataMap.keySet()) {
				Map<String, String> feeByDataMap = dataMap.get(key);
				
				for(String date : feeByDataMap.keySet()) {
					String fee = feeByDataMap.get(date);
					if(!fee.equals("0")) {
						writer.write(key + "," + date + "," + fee);
						writer.write("\n");
					}
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
	 * �Գ�Ϊ���㵥λ��ɸ�����ϸ����ݣ���������֯��¼����ͬһ�����ļ�¼����һ�𣬽���ֶΣ��ٳ��ƺţ��ڳ��ͣ������ڣ����շѽ��  
	 * ��calFeeByDay�����Ĳ�֮ͬ����δ�ϲ��κγ��м�¼��calFeeByDay�������ճ��н��Ϊ��λ����һ���¼�����˺ϲ�
	 * @param inPath �ֶΣ��ٳ��ƺ��룻�ڳ����շѳ��ͣ��۽���ʱ�䣻�ܽ��׽�ÿ���·ݶ�Ӧһ���ļ�������һ���Ľ���ļ�����calFeeByDay���������ļ�һ��
	 * @param outPath 2018(6-8)_feeByVeh.csv�ļ� ����ֶΣ��ٳ��ƺţ��ڳ��ͣ������ڣ����շѽ��  
	 */
	public static void calFeeByVeh(String inPath, String outPath) {
		DecimalFormat df = new DecimalFormat("###");//����Ϊ����0λ
		
		Map<String, ArrayList<String>> dataMap = new HashMap<String,ArrayList<String>>();//�Գ��ƺ�ΪID��valueΪ���ٳ��ͣ������ڣ��۷���
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//���δ���ÿһ���ļ�				
				String path = inPath + "/" + list.get(i);

				InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
				BufferedReader reader = new BufferedReader(inStream);

				String line = reader.readLine();
				String[] lineArray;

				while ((line = reader.readLine()) != null) {
		
					lineArray = line.split(",");
					if(lineArray.length == 4) {

						//String exVehType = lineArray[1].trim();//�����շѳ���
						String plate = lineArray[0].trim();//���ƺ�
						String vehType = lineArray[1];//����
						String date = lineArray[2];//����
						String fee = lineArray[3];//����
						if(fee.equals("0")) {
							continue;
						}
						String vehFee = df.format(Double.parseDouble(fee)/100);
						
						if((!isLetterDigitOrChineseUnderLine(plate)) || !isNormalPlate(plate)) {
							continue;
						}
						
						if(dataMap.containsKey(plate)) {
							ArrayList<String> feeByDayList = dataMap.get(plate);
							String rec = vehType + "," + date + "," + vehFee;
							feeByDayList.add(rec);	
						}else {
							ArrayList<String> feeByDayList = new ArrayList<>();
							String rec = vehType + "," + date + "," + vehFee;
							feeByDayList.add(rec);
							dataMap.put(plate, feeByDayList);
						}
					}
				}
				reader.close();	
				System.out.println(path + " read finish!");
			}
		
			writeDataByVeh(outPath + "/" + "2018(6-8)_feeByVeh.csv", dataMap);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************����Ԥ�������*************");
		
	}
	
	
	public static void writeDataByVeh(String path, Map<String, ArrayList<String>> dataMap) {
		// д�ļ�
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("���ƺ�,����,����,�շѽ��\n");
			for (String key : dataMap.keySet()) {
				ArrayList<String> feeList = dataMap.get(key);
				
				for(String rec : feeList) {
					writer.write(key + "," + rec + "\n");
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
		calFeeByDay(preProDataPath, feeByDayPath);
	}

}
