package cn.cerc.core;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TDateTime implements Serializable, Comparable<TDateTime>, Cloneable {
    private static final long serialVersionUID = -7395748632907604015L;
    private static Map<String, String> dateFormats = new HashMap<>();
    private static Map<String, String> map;

    static {
        map = new HashMap<>();
        map.put("YYYYMMDD", "yyyyMMdd");
        map.put("YYMMDD", "yyMMdd");
        map.put("YYYMMDD_HH_MM_DD", "yyyyMMdd_HH_mm_dd");
        map.put("yymmddhhmmss", "yyMMddHHmmss");
        map.put("yyyymmdd", "yyyyMMdd");
        map.put("YYYYMMDDHHMMSSZZZ", "yyyyMMddHHmmssSSS");
        map.put("YYYYMM", "yyyyMM");
        map.put("YYYY-MM-DD", "yyyy-MM-dd");
        map.put("yyyy-MM-dd", "yyyy-MM-dd");
        map.put("yyyyMMdd", "yyyyMMdd");
        map.put("YY", "yy");
        map.put("yy", "yy");
        map.put("YYYY", "yyyy");
        map.put("YYYY/MM/DD", "yyyy/MM/dd");

        dateFormats.put("yyyy-MM-dd HH:mm:ss", "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}");
        dateFormats.put("yyyy-MM-dd HH:mm", "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}");
        dateFormats.put("yyyy-MM-dd", "\\d{4}-\\d{2}-\\d{2}");
        dateFormats.put("yyyy/MM/dd HH:mm:ss", "\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}");
        dateFormats.put("yyyy/MM/dd", "\\d{4}/\\d{2}/\\d{2}");
        dateFormats.put("yyyyMMdd", "\\d{8}");
    }

    private Date data;

    public TDateTime() {
        this.data = new Date(0);
    }

    public TDateTime(String value) {
        String fmt = getFormat(value);
        if (fmt == null) {
            fmt = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        try {
            data = sdf.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TDateTime(String fmt, String value) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        try {
            data = sdf.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TDateTime(Date value) {
        data = value;
    }

    // 当时，带时分秒
    public static TDateTime now() {
        TDateTime result = new TDateTime();
        result.setData(new Date());
        return result;
    }

    @Deprecated
    public static TDateTime Now() {
        return TDateTime.now();
    }

    public static TDateTime fromDate(String val) {
        String fmt = getFormat(val);
        if (fmt == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        TDateTime tdtTo = new TDateTime();
        try {
            tdtTo.setData(sdf.parse(val));
            return tdtTo;
        } catch (ParseException e) {
            return null;
        }
    }

    public static TDateTime fromYearMonth(String val) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        TDateTime tdt = new TDateTime();
        try {
            tdt.setData(sdf.parse(val));
            return tdt;
        } catch (ParseException e) {
            ClassResource res = new ClassResource(TDateTime.class, "summer-core");
            throw new RuntimeException(String.format(res.getString(1, "不是 %s 标准年月格式 ：yyyyMM"), val));
        }
    }

    public static String getFormat(String val) {
        if (val == null) {
            return null;
        }
        if ("".equals(val)) {
            return null;
        }
        String fmt = null;
        java.util.Iterator<String> it = dateFormats.keySet().iterator();
        while (it.hasNext() && fmt == null) {
            String key = it.next();
            String str = dateFormats.get(key);
            if (val.matches(str)) {
                fmt = key;
            }
        }
        return fmt;
    }

    /**
     * 计算时间是否到期(精确到秒)
     *
     * @param startTime 起始时间
     * @param endTime   截止时间
     * @return 是否超时
     */
    public static boolean isTimeOut(TDateTime startTime, TDateTime endTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 一天的毫秒数
        long nd = 1000 * 24 * 60 * 60;

        // 一小时的毫秒数
        long nh = 1000 * 60 * 60;

        // 一分钟的毫秒数
        long nm = 1000 * 60;

        // 一秒钟的毫秒数
        long ns = 1000;

        long diff;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            // 计算时间差
            diff = dateFormat.parse(endTime.toString()).getTime() - dateFormat.parse(startTime.toString()).getTime();
            day = diff / nd;// 计算差多少天
            hour = diff % nd / nh + day * 24;// 计算差多少小时
            min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
            sec = diff % nd % nh % nm / ns;// 计算差多少秒
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 天数
        if (day > 0) {
            return true;
        }

        // 小时
        if (hour - day * 24 > 0) {
            return true;
        }

        // 分
        if (min - day * 24 * 60 > 0) {
            return true;
        }

        // 秒
        return sec - day > 0;
    }

    public static String FormatDateTime(String fmt, TDateTime value) {
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat(map.get(fmt));
        } catch (IllegalArgumentException e) {
            ClassResource res = new ClassResource(TDateTime.class, "summer-core");
            throw new RuntimeException(res.getString(2, "日期格式不正确"));
        }
        return sdf.format(value.getData());
    }

    public static TDateTime StrToDate(String val) {
        String fmt = TDateTime.getFormat(val);
        if (fmt == null) {
            ClassResource res = new ClassResource(TDateTime.class, "summer-core");
            throw new RuntimeException(String.format(res.getString(3, "时间格式不正确: value=%s"), val));
        }
        return new TDateTime(fmt, val);
    }

    public static String FormatDateTime(String fmt, Date value) {
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat(fmt);
        } catch (IllegalArgumentException e) {
            sdf = new SimpleDateFormat(map.get(fmt));
        }
        return sdf.format(value);
    }

    /**
     * 是否在指定时间范围内
     *
     * @param start 起始时间段
     * @param last  截止时间段
     * @return 是否在指定时间范围内
     */
    public static boolean isInterval(String start, String last) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Date now = null;
        Date beginTime = null;
        Date endTime = null;
        try {
            now = df.parse(df.format(new Date()));
            beginTime = df.parse(start);
            endTime = df.parse(last);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar date = Calendar.getInstance();
        date.setTime(now);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        return date.after(begin) && date.before(end);
    }

    @Override
    public String toString() {
        if (data == null) {
            return "";
        }
        return format("yyyy-MM-dd HH:mm:ss");
    }

    public String getDate() {
        return format("yyyy-MM-dd");
    }

    public String getTime() {
        return format("HH:mm:ss");
    }

    /**
     * @return 获取Java时间戳，一共13位，毫秒级
     */
    public long getTimestamp() {
        return this.getData().getTime();
    }

    /**
     * @return 获取Unix时间戳，一共10位，秒级
     */
    public long getUnixTimestamp() {
        return this.getData().getTime() / 1000;
    }

    public String getYearMonth() {
        return format("yyyyMM");
    }

    public String getMonthDay() {
        return format("MM-dd");
    }

    public String getYear() {
        return format("yyyy");
    }

    public String getFull() {
        return format("yyyy-MM-dd HH:mm:ss:SSS");
    }

    public String format(String fmt) {
        if (data == null) {
            return null;
        }
        if (data.compareTo(new Date(0)) == 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(data);
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public long compareSecond(TDateTime startTime) {
        if (startTime == null) {
            return 0;
        }

        // 一秒的毫秒数
        long second = 1000;

        long start = startTime.getData().getTime();
        long end = TDateTime.now().getData().getTime();
        return (end - start) / second;
    }

    public long compareMinute(TDateTime startTime) {
        if (startTime == null) {
            return 0;
        }

        // 一分钟的毫秒数
        long minute = 1000 * 60;

        long start = startTime.getData().getTime();
        long end = TDateTime.now().getData().getTime();
        return (end - start) / minute;
    }

    public long compareHour(TDateTime startTime) {
        if (startTime == null) {
            return 0;
        }

        // 一小时的毫秒数
        long hour = 1000 * 60 * 60;

        long start = startTime.getData().getTime();
        long end = TDateTime.now().getData().getTime();
        return (end - start) / hour;
    }

    // 若当前值大，则返回正数，否则返回负数

    public int compareDay(TDateTime dateFrom) {
        if (dateFrom == null) {
            return 0;
        }
        // 返回this - to 的差异天数 ,返回相对值
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        String str1 = sdf.format(this.getData());
        String str2 = sdf.format(dateFrom.getData());
        int count = 0;
        try {
            cal1.setTime(sdf.parse(str2));
            cal2.setTime(sdf.parse(str1));
            int flag = 1;
            if (cal1.after(cal2)) {
                flag = -1;
            }
            while (cal1.compareTo(cal2) != 0) {
                cal1.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR) + flag);
                count = count + flag;
            }
        } catch (ParseException e) {
            ClassResource res = new ClassResource(TDateTime.class, "summer-core");
            throw new RuntimeException(String.format(res.getString(4, "日期转换格式错误 ：%s") , e.getMessage()));
        }
        return count;
    }
    // 原MonthsBetween，改名为：compareMonth

    public int compareMonth(TDateTime dateFrom) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(this.getData());
        int month1 = cal1.get(Calendar.YEAR) * 12 + cal1.get(Calendar.MONTH);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dateFrom.getData());
        int month2 = cal2.get(Calendar.YEAR) * 12 + cal2.get(Calendar.MONTH);

        return month1 - month2;
    }

    public int compareYear(TDateTime dateFrom) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(this.getData());
        int year1 = cal1.get(Calendar.YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dateFrom.getData());
        int year2 = cal2.get(Calendar.YEAR);

        return year1 - year2;
    }

    public TDate asDate() {
        return new TDate(this.data);
    }

    public TDateTime incSecond(int value) {
        TDateTime result = this.clone();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getData());
        cal.set(Calendar.SECOND, value + cal.get(Calendar.SECOND));
        result.setData(cal.getTime());
        return result;
    }

    public TDateTime incMinute(int value) {
        TDateTime result = this.clone();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getData());
        cal.set(Calendar.MINUTE, value + cal.get(Calendar.MINUTE));
        result.setData(cal.getTime());
        return result;
    }

    public TDateTime incHour(int value) {
        TDateTime result = this.clone();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getData());
        cal.set(Calendar.HOUR_OF_DAY, value + cal.get(Calendar.HOUR_OF_DAY));
        result.setData(cal.getTime());
        return result;
    }

    public TDateTime incDay(int value) {
        TDateTime result = this.clone();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getData());
        cal.set(Calendar.DAY_OF_MONTH, value + cal.get(Calendar.DAY_OF_MONTH));
        result.setData(cal.getTime());
        return result;
    }

    public TDateTime incMonth(int offset) {
        TDateTime result = this.clone();
        if (offset == 0) {
            return result;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getData());
        int day = cal.get(Calendar.DATE);
        cal.set(Calendar.DATE, 1);
        boolean isMaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH) == day;
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + offset);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (isMaxDay || day > maxDay) {
            cal.set(Calendar.DATE, maxDay);
        } else {
            cal.set(Calendar.DATE, day);
        }
        result.setData(cal.getTime());
        return result;
    }

    @Deprecated
    public TDateTime addDay(int value) {
        return this.incDay(value);
    }

    // 返回value的当月第1天

    public TDateTime monthBof() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getData());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TDateTime tdt = new TDateTime();
        tdt.setData(cal.getTime());
        return tdt;
    }

    public TDateTime monthEof() {
        // 返回value的当月最后1天
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getData());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TDateTime tdt = new TDateTime();
        tdt.setData(cal.getTime());
        return tdt;
    }

    public int getMonth() {
        // 返回value的月值 1-12
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.data);
        return cal.get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        // 返回value的日值
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.data);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getHours() {
        // 返回value的小时值
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.data);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取指定日期的开始时刻
     */
    public static TDateTime getStartOfDay(TDateTime dateTime) {
        Date date = dateTime.getData();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        Date start = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        return new TDateTime(start);
    }

    /**
     * 获取指定日期的结束时刻
     */
    public static TDateTime getEndOfDay(TDateTime dateTime) {
        Date date = dateTime.getData();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        Date end = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
        return new TDateTime(end);
    }

    public int getMinutes() {
        // 返回value的分钟值
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.data);
        return cal.get(Calendar.MINUTE);
    }

    // 返回农历日期
    public String getGregDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getData());
        Lunar lunar = new Lunar(cal);
        return lunar.toString().substring(5).replaceAll("-", "/");
    }

    @Override
    public int compareTo(TDateTime tdt) {
        if (tdt == null) {
            return 1;
        }
        if (tdt.getData().getTime() == this.getData().getTime()) {
            return 0;
        } else {
            return this.getData().getTime() > tdt.getData().getTime() ? 1 : -1;
        }
    }

    @Override
    public TDateTime clone() {
        return new TDateTime(this.getData());
    }

    public boolean isNull() {
        return this.data == null;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        TDateTime date = TDateTime.fromDate("2016-02-28 08:00:01");
        System.out.println(date.getDay());
        System.out.println(date.getTimestamp());
        System.out.println(date.getUnixTimestamp());
        System.out.println(date);
        System.out.println(date.incMonth(1));
        System.out.println(date.incMonth(2));
        System.out.println(date.incMonth(3));
        System.out.println(date.incMonth(4));
        System.out.println(date.incMonth(12));
        System.out.println(date.incMonth(13));
        System.out.println(date);

        TDateTime date2 = TDateTime.fromDate("2016-05-31 23:59:59");
        System.out.println(date2);
        System.out.println(date2.incMonth(1));
        System.out.println(date2.incMonth(1).monthBof());

        System.out.println(isInterval("05:30", "17:00"));
    }
}
