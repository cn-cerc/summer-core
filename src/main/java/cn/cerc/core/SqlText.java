package cn.cerc.core;

import java.util.ArrayList;
import java.util.List;

public class SqlText {
    // 从数据库每次加载的最大笔数
    public static final int MAX_RECORDS = 50000;
    public static int PUBLIC = 1;
    public static int PRIVATE = 2;
    public static int PROTECTED = 4;
    private static ClassResource res = new ClassResource(SqlText.class, "summer-core");
    private int maximum = MAX_RECORDS;
    private int offset = 0;
    // sql 指令
    private String text;
    private ClassData classData;
    private boolean supportMssql = false;

    public SqlText() {
        super();
    }

    public SqlText(Class<?> clazz) {
        super();
        classData = ClassFactory.get(clazz);
        if (classData.getTableId() == null) {
            throw new RuntimeException("entity.name or select not define");
        }
        this.text = classData.getSelect();
    }

    public SqlText(String commandText) {
        add(commandText);
    }

    public SqlText(String format, Object... args) {
        add(format, args);
    }

    public SqlText add(String text) {
        if (text == null) {
            throw new RuntimeException("sql not is null");
        }
        if (this.text == null) {
            this.text = text;
        } else {
            this.text = this.text + " " + text;
        }
        return this;
    }

    public SqlText add(String format, Object... args) {
        ArrayList<Object> items = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof String) {
                items.add(Utils.safeString((String) arg));
            } else {
                items.add(arg);
            }
        }
        return this.add(String.format(format, items.toArray()));
    }

    @Deprecated // 请改使用getTextByLimit
    public String getSelect() {
        return getTextByLimit();
    }

    public String getTextByLimit() {
        String sql = this.text;
        if (sql == null || "".equals(sql)) {
            throw new RuntimeException("SqlText.Text is null ！");
        }

        sql = sql + String.format(" limit %d,%d", this.offset, this.maximum);
        return sql;
    }

    public SqlText clear() {
        this.text = null;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getText() {
        return text;
    }

    public String getCommand() {
        String sql = this.getText();
        if (sql == null || "".equals(sql)) {
            throw new RuntimeException("SqlText.text is null ！");
        }

        if (sql.contains("call ") || sql.contains("show")) {
            return sql;
        }

        if (!this.supportMssql) {
            if (this.offset > 0) {
                if (this.maximum < 0) {
                    sql = sql + String.format(" limit %d,%d", this.offset, MAX_RECORDS + 1);
                } else {
                    sql = sql + String.format(" limit %d,%d", this.offset, this.maximum + 1);
                }
            } else if (this.maximum == MAX_RECORDS) {
                sql = sql + String.format(" limit %d", this.maximum + 2);
            } else if (this.maximum > -1) {
                sql = sql + String.format(" limit %d", this.maximum + 1);
            }
        }
        return sql;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        if (maximum > MAX_RECORDS) {
            throw new RuntimeException(String.format(res.getString(1, "本次请求的记录数超出了系统最大笔数为  %d 的限制！"), MAX_RECORDS));
        }
        this.maximum = maximum;
    }

    public String getTableId() {
        return classData != null ? classData.getTableId() : null;
    }

    @Deprecated // 请改使用 add(whereText).getText()
    public String getWhere(String whereText) {
        return add(whereText).getText();
    }

    @Deprecated // 请改使用 addWhereKeys(values).getText()
    public String getWhereKeys(Object... values) {
        return addWhereKeys(values).getText();
    }

    public SqlText addWhereKeys(Object... values) {
        if (values.length == 0) {
            throw new RuntimeException("values is null");
        }

        if (classData == null) {
            throw new RuntimeException("classData is null");
        }

        StringBuffer sb = new StringBuffer();
        List<String> idList = classData.getSearchKeys();
        if (idList.size() == 0) {
            throw new RuntimeException("id is null");
        }

        if (idList.size() != values.length) {
            throw new RuntimeException(String.format("ids.size(%s) != values.size(%s)", idList.size(), values.length));
        }

        int i = 0;
        int count = idList.size();
        if (count > 0) {
            sb.append("where");
        }
        for (String fieldCode : idList) {
            Object value = values[i];
            sb.append(i > 0 ? " and " : " ");
            if (value == null) {
                sb.append(String.format("%s is null", fieldCode));
            }
            if (value instanceof String) {
                sb.append(String.format("%s='%s'", fieldCode, Utils.safeString((String) value)));
            } else {
                sb.append(String.format("%s='%s'", fieldCode, value));
            }
            i++;
        }

        return add(sb.toString());
    }

    public ClassData getClassData() {
        return classData;
    }

    public boolean isSupportMssql() {
        return supportMssql;
    }

    public void setSupportMssql(boolean supportMssql) {
        this.supportMssql = supportMssql;
    }

}
