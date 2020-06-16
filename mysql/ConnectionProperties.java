package com.superflower.mysql;


import java.sql.Connection;

/**
 *  数据库连接池的每一个连接对象
 */
public class ConnectionProperties {
    // 数据库连接
    private Connection connection;
    // 当前连接是否被占用
    private boolean isBusy = false;

    public ConnectionProperties(Connection connection, boolean isBusy) {
        this.connection = connection;
        this.isBusy = isBusy;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void close() {
        this.isBusy = false;
    }
}
