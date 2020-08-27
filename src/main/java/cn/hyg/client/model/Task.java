package cn.hyg.client.model;

import cn.hyg.client.util.FileUtil;
import cn.hyg.client.util.StringHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task {

    private Integer id;
    private String serial;
    private String name;
    private String type;
    private String files;
    private String path;
    private String cmd;
    private String processId;
    private String result;
    private String resultDir;
    private Date submitTime;
    private Date startTime;
    private Date finishTime;
    private Date updateTime;
    private int state;
    private int isdelete;

    public int getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(int isdelete) {
        this.isdelete = isdelete;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultDir() {
        return resultDir;
    }

    public void setResultDir(String resultDir) {
        this.resultDir = resultDir;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public StringProperty getSerialProperty() {
        return new SimpleStringProperty(serial);
    }

    public StringProperty getNameProperty() {
        return new SimpleStringProperty(name);
    }

    public StringProperty getFilesProperty() {
        String result = "";
        if (StringUtils.isNotBlank(files)) {
            String[] fa = files.split(",");
            for (String s : fa) {
                if (StringUtils.isNotBlank(result)) {
                    result += "\n";
                }
                result += FileUtil.getFilenameWithSuffix(s);
            }
        }
        return new SimpleStringProperty(result);
    }

    public StringProperty getStatusProperty() {
        return new SimpleStringProperty(getStateStr());
    }

    public StringProperty getSubmitTimeProperty() {
        String result = "";
        if (submitTime != null) {
            String[] sa = StringHelper.convertTime2Str(submitTime).split(" ");
            result = sa[0] + "\n" + sa[1];
        }
        return new SimpleStringProperty(result);
    }

    public StringProperty getFinishTimeProperty() {
        String result = "";
        if (finishTime != null) {
            String[] sa = StringHelper.convertTime2Str(finishTime).split(" ");
            result = sa[0] + "\n" + sa[1];
        }
        return new SimpleStringProperty(result);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateStr() {
        Map<String,String> map = new HashMap<>();
        map.put("QC","质控");
        map.put("ASM","拼接");
        map.put("PRD","预测");
        map.put("ANT","注释");
        switch (state) {
            case 10:
                return map.get(this.type) + "排队";
            case 11:
                return map.get(this.type) + "正在运行";
            case 60:
                return map.get(this.type) + "已完成";
            case 61:
                return "质控未通过";
            case 403:
                return map.get(this.type) + "终止";
            case 404:
                return map.get(this.type) + "异常";
            default:
                return "";
        }
    }

    public String toString() {
        return "item [" + serial + ','+ name + "," + files + "," + getStateStr() + "," +
                StringHelper.convertTime2Str(submitTime) + "," +
                StringHelper.convertTime2Str(finishTime) + "]" + "isdelete " + isdelete;
    }
}
