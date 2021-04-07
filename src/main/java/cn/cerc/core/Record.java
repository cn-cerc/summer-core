package cn.cerc.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

public class Record implements IRecord, Serializable {
	private static final Logger log = LoggerFactory.getLogger(Record.class);

    private static final long serialVersionUID = 4454304132898734723L;
    private DataSetState state = DataSetState.dsNone;
    private FieldDefs defs;
    private Map<String, Object> items = new LinkedHashMap<String, Object>();
    private Map<String, Object> delta = new HashMap<String, Object>();
    private DataSet dataSet;

    public Record() {
        this.defs = new FieldDefs();
    }

    public Record(FieldDefs defs) {
        this.defs = defs;
    }

    public static void main(String[] args) {
        Record record = new Record();
        // record.getFieldDefs().add("num", new DoubleField(18, 4));
        record.setField("num", 12345);
        record.setState(DataSetState.dsEdit);
        record.setField("num", 0);
        record.setField("num", 123452);

        // 增加对BigInteger的测试
        record.setField("num2", 123);
        System.out.println(record.getBigInteger("num2"));
        record.setField("num2", 123452L);
        System.out.println(record.getBigInteger("num2"));
        record.setField("num2", 123452d);
        System.out.println(record.getBigInteger("num2"));
        record.setField("num2", "123452");
        System.out.println(record.getBigInteger("num2"));
        record.setField("num2", new Object());
        System.out.println(record.getBigInteger("num2"));

        if (record.isModify()) {
            System.out.println("num old: " + record.getOldField("num"));
            System.out.println("num new: " + record.getField("num"));
        }
        System.out.println(record);
        record.delete("num2");
        record.getFieldDefs().add("num3");
        System.out.println(record);
        record.delete("num3");
        System.out.println(record);
    }

    public DataSetState getState() {
        return state;
    }

    public void setState(DataSetState dataSetState) {
        if (dataSetState == DataSetState.dsEdit) {
            if (this.state == DataSetState.dsInsert) {
                // throw new RuntimeException("当前记录为插入状态 不允许被修改");
                return;
            }
        }
        if (dataSetState.equals(DataSetState.dsNone)) {
            delta.clear();
        }
        this.state = dataSetState;
    }

    @Override
    public Record setField(String field, Object value) {
        if (field == null || "".equals(field)) {
            throw new RuntimeException("field is null!");
        }
        if (!defs.exists(field)) {
            defs.add(field);
        }

        if (this.state == DataSetState.dsEdit) {
            Object oldValue = items.get(field);
            // 只有值发生变更的时候 才做处理
            if (compareValue(value, oldValue)) {
                return this;
            } else {
                if (!delta.containsKey(field)) {
                    setValue(delta, field, oldValue);
                }
            }
        }
        setValue(items, field, value);

        return this;
    }

    private void setValue(Map<String, Object> map, String field, Object value) {
        if (value == null || value instanceof Integer || value instanceof Double || value instanceof Boolean
                || value instanceof Long) {
            map.put(field, value);
        } else if (value instanceof String) {
            if ("{}".equals(value)) {
                map.put(field, null);
            } else {
                map.put(field, value);
            }
        } else if (value instanceof BigDecimal) {
            map.put(field, ((BigDecimal) value).doubleValue());
        } else if (value instanceof LinkedTreeMap) {
            map.put(field, null);
        } else if (value instanceof Date) {
            map.put(field, value);
        } else if (value instanceof TDateTime) {
            map.put(field, ((TDateTime) value).getData());
        } else {
            map.put(field, value);
        }
    }

    private boolean compareValue(Object value, Object compareValue) {
        // 都为空
        if (value == null && compareValue == null) {
            return true;
        }
        // 都不为空
        if (value != null && compareValue != null) {
            if ((value instanceof Integer) && (compareValue instanceof Double)) {
                Integer v1 = (Integer) value;
                Double v2 = (Double) compareValue;
                return v2 - v1 == 0;
            } else {
                return value.equals(compareValue);
            }
        } else {
            return false;
        }
    }

    @Override
    public Object getField(String field) {
        if (field == null || "".equals(field)) {
            throw new RuntimeException("field is null!");
        }
        return items.get(field);
    }

    public Map<String, Object> getDelta() {
        return delta;
    }

