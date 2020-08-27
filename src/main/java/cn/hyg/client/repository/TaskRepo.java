package cn.hyg.client.repository;

import cn.hyg.client.model.Task;
import cn.hyg.client.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskRepo {

    public void create() {
        String sql = "create table task(" +
                " id int(11) NOT NULL AUTO_INCREMENT," +
                " serial char(255)," +
                " name char(255)," +
                " type char(255)," +
                " files char(255)," +
                " path char(255)," +
                " cmd char(255)," +
                " process_id char(255)," +
                " result char(255)," +
                " result_dir char(255)," +
                " submit_time datetime," +
                " start_time datetime," +
                " finish_time datetime," +
                " update_time datetime," +
                " state int(11) default 0," +
                " isdelete int(11) default 0," +
                " PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
        DbGeneric.createTable(sql);
    }

    public void insert(Task newTask) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;
        String sqlInsert = "insert into task(serial,name,files,cmd,submit_time,state,result_dir,type,path) values(?,?,?,?,?,?,?,?,?)";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlInsert);

            // 填充sql语句中的参数(？)
            pStm.setString(1, newTask.getSerial());
            pStm.setString(2, newTask.getName());
            pStm.setString(3, newTask.getFiles());
            pStm.setString(4, newTask.getCmd());
            //pStm.setString(5, newTask.getProcessId());
            pStm.setString(5, StringHelper.convertTime2Str(newTask.getSubmitTime()));
            pStm.setString(6, String.valueOf(newTask.getState()));
            pStm.setString(7, newTask.getResultDir());
            pStm.setString(8, newTask.getType());
            pStm.setString(9, newTask.getPath());

            // 使用executeUpdate函数执行sql语句
            int row = pStm.executeUpdate();

            //System.out.println("新增用户成功" + row + "行受到影响");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(null, pStm, conn);
        }
    }

    public void startTask(String serial, String pid) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;
        String sqlInsert = "update task t set t.start_time = ?, t.process_id = ?, t.state=11 where t.serial = '" + serial + "'";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlInsert);

            // 填充sql语句中的参数(？)
            pStm.setString(1, StringHelper.convertTime2Str(new Date()));
            pStm.setString(2, pid);

            // 使用executeUpdate函数执行sql语句
            pStm.executeUpdate();

            //System.out.println("新增用户成功" + row + "行受到影响");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(null, pStm, conn);
        }
    }

    public void finishTask(String serial, String result, int state) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;
        String sqlInsert = "update task t set t.finish_time = ?, t.result = ?, t.state = ? where t.serial = '" + serial + "'";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlInsert);

            // 填充sql语句中的参数(？)
            pStm.setString(1, StringHelper.convertTime2Str(new Date()));
            pStm.setString(2, result);
            pStm.setObject(3, state);

            // 使用executeUpdate函数执行sql语句
            pStm.executeUpdate();

            //System.out.println("新增用户成功" + row + "行受到影响");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(null, pStm, conn);
        }
    }

    /**
     * 查询结果以list返回
     *
     * @param pageIndex 页数
     * @param pageSize  每页记录条数
     */
    public List<Task> queryList(int pageIndex, int pageSize, int state, String type) {

        int offset = pageIndex * pageSize;

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;
        List<Task> dataList = new ArrayList<>();

        String sqlList = "select t.* from task t where 1=1 and type='" + type + "' and isdelete=0 ";
        String stateCondition = assembleStateCondition(state);
        sqlList += stateCondition + " order by t.id desc limit " + offset + "," + pageSize;

        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return dataList;
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return dataList;
            }

            List list = DbGeneric.populate(rs, Task.class);
            for (Object o : list) {
                Task t = (Task) o;
                dataList.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }

        return dataList;
    }

    /**
     * 获取一共多少数据
     */
    public int count(int state, String type) {

        int result = 0;

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String sqlList = "select count(distinct t.id) as total from task t where 1=1 and type='" + type + "' "
                + "and isdelete=0"
                + assembleStateCondition(state);

        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return result;
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return result;
            }

            if (rs.next()) {
                result = rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }

        return result;
    }

    /**
     * 查询结果以list返回
     *
     */
    public List<Task> listAllByState(int state, String type) {

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;
        List<Task> dataList = new ArrayList<>();

        String sqlList = "select t.* from task t where 1=1 and isdelete=0 ";
        String typeCondition = "";
        if (StringUtils.isNotBlank(type)) {
            typeCondition += " and type='" + type + "' ";
        }
        String stateCondition = assembleStateCondition(state);
        sqlList += typeCondition + stateCondition + " order by t.id desc ";

        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return dataList;
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return dataList;
            }

            List list = DbGeneric.populate(rs, Task.class);
            for (Object o : list) {
                Task t = (Task) o;
                dataList.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }

        return dataList;
    }

    public void setState(String serial, int state) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;
        String sqlInsert = "update task t set t.update_time = ?, t.state = ? where t.serial = '" + serial + "'";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlInsert);

            // 填充sql语句中的参数(？)
            pStm.setString(1, StringHelper.convertTime2Str(new Date()));
            pStm.setObject(2, state);

            // 使用executeUpdate函数执行sql语句
            pStm.executeUpdate();

            //System.out.println("新增用户成功" + row + "行受到影响");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(null, pStm, conn);
        }
    }
    public int getState(String serial){
        int result = 0;

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String sqlList = "select state from task where 1=1 and serial='" + serial + "' ";
        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return result;
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return result;
            }

            if (rs.next()) {
                result = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }
        return result;
    }
    public int deleTask(String serial){
        int result = 0;

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String sqlList = "delete from task where 1=1 and serial='" + serial + "' ";
        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return result;
            }

            result = pStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }
        return result;
    }
    public void setDelete(String serial, int isDelete) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;
        String sqlInsert = "update task t set t.update_time = ?, t.isdelete = ? where t.serial = '" + serial + "'";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlInsert);

            // 填充sql语句中的参数(？)
            pStm.setString(1, StringHelper.convertTime2Str(new Date()));
            pStm.setObject(2, isDelete);

            // 使用executeUpdate函数执行sql语句
            pStm.executeUpdate();

            //System.out.println("新增用户成功" + row + "行受到影响");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(null, pStm, conn);
        }
    }
    /**
     * 获取结果目录
     */
    public String getResultDir(String serial) {

        String result = "";

        Connection conn = DbGeneric.getConnection();

        PreparedStatement pStmt = null;
        ResultSet rs = null;

        String sqlList = "select t.result_dir as resultDir from task t where t.serial = '" + serial + "'";

        try {
            pStmt = conn.prepareStatement(sqlList);
            if (pStmt == null) {
                return result;
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return result;
            }

            if (rs.next()) {
                result = rs.getString("resultDir");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }

        return result;
    }

    private String assembleStateCondition(int state) {
        String stateCondition = "";
        switch (state) {
            case 10:
            case 11:
                stateCondition = " and t.state in (10,11) ";
                break;
            case 60:
            case 61:
            case 404:
                stateCondition = " and t.state=" + state;
                break;
            default:
                break;
        }

        return stateCondition;
    }

}
