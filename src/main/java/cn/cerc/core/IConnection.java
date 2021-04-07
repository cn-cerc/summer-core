package cn.cerc.core;

public interface IConnection {
    String getClientId();

    // 返回会话
    Object getClient();

    @Deprecated
    void setConfig(IConfig config);
}
