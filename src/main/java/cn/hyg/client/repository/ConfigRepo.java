package cn.hyg.client.repository;

import cn.hyg.client.model.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ConfigRepo {

    public void create() {
        String sql = "create table config(" +
                " id int(11) NOT NULL AUTO_INCREMENT," +
                " name char(255)," +
                " value char(255)," +
                " PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
        DbGeneric.createTable(sql);
    }

    public String getTargetDir(String target) {
        Connection conn = DbGeneric.getConnection();
        String sql = "select c.* from config c where c.name='" + target + "'";
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            pStmt = conn.prepareStatement(sql);
            if (pStmt == null) {
                return "";
            }

            rs = pStmt.executeQuery();
            if (rs == null) {
                return "";
            }

            List list = DbGeneric.populate(rs, Config.class);
            for (Object o : list) {
                Config c = (Config) o;
                if (c.getName().equals(target)) {
                    return c.getValue();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放对数据库的连接
            DbGeneric.closeConnection(rs, pStmt, conn);
        }

        return "";
    }

    public void setTargetDir(String dir, String target) {
        Connection conn = DbGeneric.getConnection();
        PreparedStatement pStm = null;

        String sqlUpdate = "update config c set c.value = ? where c.name = '" + target + "'";    // sql语言
        try {
            pStm = conn.prepareStatement(sqlUpdate);

            // 填充sql语句中的参数(？)
            pStm.setString(1, dir);

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

}
