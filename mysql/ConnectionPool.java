package com.superflower.mysql;

public interface ConnectionPool {

    public ConnectionProperties getConnection();

    public void creatConnectionPool(int initCount);

}
