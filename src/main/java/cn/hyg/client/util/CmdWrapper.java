package cn.hyg.client.util;

import cn.hyg.client.value.TaskType;


/**
 * 自动打包
 */
public class CmdWrapper {
    public static String assembleCmdForAsm(String filenames,String resultDir,String fastaDir){
        StringBuilder cmd =  new StringBuilder();
        if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
            cmd.append("perl /share/ass/genome_pipeline.pl");
            cmd.append(" -r ").append(filenames);
            cmd.append(" -o ").append(resultDir);
            cmd.append(" -fo ").append(fastaDir);
            cmd.append(" -ass");
        }
        else{
            cmd.append("ping baidu.com");
        }
        //System.out.println(cmd.toString());
        return cmd.toString();
    }
    public static String assembleCmd(String filenames, String resultDir, TaskType tt) {

        StringBuilder cmd = new StringBuilder();
        if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
            /*String fileStr = "";
            for (File f : files) {
                if (StringUtils.isNotBlank(fileStr)) {
                    fileStr += ",";
                }
                fileStr += f.getAbsolutePath();
            }*/
            cmd.append("perl /share/ass/genome_pipeline.pl ");
            cmd.append(" -o ").append(resultDir);

            // deepin/ubuntu
            switch (tt) {
                case QC:
                    cmd.append(" -r ").append(filenames).append(" -qc");
                    break;
//                case ASM:
//                    cmd.append(" -r ").append(filenames).append(" -ass");
//                    break;
                case PRD:
                    cmd = new StringBuilder();
                    cmd.append("perl /share/ass/genome_pipeline.pl -prd -fasta ");
                    cmd.append(filenames);
                    cmd.append(" -o ").append(resultDir);
                    break;
                case ANT:
                    cmd = new StringBuilder();
                    cmd.append("perl /share/ass/genome_pipeline.pl -anno -fasta ");
                    cmd.append(filenames);
                    cmd.append(" -o ").append(resultDir);
                    //cmd.append(" -fasta ").append(filenames).append(" -anno");
                    break;
                default:
                    cmd = new StringBuilder();
                    cmd.append("echo 1 && cd /share/ass/celbox-app/");
                    break;
            }
        } else {
            // windows
            cmd.append("ping baidu.com");
        }
        //System.out.println(cmd.toString());
        return cmd.toString();
    }
}
