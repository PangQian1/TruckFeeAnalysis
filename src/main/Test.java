package main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.LongPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	
	public static boolean isLetterDigitOrChineseUnderLine(String str) {
		  String regex = "^[_a-z0-9A-Z\u4e00-\u9fa5]+$";//其他需要，直接修改正则表达式就好
		  return str.matches(regex);
		 }
	
	public static String formatDate(String date) {
		StringBuilder resBuilder = new StringBuilder(date);
		resBuilder.insert(6, "-").insert(4, "-");
		String res = resBuilder.toString();
		return res;
	}
	
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    
	//获取平均值
	public static double getAverage(long[] arr){
        long sum = 0;
        int len = arr.length;
        int num = 0;
        for(int i = 1;i < len-1;i++){
        	if(arr[i] != 0) {
                sum += arr[i];
                num++;
        	}
        }
        double ave = (double)sum / num;
        return ave;
    }
	
	//获取方差
    public static double getVariance(long[] arr, double ave){
        double sum = 0;
        int len = arr.length;
        int num = 0;
        for(int i = 1;i < len-1;i++){
        	if(arr[i] != 0) {
        		sum += ((double)arr[i] - ave) * (arr[i] - ave);
        		num++;
        	}
        }
        
        return (sum / (num-1));
    }
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
	}

}
