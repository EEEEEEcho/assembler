package cn.hyg.client.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static String convertTime2Str(Date time) {
        if (time != null) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
        } else {
            return "";
        }
    }

    public static String upperCharToUnderLine(String param) {
        Pattern p = Pattern.compile("[A-Z]");
        if (param == null || param.equals("")) {
            return "";
        }
        StringBuilder builder = new StringBuilder(param);
        Matcher mc = p.matcher(param);
        int i = 0;
        while (mc.find()) {
            //System.out.println(builder.toString());
            //System.out.println("mc.start():" + mc.start() + ", i: " + i);
            //System.out.println("mc.end():" + mc.start() + ", i: " + i);
            builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase());
            i++;
        }

        if ('_' == builder.charAt(0)) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    public static boolean isDigital(String str) {
        return str.matches("^[0-9]*$");
    }

//    public static String extractStrainName(String fullName) {
//
//        if (StringUtils.isBlank(fullName)) {
//            return "";
//        }
//
//        String t = FileUtil.getFilenameWithoutSuffix(fullName);
//        if (t == null) {
//            return "";
//        }
//        if (t.contains("_1")) {
//            return t.split("_1")[0];
//        } else if (t.contains("_2")) {
//            return t.split("_2")[0];
//        } else {
//            return t;
//        }
//    }
    public static String extractStrainName(String fullName) {

        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        //获取没有后缀名的文件名
        String t = FileUtil.getFilenameWithoutSuffix(fullName);
        //提取菌种名，若是双端类型，肯定会存在以1 或者 2结尾，而且肯定会有下划线
        //例如_1 _2,_R1 _R2,_001 _002这种类型，而且是在文件名最末尾，
        //因此菌种名就是之前的部分
        //System.out.println("没有后缀名：" +  t); //S1_00002507_R2.fq //sm04_L309_BDM-1a_2.fq
        if (t == null) {
            return "";
        }
        //证明是双端类型，单端类型以这样的格式结尾的也可以使用。
        if((t.endsWith("1") && t.contains("_")) || (t.endsWith("2") && t.contains("_"))){
            return t.substring(0,t.lastIndexOf("_"));
        }

    //        String partten = "(.*)_(.*)(\\d+)\\.f(.*)";
    //        boolean isMatch = Pattern.matches(partten,t);
    //        System.out.println("isMatch:" + isMatch);
    //        if(isMatch){
    //            return t.substring(0,t.lastIndexOf("_"));
    //        }
        else {
            return t;
        }

    }
    public static String extractStrainNameForOne(String fullName) {

        if (StringUtils.isBlank(fullName)) {
            return "";
        }

        String t = FileUtil.getFilenameWithoutSuffix(fullName);
        //System.out.println(t);
        if (t == null) {
            return "";
        }
        if (t.contains("_1")) {
            return t.split("_1")[0] + "_1";
        } else if (t.contains("_2")) {
            return t.split("_2")[0] + "_2";
        } else {
            return t;
        }
    }
    public static String getDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }
    public static String makeFastaDir(String fastaDir,String outputFile){
        if(fastaDir == null || fastaDir.equals("") || fastaDir.length() == 0){
            fastaDir = outputFile + File.separator + "fasta" + File.separator + StringHelper.getDate();
        }
        else{
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            String[] dirs = fastaDir.split(pattern);
            if(! dirs[dirs.length - 1].equals(StringHelper.getDate())){
                fastaDir = "";
                for(int i=0;i < dirs.length - 1;i ++){
                    fastaDir += dirs[i] + File.separator;
                }
                fastaDir += StringHelper.getDate();
            }
        }
        return fastaDir;
    }
}
