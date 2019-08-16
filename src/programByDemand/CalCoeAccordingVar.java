package programByDemand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;


public class CalCoeAccordingVar {
	
	private static String feeStatisticsByWeekResSortedPath = "I:\\programData\\truckFee\\feeStatisticsByWeekResSorted_new.csv"; 
	private static String clusterByCoeFilePath = "I:\\programData\\truckFee\\聚类分析文件\\feeStatisticsSortedWithCoe.csv";
	
	/**
	 * 根据按周统计的货车收费数据文件（数据依据周最大消费降序排序），计算相关指标：周均/周标准差，周最大/周标准差，有几个周有出行数据（根据需要择取指标）
	 * @param inPath feeStatisticsByWeekResSorted.csv文件，即根据按周统计的货车收费数据文件（数据依据周最大消费降序排序）
	 * @param outPath 指标计算结果
	 */
	public static void calCoeAccordingVar(String inPath, String outPath){
		DecimalFormat df = new DecimalFormat("###.##");//保留两位小数
		try {
			
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8");
			BufferedWriter writer = new BufferedWriter(writerStream);

			InputStreamReader inStream = new InputStreamReader(new FileInputStream(inPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inStream);

			String line = reader.readLine();
			writer.write(line + ",");
			writer.write("周最大/周标准差,周均/周标准差,出行周数,总出行次数" + "\n");
			String[] lineArray;

			while ((line = reader.readLine()) != null) {
				
				lineArray = line.split(",");
				if(lineArray.length == 18) {
					double weekAve = Double.parseDouble(lineArray[15]);
					double weekVar = Double.parseDouble(lineArray[16]);
					double weekMax = Double.parseDouble(lineArray[17]);
					
					if(weekVar == 0){
						continue;
					}
					
					int weekNum = 0;
					for(int i = 3; i < 15; i++) {
						if(!lineArray[i].equals("0")) {
							weekNum++;
						}
					}
					
					String aveDiviVar = df.format(weekAve/weekVar);
					String maxDiviVar = df.format(weekMax/weekVar);
					
					writer.write(line + ",");
					writer.write(maxDiviVar + "," + aveDiviVar);
					writer.write("," + weekNum + "," + lineArray[2]);
					writer.write("\n");
				}
			}
			reader.close();	
			System.out.println(inPath + " read finish!");
	
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("******************数据处理结束**********************");
	}

	public static void main(String[] args) {
		calCoeAccordingVar(feeStatisticsByWeekResSortedPath, clusterByCoeFilePath);

	}

}
