package com.superflower.mysql;

import com.mysql.jdbc.Driver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionPoolImpl implements ConnectionPool {

    private static String jdbcDriver;

    private static String jdbcurl;

    private static String userName;

    private static String password;

    private static int initCount;

    private static int stepSize;

    private static int poolMaxSize;

    private static CopyOnWriteArrayList<ConnectionProperties> connectionList = new CopyOnWriteArrayList<>();

    public ConnectionPoolImpl() {
        init();
    }

    // 根据配置文件初始化参数 创建初始连接数的连接池
    public void init() {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");

        Properties pro = new Properties();
        try {
            pro.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jdbcDriver = pro.getProperty("jdbcDriver");
        jdbcurl = pro.getProperty("jdbcurl");
        userName = pro.getProperty("userName");
        password = pro.getProperty("password");
        initCount = Integer.valueOf(pro.getProperty("initCount"));
        stepSize = Integer.valueOf(pro.getProperty("stepSize"));
        poolMaxSize = Integer.valueOf(pro.getProperty("poolMaxSize"));

        // 将当前数据库类型的driver注册进driverManager
        try {
            Driver driver = (Driver) Class.forName(jdbcDriver).newInstance();
            try {
                DriverManager.registerDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        creatConnectionPool(initCount);
    }

    @Override
    public ConnectionProperties getConnection() {
        if (connectionList.size() == 0) {
            System.err.println("连接池暂未初始化");
        }
        try {
            ConnectionProperties connection = getRealConnection();
            // 如果获取到的连接对象为null 表名连接池已满 向连接池添加更多连接
            while (connection == null) {
                creatConnectionPool(4);
                connection = getRealConnection();
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从连接列表中挑出一个有效并且不处于繁忙状态的连接对象
     * @return
     */
    private synchronized ConnectionProperties getRealConnection() throws SQLException {
        for (ConnectionProperties properties : connectionList) {
            if (properties.isBusy() == false) {
                // 校验是否是个有效的连接 无效创建一个
                Connection connection = properties.getConnection();
                if (!connection.isValid(2000)) {
                    Connection con = DriverManager.getConnection(jdbcurl, userName, password);
                    properties.setConnection(con);
                    properties.setBusy(true);
                }
                return properties;
            }
        }
        return null;
    }

    @Override
    public void creatConnectionPool(int initCount) {
        if(poolMaxSize > 0 && connectionList.size() + initCount > poolMaxSize) {
            System.err.println("创建连接池失败");
            return;
        }

        for (int i = 0; i < initCount; i++) {
            try {
                Connection connection = DriverManager.getConnection(jdbcurl, userName, password);
                ConnectionProperties properties = new ConnectionProperties(connection, false);
                connectionList.add(properties);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
