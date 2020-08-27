package cn.hyg.client.repository;

import cn.hyg.client.util.StringHelper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbGeneric {
//
    private static final String dbUrl = "jdbc:mysql://localhost/assembler_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8";
    private static final String acc = "assembler";
    private static final String pwd = "#*dK3y^!@";

//    private static final String dbUrl = "jdbc:mysql://localhost/assembler_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8";
//    private static final String acc = "root";
//    private static final String pwd = "s814466057";

    /**
     * 1. 实现数据库连接的方法
     */
    public static Connection getConnection() {

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(dbUrl, acc, pwd);
            //System.out.println("Database connected");

        } catch (Exception ex) {

            System.out.println("Database connection failed");
            ex.printStackTrace();

        }

        return conn;
    }

    /**
     * 2. 释放数据库连接
     */
    public static void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 创建数据库
    public static void createTable(String sql_create) {
        Connection conn = getConnection();
        if (conn == null) {
            return;
        }
        PreparedStatement pStm = null;

        try {

            pStm = conn.prepareStatement(sql_create);
            pStm.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        } finally {
            if (pStm != null) {
                closeConnection(null, pStm, conn);
            }
        }
    }

    /*
     * 将rs结果转换成对象列表
     * @param rs jdbc结果集
     * @param clazz 对象的映射类
     * return 封装了对象的结果列表
     */
    public static List populate(ResultSet rs, Class clazz) throws SQLException {
        // 结果集的元素对象
        ResultSetMetaData rsMd = rs.getMetaData();
        // 获取结果集的元素个数
        int colCount = rsMd.getColumnCount();
        /*System.out.println("#");
        for(int i = 1;i<=colCount;i++){
            System.out.println(rsMd.getColumnName(i));
            System.out.println(rsMd.getColumnClassName(i));
            System.out.println("#");
        }*/

        // 返回结果的列表集合
        List list = new ArrayList();
        // 业务对象的属性数组
        Field[] fields = clazz.getDeclaredFields();
        try {
            while (rs.next()) { // 对每一条记录进行操作
                Object obj = clazz.newInstance(); // 构造业务对象实体
                // 将每一个字段取出进行赋值
                for (int i = 1; i <= colCount; i++) {
                    Object value = rs.getObject(i);
                    // 寻找该列对应的对象属性
                    for (Field f : fields) {
                        // 如果匹配进行赋值
                        if (StringHelper.upperCharToUnderLine(f.getName()).equalsIgnoreCase(rsMd.getColumnName(i))) {
                            boolean flag = f.isAccessible();
                            f.setAccessible(true);
                            f.set(obj, value);
                            f.setAccessible(flag);
                        }
                    }
                }
                list.add(obj);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return list;
    }
}
