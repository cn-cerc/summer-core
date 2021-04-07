package cn.cerc.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期对象
 *
 * @author 张弓
 */
public class TDate extends TDateTime {
    private static ClassResource res = new ClassResource(TDate.class, "summer-core");
    private static final long serialVersionUID = 1L;

    public TDate(Date date) {
        this.setData(date);
    }

    public TDate(String date) {
        TDateTime val = TDateTime.fromDate(date);
        if (val == null) {
            throw new RuntimeException(String.format(res.getString(1, "%s 不是一个有效的日期格式！"), date));
        }
        this.setData(val.getData());
    }

    @Deprecated
    public static TDate Today() {
        return TDate.today();
    }

    // 当天，不带时分秒
    public static TDate today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = sdf.format(date);
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(res.getString(2, "日期类型转换错误"));
        }
        return new TDate(date);
    }

    public static void main(String[] args) {
        TDate val;
        val = new TDate(TDateTime.now().incMonth(-13).getData());
        System.out.println(val.getShortValue());
        val = TDate.today();
        System.out.println(val.getShortValue());
    }

    @Override
    public String toString() {
        return this.getDate();
    }

    public String getShortValue() {
        String year = this.getYearMonth().substring(2, 4);
        int month = this.getMonth();
        int day = this.getDay();
        if (TDateTime.now().compareYear(this) != 0) {
            return String.format("%s年%d月", year, month);
        } else {
            return String.format("%d月%d日", month, day);
        }
    }
}
