package cn.hyg.client.util;

import cn.hyg.client.repository.TaskRepo;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ShellUtil {

    public static String[] runCmd(String cmd[]) {
        // 需要执行的命令
        String[] result = new String[3];

        //ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(new File(directory));
        try {
            Process p = Runtime.getRuntime().exec(cmd); // 调用控制台执行shell
            // read result
            result = readResult(p);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static String[] runTask(String serial, String cmd,String jobType) {

        // 需要执行的命令
        String[] result = new String[3];

        //ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(new File(directory));
        try {
            Process p = Runtime.getRuntime().exec(cmd); // 调用控制台执行shell

            TaskRepo tr = new TaskRepo();
            tr.startTask(serial, String.valueOf(getPid(p)));

            // read result
            result = readResult(p,jobType);
            //System.out.println("result[2]:" + result[2]);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private static String[] readResult(Process p) {
        // 0 output
        // 1 error
        // 2 exit value
        String[] result = new String[3];

        BufferedReader br = null;
        StringBuilder error = new StringBuilder();
        StringBuilder input = new StringBuilder();

        try {
            String line;
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                input.append(line).append("\n");
            }
            result[0] = input.toString();
            // 获取执行后出现的错误；getInputStream是获取执行后的结果
            br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "GB2312"));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                error.append(line).append("\n");
            }
            result[1] = error.toString();
            //int exitValue = p.exitValue();
            int exitValue = p.waitFor();
            result[2] = String.valueOf(exitValue);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String[] readResult(Process p,String jobType) {
        // 0 output
        // 1 error
        // 2 exit value
        String[] result = new String[3];

        BufferedReader br = null;
        StringBuilder error = new StringBuilder();
        StringBuilder input = new StringBuilder();

        try {
            boolean asmFlg = false;
            String line;
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if(jobType.equals("ASM") && line.contains("QC FAIL")){
                    asmFlg = true;
                }
                input.append(line).append("\n");
            }
            result[0] = input.toString();
            // 获取执行后出现的错误；getInputStream是获取执行后的结果
            br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "GB2312"));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if(jobType.equals("ASM") && line.contains("QC FAIL")){
                    asmFlg = true;
                }
                error.append(line).append("\n");
            }
            result[1] = error.toString();
            //int exitValue = p.exitValue();
            int exitValue = p.waitFor();
            if(asmFlg){
                result[2] = "61";
            }
            else{
                result[2] = String.valueOf(exitValue);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int getPid(Process process) {
        List<String> matches = RegexUtils.getMatches("pid=\\d*", process.toString());
        String pid = "";
        if (matches.size() > 0) {
            pid = matches.get(0);
        }
        List<String> pidList = RegexUtils.getMatches("\\d+", pid);
        if (pidList.size() > 0) {
            pid = pidList.get(0);
        } else {
            pid = "-1";
        }
        System.out.println("[系统进程号]：" + pid);
        if (StringUtils.isBlank(pid) || !StringHelper.isDigital(pid)) {
            return -1;
        } else {
            return Integer.parseInt(pid);
        }
    }
}