    public Object getOldField(String field) {
        if (field == null || "".equals(field)) {
            throw new RuntimeException("field is null!");
        }
        return delta.get(field);
    }

    public int size() {
        return items.size();
    }

    public Map<String, Object> getItems() {
        return this.items;
    }

    public FieldDefs getFieldDefs() {
        return defs;
    }

    public void copyValues(Record source) {
        this.copyValues(source, source.getFieldDefs());
    }

    public void copyValues(Record source, FieldDefs defs) {
        List<String> tmp = defs.getFields();
        String[] items = new String[defs.size()];
        for (int i = 0; i < defs.size(); i++) {
            items[i] = tmp.get(i);
        }
        copyValues(source, items);
    }

    public void copyValues(Record source, String... fields) {
        if (fields.length > 0) {
            for (String field : fields) {
                this.setField(field, source.getField(field));
            }
        } else {
            for (String field : source.getFieldDefs().getFields()) {
                this.setField(field, source.getField(field));
            }
        }
    }

    @Override
    public String toString() {
        Map<String, Object> items = new TreeMap<>();
        for (int i = 0; i < defs.size(); i++) {
            String field = defs.getFields().get(i);
            Object obj = this.getField(field);
            if (obj instanceof TDateTime) {
                items.put(field, obj.toString());
            } else if (obj instanceof Date) {
                items.put(field, (new TDateTime((Date) obj)).toString());
            } else {
                items.put(field, obj);
            }
        }
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(items);
    }

    public void setJSON(Object jsonObj) {
        if (!(jsonObj instanceof Map<?, ?>)) {
            throw new RuntimeException("not support type：" + jsonObj.getClass().getName());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> head = (Map<String, Object>) jsonObj;
        for (String field : head.keySet()) {
            Object obj = head.get(field);
            if (obj instanceof Double) {
                double tmp = (double) obj;
                if (tmp >= Integer.MIN_VALUE && tmp <= Integer.MAX_VALUE) {
                    Integer val = (int) tmp;
                    if (tmp == val) {
                        obj = val;
                    }
                }
            }
            setField(field, obj);
        }
    }

    public void setJSON(String jsonStr) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        items = gson.fromJson(jsonStr, new TypeToken<Map<String, Object>>() {
        }.getType());
        defs.clear();
        for (String key : items.keySet()) {
            defs.add(key);
            if ("{}".equals(items.get(key))) {
                items.put(key, null);
            }
        }
    }

    @Override
    public boolean getBoolean(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }

