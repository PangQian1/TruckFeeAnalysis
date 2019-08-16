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
	 * 判断字符串是否由中文，26个大小写英文字母以及下划线构成
	 * @param str 待判断字符串
	 * @return 
	 */
	public static boolean isLetterDigitOrChineseUnderLine(String str) {
		  String regex = "^[_a-z0-9A-Z\u4e00-\u9fa5]+$";//其他需要，直接修改正则表达式就好
		  return str.matches(regex);
		 }
	
	
    /**
	     * 判断字符串中是否包含中文
     * @param str
	     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    
	public static boolean isNormalPlate(String plate) {//判断车牌是否是正常车牌
		boolean flag = true;
		
		//无_0，无车牌_0，参数错误1_5，无识别结果_0   新增未执行：整牌拒识，车牌_9
		if(plate.length() <= 3 || plate.indexOf("无") != -1 || plate.indexOf("错误") != -1 
				|| plate.indexOf("拒") != -1 || plate.indexOf("牌") != -1) {
			return false;
		}
		
		//9代表不确定车牌颜色
		int len = plate.length();
//		String color = plate.substring(len-1, len);
//		if(color.equals("9")) {
//			flag = false;
//		}
		
		//黑000000_0，浙111111_0
		String num_1 = plate.substring(1,len-2);
		if(num_1.equals("000000") || num_1.equals("0000000") || num_1.equals("111111") || num_1.equals("1111111") || 
				num_1.equals("123456") || num_1.equals("1234567")) {
			flag = false;
		}
		
		//沪A00000_0，皖A12345_0，浙A11111_0
		String num_2 = plate.substring(2, len-2);
		if(num_2.equals("00000") || num_2.equals("000000") || num_2.equals("11111") || num_2.equals("111111") ||
				num_2.equals("12345") || num_2.equals("123456")) {
			flag = false;
		}
		
		//默A000_0
		String chin = plate.substring(0, 1);
		if(chin.equals("默")) {
			flag = false;
		}
		
		//5555555_1，WPH4Z4A_0
		if(!isContainChinese(plate)) {
			flag = false;
		}
		
		return flag;
	}
	
	/**
	 * 基于全国高速出口交易数据2018年6-8月份收费数据进行数据筛选。筛选条件：车型大于11。结果字段：①车牌号码；②出口收费车型；③交易时间；④交易金额。每个月份对应一个文件
	 * @param inPath 2018年6-8月份收费数据
	 * @param outPath 结果字段：①车牌号码；②出口收费车型；③交易时间；④交易金额。每个月份对应一个文件
	 */
	public static void dataPrePro(String inPath, String outPath) {
		
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
				
				String pathOut = outPath + "/" + list.get(i);
				pathOut +=  ".csv";
				OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(pathOut), "utf-8");
				BufferedWriter writer = new BufferedWriter(writerStream);
				writer.write("车牌号,车型,日期,收费金额\n");
				
				for(int j = 0;j < listIn.size(); j++){
					//依次处理每一个文件
					String pathIn=path+"/"+listIn.get(j);
					
					// 读一行记录写一行记录
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(pathIn), "UTF-8");
					BufferedReader reader = new BufferedReader(inStream);

					String line = "";
					String[] lineArray;

					while ((line = reader.readLine()) != null) {
			
						lineArray = line.split(",");
						if(lineArray.length == 26) {

							String exVehType = lineArray[15].trim();//出口收费车型
							String plate = lineArray[12].trim();//车牌号
							
							if(exVehType != null && exVehType != "" && Integer.parseInt(exVehType) > 11 
									&& plate != null && plate != ""
									&& lineArray[1].length() == 37) {
					
								String dealDate = lineArray[1].trim().substring(21, 29);//交易时间
								String fee = lineArray[3].trim();//交易金额
								
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
		System.out.println("******************数据预处理完毕*************");
	}
	
	/**
	 * 以车为计算单位，计算每辆车的日消费金额，将同一辆车的记录放在一起，筛掉异常车牌以及默认车牌，结果字段：①车牌号；②日期；③日消费总金额
	 * @param inPath 字段：①车牌号码；②出口收费车型；③交易时间；④交易金额。每个月份对应一个文件，即上一步的结果文件
	 * @param outPath 2018(6-8)_feeByDay.csv文件，结果字段：①车牌号；②日期；③日消费总金额
	 */
	public static void calFeeByDay(String inPath, String outPath) {
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();//第一层map以车牌号为ID，第二层map以日期为ID，value为费用
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//依次处理每一个文件				
				String path = inPath + "/" + list.get(i);

				InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
				BufferedReader reader = new BufferedReader(inStream);

				String line = reader.readLine();
				String[] lineArray;

				while ((line = reader.readLine()) != null) {
		
					lineArray = line.split(",");
					if(lineArray.length == 4) {

						//String exVehType = lineArray[1].trim();//出口收费车型
						String plate = lineArray[0].trim();//车牌号
						String date = lineArray[2];//日期
						String fee = lineArray[3];//费用
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
		System.out.println("******************数据预处理完毕*************");
		
	}

	public static void writeData(String path, Map<String, Map<String, String>> dataMap) {
		// 写文件
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("车牌号,日期,收费金额\n");
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
	 * 以车为计算单位，筛掉不合格数据，并重新组织记录，将同一辆车的记录放在一起，结果字段：①车牌号；②车型；③日期；④收费金额  
	 * 与calFeeByDay方法的不同之处是未合并任何出行记录，calFeeByDay方法以日出行金额为单位，将一天记录进行了合并
	 * @param inPath 字段：①车牌号码；②出口收费车型；③交易时间；④交易金额。每个月份对应一个文件，即上一步的结果文件，与calFeeByDay方法输入文件一致
	 * @param outPath 2018(6-8)_feeByVeh.csv文件 结果字段：①车牌号；②车型；③日期；④收费金额  
	 */
	public static void calFeeByVeh(String inPath, String outPath) {
		DecimalFormat df = new DecimalFormat("###");//这样为保持0位
		
		Map<String, ArrayList<String>> dataMap = new HashMap<String,ArrayList<String>>();//以车牌号为ID，value为：①车型；②日期；③费用
		File file = new File(inPath);
		List<String> list = Arrays.asList(file.list());	
		
		try {
			
			for (int i = 0; i < list.size(); i++) {
				//依次处理每一个文件				
				String path = inPath + "/" + list.get(i);

				InputStreamReader inStream = new InputStreamReader(new FileInputStream(path), "UTF-8");
				BufferedReader reader = new BufferedReader(inStream);

				String line = reader.readLine();
				String[] lineArray;

				while ((line = reader.readLine()) != null) {
		
					lineArray = line.split(",");
					if(lineArray.length == 4) {

						//String exVehType = lineArray[1].trim();//出口收费车型
						String plate = lineArray[0].trim();//车牌号
						String vehType = lineArray[1];//车型
						String date = lineArray[2];//日期
						String fee = lineArray[3];//费用
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
		System.out.println("******************数据预处理完毕*************");
		
	}
	
	
	public static void writeDataByVeh(String path, Map<String, ArrayList<String>> dataMap) {
		// 写文件
		System.out.println(path + "  writing !");
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(path), "utf-8");
			BufferedWriter writer = new BufferedWriter(writerStream);
			writer.write("车牌号,车型,日期,收费金额\n");
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
