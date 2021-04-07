package cn.cerc.core;

import java.sql.Connection;

public abstract interface ISqlConnection extends IConnection {

    // 返回会话
    Connection getClient();

}