        Object obj = this.getField(field);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            String str = (String) obj;
            return !"".equals(str) && !"0".equals(str) && !"false".equals(str);
        } else if (obj instanceof Integer) {
            int value = (Integer) obj;
            return value > 0;
        } else {
            return false;
        }
    }

    @Override
    public int getInt(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }

        Object obj = this.getField(field);
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof BigInteger) {
            log.warn("type error: getInt() can not use BigInteger");
            return ((BigInteger) obj).intValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else if (obj instanceof String) {
            String str = (String) obj;
            if ("".equals(str)) {
                return 0;
            }
            double val = Double.parseDouble(str);
            return (int) val;
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if ((obj instanceof Boolean)) {
            return (Boolean) obj ? 1 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public BigInteger getBigInteger(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }

        Object obj = this.getField(field);
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        } else if (obj instanceof String) {
            String str = (String) obj;
            if ("".equals(str)) {
                return BigInteger.valueOf(0);
            }
            return new BigInteger(str);
        } else if (obj instanceof Integer) {
            return BigInteger.valueOf((Integer) obj);
        } else if (obj instanceof Double) {
            return BigInteger.valueOf(((Double) obj).longValue());
        } else if (obj instanceof Long) {
            return BigInteger.valueOf((Long) obj);
        } else {
            return BigInteger.valueOf(0);
        }
    }

    @Override
    public BigDecimal getBigDecimal(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }
        Object obj = this.getField(field);
        if (obj instanceof BigInteger) {
            return (BigDecimal) obj;
        } else if (obj instanceof String) {
            String str = (String) obj;
            if ("".equals(str)) {
                return BigDecimal.valueOf(0);
            }
            return new BigDecimal(str);
        } else if (obj instanceof Integer) {
            return BigDecimal.valueOf((Integer) obj);
        } else if (obj instanceof Double) {
            return BigDecimal.valueOf(((Double) obj).longValue());
        } else if (obj instanceof Long) {
            return BigDecimal.valueOf((Long) obj);
        } else {
            return BigDecimal.valueOf(0);
        }
    }

    @Override
    public double getDouble(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }

        Object obj = this.getField(field);
        if (obj instanceof String) {
            String str = (String) obj;
            if ("".equals(str)) {
                return 0;
            }
            return Double.parseDouble((String) obj);
        }
        if (obj instanceof Integer) {
            return ((Integer) obj) * 1.0;
        } else if (obj == null) {
            return 0.0;
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).doubleValue();
        } else if (obj instanceof Long) {
            Long tmp = (Long) obj;
            return tmp * 1.0;
        } else if ((obj instanceof Boolean)) {
            return (Boolean) obj ? 1 : 0;
        } else {
            double d = (Double) obj;
            if (d == 0) {
                d = 0;
            }
            return d;
        }
    }

    public double getDouble(String field, int digit) {
        double result = this.getDouble(field);
        String str = "0.00000000";
        str = str.substring(0, str.indexOf(".") + (-digit) + 1);
        DecimalFormat df = new DecimalFormat(str);
        return Double.parseDouble(df.format(result));
    }

    @Override
    public String getString(String field) {
        if (field == null) {
            throw new RuntimeException("field is null");
        }
        if (!defs.exists(field)) {
            defs.add(field);
        }

        String result = "";
        Object obj = this.getField(field);
        if (obj != null) {
            if (obj instanceof String) {
                result = (String) obj;
            } else if (obj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                result = sdf.format(obj);
            } else {
                result = obj.toString();
            }
        }
        return result;
    }

    /**
     * 防止注入攻击
     *
     * @param field 字段名
     * @return 返回安全的字符串
     */
    @Deprecated
    public String getSafeString(String field) {
        String value = getString(field);
        return value == null ? "" : value.replaceAll("'", "''");
    }

    @Override
    public TDate getDate(String field) {
        return this.getDateTime(field).asDate();
    }

    @Override
    public TDateTime getDateTime(String field) {
        if (!defs.exists(field)) {
            defs.add(field);
        }

        Object obj = this.getField(field);
        if (obj == null) {
            return new TDateTime();
        } else if (obj instanceof TDateTime) {
            // return (TDateTime) obj;
            throw new RuntimeException("Record not support TDateTime");
        } else if (obj instanceof String) {
            String val = (String) obj;
            TDateTime tdt = new TDateTime();
            SimpleDateFormat sdf = new SimpleDateFormat(val.length() == 10 ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss");
            try {
                Date date = sdf.parse(val);
                tdt.setData(date);
                return tdt;
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else if (obj instanceof Date) {
            return new TDateTime((Date) obj);
        } else {
            throw new RuntimeException(String.format("%s Field not is %s.", field, obj.getClass().getName()));
        }
    }

    public void clear() {
        items.clear();
        delta.clear();
    }

    @Override
    public boolean exists(String field) {
        return this.defs.exists(field);
    }

    public boolean hasValue(String field) {
        return defs.exists(field) && !"".equals(getString(field));
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public DataSet locate() {
        int recNo = dataSet.getRecords().indexOf(this) + 1;
        dataSet.setRecNo(recNo);
        return dataSet;
    }

    public boolean isModify() {
        switch (this.state) {
            case dsInsert:
                return true;
            case dsEdit: {
                if (delta.size() == 0) {
                    return false;
                }
                List<String> delList = new ArrayList<>();
                for (String field : delta.keySet()) {
                    Object value = items.get(field);
                    Object oldValue = delta.get(field);
                    if (compareValue(value, oldValue)) {
                        delList.add(field);
                    }
                }
                for (String field : delList) {
                    delta.remove(field);
                }
                return delta.size() > 0;
            }
            default:
                return false;
        }
    }

    public boolean equalsValues(Map<String, Object> values) {
        for (String field : values.keySet()) {
            Object obj1 = getField(field);
            String value = obj1 == null ? "null" : obj1.toString();
            Object obj2 = values.get(field);
            String compareValue = obj2 == null ? "null" : obj2.toString();
            if (!value.equals(compareValue)) {
                return false;
            }
        }
        return true;
    }

    public void delete(String field) {
        delta.remove(field);
        items.remove(field);
        if (defs != null) {
            defs.delete(field);
        }
    }
}
