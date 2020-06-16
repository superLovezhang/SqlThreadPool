package com.superflower.mysql;

public class ManagerPool {

    private static class CreatPool {
        private static ConnectionPool connectionPool = new ConnectionPoolImpl();
    }

    public static ConnectionPool getInstance() {
        return CreatPool.connectionPool;
    }

}
