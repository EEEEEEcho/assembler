package cn.hyg.client.job;

import cn.hyg.client.model.Task;
import cn.hyg.client.repository.TaskRepo;
import cn.hyg.client.util.*;
import cn.hyg.client.value.TaskType;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class JobInstance implements Supplier<String> {

    //private List<File> inputs;
    private String filenames;
    private String inputDir;
    private String outputDir;
    private String serial;
    private Task task;
    private TaskType tt;
    private boolean rerun;
    private String cmd;
    private String realOutputDir;
    private String strainName;
    private String fastaDir;


    public JobInstance(String filenames, String inputDir, String outputDir, TaskType tt) {
        this.filenames = filenames;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.tt = tt;
    }


    public void init() {
        /*if (inputs == null || inputs.size() == 0) {
            System.out.println("输入文件为空");
            return;
        }*/

        if (StringUtils.isBlank(filenames)) {
            System.out.println("输入文件为空");
            return;
        }

        if (StringUtils.isBlank(outputDir)) {
            System.out.println("目标文件夹为空");
            return;
        }

        // 获取菌种名
        if (filenames.contains(",")) {
            String s = filenames.split(",")[0];
            strainName = StringHelper.extractStrainName(s);
        } else {
            strainName = StringHelper.extractStrainName(filenames);
        }
        if (StringUtils.isBlank(strainName)) {
            return;
        }
        serial = UuidUtil.get32UUIDFull();
        realOutputDir = outputDir + File.separator + FileUtil.appendTimestampToFilename(strainName) + File.separator;
        String inputNames = "";
        if (filenames.contains(",")) {
            inputNames = inputDir + filenames.split(",")[0];
            inputNames += "," + inputDir + filenames.split(",")[1];
        } else {
            inputNames = inputDir + filenames;
        }
        if(tt.equals(TaskType.ASM)){
            cmd = CmdWrapper.assembleCmdForAsm(inputNames,realOutputDir,fastaDir);
        }
        else{
            cmd = CmdWrapper.assembleCmd(inputNames, realOutputDir, tt);
        }
        if (!rerun) {
            serial = initTask(strainName, filenames, inputDir, realOutputDir, cmd, tt);
            if (StringUtils.isBlank(serial)) {
                return;
            }
        } else {
            serial = task.getSerial();
        }
    }

    // 新建Task对象，并写入数据库，记录提交时间
    private String initTask(String strainName, String files, String inputDir, String outputDir, String cmd, TaskType tt) {
        TaskRepo tr = new TaskRepo();
        Task t = new Task();
        t.setName(strainName);
        t.setSerial(serial);
        t.setFiles(files);
        t.setPath(inputDir);
        t.setCmd(cmd);
        t.setSubmitTime(new Date());
        t.setState(10);
        t.setType(tt.name());
        t.setResultDir(outputDir);
        tr.insert(t);
        return serial;
    }

    private String rerunTask(Task t) {
        String serial = t.getSerial();
        TaskRepo tr = new TaskRepo();
        tr.setState(serial, 10);
        return serial;
    }

    private void finishTask(String serial, String[] cmdResult) {
        TaskRepo tr = new TaskRepo();
        int state = justifyResult(cmdResult, serial);
        tr.finishTask(serial, cmdResult[2], state);
    }

    // 0 output
    // 1 error
    // 2 exit value
    private int justifyResult(String[] cmdResult, String serial) {

        TaskRepo tr = new TaskRepo();
        String resultDir = tr.getResultDir(serial);

        if (StringUtils.isBlank(resultDir)) {
            return 404;
        }

        List<String> lines = new ArrayList<>();
        // 将结果输出至文件
        if (StringUtils.isNotBlank(cmdResult[0])) {
            String logFile = resultDir + "asm.log";
            String[] logs = cmdResult[0].split("\n");
            //System.out.println("task:" + this.task);
            if (logs.length > 0) {
                Collections.addAll(lines, logs);
                FileUtil.writeFile(logFile, lines);
            }
        }

        if (StringUtils.isNotBlank(cmdResult[1])) {
            String errFile = resultDir + "asm.err";
            String[] err = cmdResult[1].split("\n");
            if (err.length > 0) {
                lines = new ArrayList<>();
                Collections.addAll(lines, err);
                FileUtil.writeFile(errFile, lines);
            }
        }

        String evFile = resultDir + "exit.value";
        String ev = cmdResult[2];
        int exitValue = -1;
        if (StringUtils.isNotBlank(ev) && StringHelper.isDigital(ev)) {
            exitValue = Integer.parseInt(ev);
        }
        //System.out.println("result: " + ev + ", " + String.valueOf(exitValue));
        lines = new ArrayList<>();
        lines.add(ev);
        FileUtil.writeFile(evFile, lines);
        if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
            String filenameWithPath = resultDir + "LOG.txt";
            File f = new File(filenameWithPath);
            if (!f.exists()) {
                return 404;
            }

            lines = FileUtil.readFileContent(filenameWithPath, "UTF-8");
            if (lines.size() == 0) {
                return 404;
            }
//            if(this.task.getType().equals("ASM")){
//                System.out.println("It is ASM!!!!!!!!!!!!!!!!!!!!!!!!!");
//                for(String line : lines){
//                    System.out.println(line);
//                    if(line.contains("QC FAIL")){
//                        return 61;
//                    }
//                }
//            }
            String lastLine = lines.get(lines.size() - 1);
            /*String[] output = cmdResult[0].split("\n");
            String lastLine = output[output.length - 1];*/
            System.out.println("last line: " + lastLine);

            if (StringUtils.isBlank(lastLine) || !lastLine.contains("Finished")) {
                return 404;
            } else if(cmdResult[2].equals("61")){
                return 61;
            }
            else{
                return 60;
            }
        } else {
            switch (exitValue) {
                case 0:
                    return 60;
                default:
                    return 404;
            }
        }
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    public String getFastaDir() {
        return fastaDir;
    }

    public void setFastaDir(String fastaDir) {
        this.fastaDir = fastaDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public String get() {
        TaskRepo taskRepo = new TaskRepo();
        int state = taskRepo.getState(serial);
        if(state == 403){
            return "终止";
        }
        System.out.println("[" + new Date() + "] 开始运行任务");
        System.out.println("输出文件夹: " + realOutputDir);
        File targetDir = new File(realOutputDir);
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            if (!FileUtil.createDir(realOutputDir)) {
                System.out.println("创建文件夹失败: " + realOutputDir);
                return "创建文件夹失败";
            }
        }
        System.out.println("命令: " + cmd);
        // 0 output
        // 1 error
        // 2 exit value
        //System.out.println("tast:"+tt.name());
        String[] result = ShellUtil.runTask(serial, cmd,tt.name());
        //System.out.println("result[0]:" + result[0]);

        // 更新Task数据对象
        finishTask(serial, result);
        System.out.println("[" + new Date() + "] 任务结束: " + serial);
        return serial;
    }
}
