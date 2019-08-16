package programByDemand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class GetAbnormalRec {
	
	private static String exTransDataPath = "/home/highwaytransaction/extransaction/2018-08";
	private static String abnormalDataPath = "/home/pq/truckFeeAnalysis/�쳣����/��GP903A_0�쳣����.csv";
	
	public static String formatDate(String date) {
		StringBuilder resBuilder = new StringBuilder(date);
		resBuilder.insert(6, "-").insert(4, "-");
		String res = resBuilder.toString();
		return res;
	}
	
	public static Boolean isInPeriod(String date) {
		Boolean flag = false;
		//�жϸ�ʱ���Ƿ���2018��8.6��8.12������
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String initDay = "2018-08-06";
		
		try {
			Date curDate = sdf.parse(date);
			Date initDate = sdf.parse(initDay);
			
			//һ��Ϊ604800��
			long timeInterval = (curDate.getTime() - initDate.getTime())/(long)1000;
			
			if(timeInterval >= 0 && timeInterval < 604800) {
				flag = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return flag;

	}
	
	public static void getAbnormalRec(String inPath, String outPath, String abnormalPlate) {
				
		try {
			
			File file = new File(inPath);
			List<String> list = Arrays.asList(file.list());	
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			
			for (int i = 0; i < list.size(); i++) {
				//���δ���ÿһ���ļ�	
				String dt = list.get(i).substring(0, 10);
				if(!isInPeriod(dt) && !dt.equals("2018-08-05") && !dt.equals("2018-08-13")) {
					continue;
				}
				String path = inPath + "/" + list.get(i);

				InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
				BufferedReader reader = new BufferedReader(inStream);

				String line = "";
				String[] lineArray;

				while ((line = reader.readLine()) != null) {
		
					lineArray = line.split(",");
					if(lineArray.length == 26) {
						String dealDate = formatDate(lineArray[1].trim().substring(21, 29));//����ʱ��
						String plate = lineArray[12].trim();//���ƺ�
						
						if(plate.equals(abnormalPlate) && isInPeriod(dealDate)) {
							writer.write(line + "\n");
						}

					}
				}
				reader.close();	
				System.out.println(path + " read finish!");
			}
		
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************����Ԥ�������*************");
	}

	public static void main(String[] args) {
		getAbnormalRec(exTransDataPath, abnormalDataPath, "��GP903A_0");
	}

}
