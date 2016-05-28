/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feilong.core.date;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.feilong.core.DatePattern;
import com.feilong.core.Validator;

/**
 * 日期扩展工具类.
 * 
 * <h3>和 {@link DateUtil} 的区别:</h3>
 * 
 * <blockquote>
 * <p>
 * {@link DateUtil}是纯操作Date API的工具类,而 {@link DateExtensionUtil}类用于个性化输出结果,针对业务个性化显示.
 * </p>
 * </blockquote>
 * 
 * <h3>获得两个日期间隔:</h3>
 * 
 * <blockquote>
 * <ul>
 * <li>{@link #getIntervalDayList(String, String, String)}</li>
 * <li>{@link #getIntervalForView(long)}</li>
 * <li>{@link #getIntervalForView(Date, Date)}</li>
 * </ul>
 * </blockquote>
 * 
 * @author feilong
 * @version 1.0.8 2014年7月31日 下午2:34:33
 * @since 1.0.8
 */
public final class DateExtensionUtil{

    /** 昨天. */
    private static final String YESTERDAY               = "昨天";

    /** 前天. */
    private static final String THEDAY_BEFORE_YESTERDAY = "前天";

    /** 天. */
    private static final String DAY                     = "天";

    /** 小时. */
    private static final String HOUR                    = "小时";

    /** 分钟. */
    private static final String MINUTE                  = "分钟";

    /** 秒. */
    private static final String SECOND                  = "秒";

    /** 毫秒. */
    private static final String MILLISECOND             = "毫秒";

    /** Don't let anyone instantiate this class. */
    private DateExtensionUtil(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    // [start] 获得时间 /时间数组,可以用于sql查询
    /**
     * 获得重置清零的今天和明天,当天0:00:00及下一天0:00:00.
     * 
     * <p>
     * 一般用于统计当天数据,between ... and ...
     * </p>
     * 
     * <pre class="code">
     * 比如今天是 2012-10-16 22:18:34
     * 
     * return {2012-10-16 00:00:00.000,2012-10-17 00:00:00.000}
     * </pre>
     * 
     * @return Date数组 第一个为today 第二个为tomorrow
     */
    public static Date[] getResetTodayAndTomorrow(){
        Calendar calendar = CalendarUtil.resetDayBegin(new Date());
        Date today = calendar.getTime();
        return new Date[] { today, DateUtil.addDay(today, 1) };
    }

    /**
     * 获得重置清零的昨天和今天 [yestoday,today].
     * 
     * <p>
     * 第一个为昨天00:00 <br>
     * 第二个为今天00:00 <br>
     * 一般用于sql/hql统计昨天数据,between ... and ...
     * </p>
     * 
     * <pre class="code">
     * 比如现在 :2012-10-16 22:46:42
     * 
     * return  {2012-10-15 00:00:00.000,2012-10-16 00:00:00.000}
     * </pre>
     * 
     * @return Date数组 <br>
     *         第一个为昨天00:00 <br>
     *         第二个为今天00:00
     */
    public static Date[] getResetYesterdayAndToday(){
        Calendar calendar = CalendarUtil.resetDayBegin(new Date());
        Date today = calendar.getTime();
        return new Date[] { DateUtil.addDay(today, -1), today };
    }

    // [end]
    /**
     * 获得两个日期时间的日期间隔时间集合(包含最小和最大值),用于统计日报表.
     * 
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * getIntervalDayList("2011-03-5 23:31:25.456", "2011-03-10 01:30:24.895", DatePattern.commonWithTime)
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
    2011-03-05 00:00:00
    2011-03-06 00:00:00
    2011-03-07 00:00:00
    2011-03-08 00:00:00
    2011-03-09 00:00:00
    2011-03-10 00:00:00
     * </pre>
     * 
     * </blockquote>
     * 
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>每天的日期会被重置清零 <code>00:00:00.000</code></li>
     * <li>方法自动辨识 <code>fromDateString</code>和 <code>toDateString</code>哪个是开始时间</li>
     * </ol>
     * </blockquote>
     * 
     * @param fromDateString
     *            开始时间
     * @param toDateString
     *            结束时间
     * @param datePattern
     *            时间模式 {@link DatePattern}
     * @return the interval day list<br>
     *         如果 <code>fromDateString</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>fromDateString</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     *         如果 <code>toDateString</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>toDateString</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     *         如果 <code>datePattern</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>datePattern</code> 是blank,抛出 {@link IllegalArgumentException}
     * @see #getIntervalDayList(Date, Date)
     */
    public static List<Date> getIntervalDayList(String fromDateString,String toDateString,String datePattern){
        Validate.notBlank(fromDateString, "fromDateString can't be null/empty!");
        Validate.notBlank(toDateString, "toDateString can't be null/empty!");
        Validate.notBlank(datePattern, "datePattern can't be null/empty!");

        Date fromDate = DateUtil.string2Date(fromDateString, datePattern);
        Date toDate = DateUtil.string2Date(toDateString, datePattern);

        return getIntervalDayList(fromDate, toDate);
    }

    /**
     * 获得两个日期时间的日期间隔时间集合(包含最小和最大值),用于统计日报表.
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * Date fromDate = DateUtil.string2Date("2011-03-5 23:31:25.456", DatePattern.COMMON_DATE_AND_TIME);
     * Date toDate = DateUtil.string2Date("2011-03-10 01:30:24.895", DatePattern.COMMON_DATE_AND_TIME);
     * LOGGER.debug(JsonUtil.format(DateExtensionUtil.getIntervalDayList(fromDate, toDate)));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     ["2011-03-05 00:00:00",
      "2011-03-06 00:00:00",
      "2011-03-07 00:00:00",
      "2011-03-08 00:00:00",
      "2011-03-09 00:00:00",
      "2011-03-10 00:00:00"
      ]
     * </pre>
     * 
     * </blockquote>
     * 
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>每天的日期会被重置清零 <code>00:00:00.000</code></li>
     * <li>方法自动辨识 <code>fromDate</code>和 <code>toDate</code>哪个是开始时间</li>
     * </ol>
     * </blockquote>
     * 
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @return the interval day list <br>
     *         如果 <code>fromDate</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>toDate</code> 是null,抛出 {@link NullPointerException}
     * @see DateUtil#getIntervalDay(Date, Date)
     * @see org.apache.commons.lang3.time.DateUtils#iterator(Calendar, int)
     * @since 1.5.4
     */
    public static List<Date> getIntervalDayList(Date fromDate,Date toDate){
        Validate.notNull(fromDate, "fromDate can't be null!");
        Validate.notNull(toDate, "toDate can't be null!");

        Date minDate = fromDate.before(toDate) ? fromDate : toDate;
        Date maxDate = fromDate.before(toDate) ? toDate : fromDate;

        // ******重置时间********
        Date beginDateReset = DateUtil.getFirstDateOfThisDay(minDate);
        Date endDateReset = DateUtil.getLastDateOfThisDay(maxDate);

        List<Date> dateList = new ArrayList<Date>();
        dateList.add(beginDateReset);

        // 相隔的天数
        int intervalDay = DateUtil.getIntervalDay(beginDateReset, endDateReset);
        for (int i = 0; i < intervalDay; ++i){
            dateList.add(DateUtil.addDay(beginDateReset, i + 1));
        }

        return dateList;
    }

    /**
     * 获得一年中所有的 指定的 <code>week</code> 周几集合.
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <p>
     * 如果当前年是 2016 年,那么
     * </p>
     * 
     * <pre class="code">
     * getWeekDateStringList(Calendar.THURSDAY, DatePattern.COMMON_DATE_AND_TIME_WITH_MILLISECOND)
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * [
     * "2016-01-07 00:00:00.000",
     * "2016-01-14 00:00:00.000",
     * "2016-01-21 00:00:00.000",
     * "2016-01-28 00:00:00.000",
     * "2016-02-04 00:00:00.000",
     * "2016-02-11 00:00:00.000",
     * "2016-02-18 00:00:00.000",
     * "2016-02-25 00:00:00.000",
     * "2016-03-03 00:00:00.000",
     * "2016-03-10 00:00:00.000",
     * "2016-03-17 00:00:00.000",
     * "2016-03-24 00:00:00.000",
     * "2016-03-31 00:00:00.000",
     * "2016-04-07 00:00:00.000",
     * "2016-04-14 00:00:00.000",
     * "2016-04-21 00:00:00.000",
     * "2016-04-28 00:00:00.000",
     * "2016-05-05 00:00:00.000",
     * "2016-05-12 00:00:00.000",
     * "2016-05-19 00:00:00.000",
     * "2016-05-26 00:00:00.000",
     * "2016-06-02 00:00:00.000",
     * "2016-06-09 00:00:00.000",
     * "2016-06-16 00:00:00.000",
     * "2016-06-23 00:00:00.000",
     * "2016-06-30 00:00:00.000",
     * "2016-07-07 00:00:00.000",
     * "2016-07-14 00:00:00.000",
     * "2016-07-21 00:00:00.000",
     * "2016-07-28 00:00:00.000",
     * "2016-08-04 00:00:00.000",
     * "2016-08-11 00:00:00.000",
     * "2016-08-18 00:00:00.000",
     * "2016-08-25 00:00:00.000",
     * "2016-09-01 00:00:00.000",
     * "2016-09-08 00:00:00.000",
     * "2016-09-15 00:00:00.000",
     * "2016-09-22 00:00:00.000",
     * "2016-09-29 00:00:00.000",
     * "2016-10-06 00:00:00.000",
     * "2016-10-13 00:00:00.000",
     * "2016-10-20 00:00:00.000",
     * "2016-10-27 00:00:00.000",
     * "2016-11-03 00:00:00.000",
     * "2016-11-10 00:00:00.000",
     * "2016-11-17 00:00:00.000",
     * "2016-11-24 00:00:00.000",
     * "2016-12-01 00:00:00.000",
     * "2016-12-08 00:00:00.000",
     * "2016-12-15 00:00:00.000",
     * "2016-12-22 00:00:00.000",
     * "2016-12-29 00:00:00.000"
     * ]
     * </pre>
     * 
     * </blockquote>
     * 
     * 
     * @param week
     *            周几<br>
     *            星期天开始为1 依次2 3 4 5 6 7,<br>
     *            建议使用 常量 {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
     *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY}, {@link Calendar#SATURDAY}
     * @param datePattern
     *            获得集合里面时间字符串模式 see {@link DatePattern}
     * @return 获得一年中所有的周几集合<br>
     *         如果 <code>datePattern</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>datePattern</code> 是blank,抛出 {@link IllegalArgumentException}
     * @see org.apache.commons.lang3.time.DateUtils#iterator(Date, int)
     * @see Calendar#SUNDAY
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     */
    public static List<String> getWeekDateStringList(int week,String datePattern){
        Validate.notBlank(datePattern, "datePattern can't be blank!");
        Date now = new Date();
        Date firstWeekOfSpecifyDateYear = DateUtil.getFirstWeekOfSpecifyDateYear(now, week);
        //当年最后一天
        Calendar calendarEnd = CalendarUtil.resetYearEnd(DateUtil.toCalendar(now));

        List<String> list = new ArrayList<String>();
        Calendar firstWeekOfSpecifyDateYearCalendar = DateUtil.toCalendar(firstWeekOfSpecifyDateYear);
        for (Calendar calendar = firstWeekOfSpecifyDateYearCalendar; calendar.before(calendarEnd); calendar.add(Calendar.DAY_OF_YEAR, 7)){
            list.add(CalendarUtil.toString(calendar, datePattern));
        }
        return list;
    }

    // [start]转换成特色时间 toPrettyDateString

    /**
     * 人性化显示date时间,依据是和现在的时间间隔.
     * 
     * <p>
     * 转换规则,将传入的inDate和 new Date()当前时间比较;当两者的时间差,(一般inDate小于当前时间 ,暂时不支持大于当前时间)
     * </p>
     * 
     * <ul>
     * <li>如果时间差为0天,<br>
     * 如果小时间隔等于0,如果分钟间隔为0,则显示间隔秒 + "秒钟前"<br>
     * 如果小时间隔等于0,如果分钟间隔不为0,则显示间隔分钟 + "分钟前"<br>
     * </li>
     * <li>如果时间差为0天,<br>
     * 如果小时间隔不等于0,如果inDate的day 和current的day 相等,则显示space_hour + "小时前"<br>
     * 如果小时间隔不等于0,如果inDate的day 和current的day不相等,则显示"昨天 " + date2String(inDate, "HH:mm")<br>
     * </li>
     * <li>如果时间差为1天,且inDate的day+1和currentDate的day 相等,则显示"昨天 HH:mm"</li>
     * <li>如果时间差为1天,且inDate的day+1和currentDate的day 不相等,则显示"前天 HH:mm"</li>
     * <li>如果时间差为2天,且inDate的day+2和currentDate的day 相等,则显示"前天 HH:mm"</li>
     * <li>如果时间差为2天,且inDate的day+2和currentDate的day 不相等,<br>
     * 1).如果inDate的year和currentDate的year相等,则显示"MM-dd HH:mm"<br>
     * 2).如果inDate的year和currentDate的year不相等,则显示"yyyy-MM-dd HH:mm"</li>
     * <li>如果时间差大于2天<br>
     * 1).如果inDate的year和currentDate的year相等,则显示"MM-dd HH:mm"<br>
     * 2).如果inDate的year和currentDate的year不相等,则显示"yyyy-MM-dd HH:mm"</li>
     * </ul>
     * 
     * @param inDate
     *            任意日期<br>
     *            warn:一般inDate{@code <=}当前时间 ,暂时不支持大于当前时间
     * @return 人性化显示date时间<br>
     *         如果 <code>inDate</code> 是null,抛出 {@link NullPointerException}<br>
     * @see DateUtil#date2String(Date, String)
     * @see DateUtil#getYear(Date)
     * @see DateUtil#getDayOfMonth(Date)
     * @see DateUtil#getYear(Date)
     * @see DateUtil#getIntervalTime(Date, Date)
     * @see DateUtil#getIntervalDay(long)
     * @see DateUtil#getIntervalHour(long)
     * @see DateUtil#getIntervalMinute(long)
     * @see DateUtil#getIntervalSecond(long)
     * @see org.apache.commons.lang3.time.DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     */
    public static String toPrettyDateString(Date inDate){
        Validate.notNull(inDate, "inDate can't be null!");

        Date nowDate = new Date();

        // 传过来的日期的年份
        int inYear = DateUtil.getYear(inDate);
        //**************************************************************************************/
        int currentYear = DateUtil.getYear(nowDate);// 当前时间的年
        boolean isSameYear = currentYear == inYear;//是否是同一年
        long spaceTime = DateUtil.getIntervalTime(inDate, nowDate);// 任意日期和现在相差的毫秒数
        int spaceDay = DateUtil.getIntervalDay(spaceTime);// 相差天数
        //**************************************************************************************/
        switch (spaceDay) {
            case 0: // 间隔0天
                return doWithZeroDayInterval(inDate, nowDate, spaceTime);
            case 1: // 间隔一天
                return doWithOneDayInterval(inDate, nowDate);
            case 2: // 间隔2天
                return doWithTwoDaysInterval(inDate, nowDate, isSameYear);
            default://spaceDay > 2     // 间隔大于2天
                return isSameYear ? DateUtil.date2String(inDate, DatePattern.COMMON_DATE_AND_TIME_WITHOUT_YEAR_AND_SECOND)
                                : DateUtil.date2String(inDate, DatePattern.COMMON_DATE_AND_TIME_WITHOUT_SECOND);
        }
    }

    // [end]

    /**
     * 将日期集合装成特定pattern的字符串集合.
     * 
     * @param dateList
     *            日期集合
     * @param datePattern
     *            模式 {@link DatePattern}
     * @return 如果 <code>dateList</code> 是null或者empty,返回 {@link Collections#emptyList()}<br>
     *         否则循环date转成string,返回{@code List<String>}
     */
    public static List<String> toStringList(List<Date> dateList,String datePattern){
        if (Validator.isNullOrEmpty(dateList)){
            return Collections.emptyList();
        }

        Validate.notBlank(datePattern, "datePattern can't be blank!");

        List<String> stringList = new ArrayList<String>();
        for (Date date : dateList){
            stringList.add(DateUtil.date2String(date, datePattern));
        }
        return stringList;
    }

    /**
     * 将间隔毫秒数,转换成直观的表示方式.
     * 
     * <p>
     * 常用于日志输出一段代码执行时长
     * </p>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * DateExtensionUtil.getIntervalForView(13516)    return 13秒516毫秒
     * DateExtensionUtil.getIntervalForView(0)        return 0
     * 
     * 自动增加 天,小时,分钟,秒,毫秒中文文字
     * </pre>
     * 
     * </blockquote>
     * 
     * @param spaceMilliseconds
     *            总共相差的毫秒数
     * @return 将间隔毫秒数,转换成直观的表示方式.<br>
     *         如果 spaceMilliseconds 是0 直接返回0<br>
     *         如果 {@code spaceMilliseconds<0},抛出 {@link IllegalArgumentException}
     * @see DateUtil#getIntervalDay(long)
     * @see DateUtil#getIntervalHour(long)
     * @see DateUtil#getIntervalMinute(long)
     * @see DateUtil#getIntervalSecond(long)
     * @see org.apache.commons.lang3.time.DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     */
    public static String getIntervalForView(long spaceMilliseconds){
        Validate.isTrue(spaceMilliseconds >= 0, "spaceMilliseconds can't <0");

        if (0 == spaceMilliseconds){
            return "0";
        }
        // **************************************************************************************
        // 间隔天数
        long spaceDay = DateUtil.getIntervalDay(spaceMilliseconds);
        // 间隔小时 减去间隔天数后,
        long spaceHour = DateUtil.getIntervalHour(spaceMilliseconds) - spaceDay * 24;
        // 间隔分钟 减去间隔天数及间隔小时后,
        long spaceMinute = DateUtil.getIntervalMinute(spaceMilliseconds) - (spaceDay * 24 + spaceHour) * 60;
        // 间隔秒 减去间隔天数及间隔小时,间隔分钟后,
        long spaceSecond = DateUtil.getIntervalSecond(spaceMilliseconds) - ((spaceDay * 24 + spaceHour) * 60 + spaceMinute) * 60;
        // 间隔毫秒 减去间隔天数及间隔小时,间隔分钟,间隔秒后,
        long spaceMillisecond = spaceMilliseconds - (((spaceDay * 24 + spaceHour) * 60 + spaceMinute) * 60 + spaceSecond) * 1000;
        // **************************************************************************************
        StringBuilder sb = new StringBuilder();
        if (0 != spaceDay){
            sb.append(spaceDay + DAY);
        }
        if (0 != spaceHour){
            sb.append(spaceHour + HOUR);
        }
        if (0 != spaceMinute){
            sb.append(spaceMinute + MINUTE);
        }
        if (0 != spaceSecond){
            sb.append(spaceSecond + SECOND);
        }
        if (0 != spaceMillisecond){
            sb.append(spaceMillisecond + MILLISECOND);
        }
        return sb.toString();
    }

    /**
     * 将两日期之间的间隔,转换成直观的表示方式.
     * 
     * <p>
     * 常用于日志输出一段代码执行时长
     * </p>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * Date beginDate = new Date();
     * 
     * // do some logic
     * // balabala logic
     * 
     * LOGGER.info("use time:}{}", DateExtensionUtil.getIntervalForView(beginDate, new Date()));
     * 
     * </pre>
     * 
     * </blockquote>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * DateExtensionUtil.getIntervalForView(2011-05-19 8:30:40,2011-05-19 11:30:24)             return 2小时59分44秒
     * DateExtensionUtil.getIntervalForView(2011-05-19 11:31:25.456,2011-05-19 11:30:24.895)    return 1分钟1秒
     * 
     * 自动增加 天,小时,分钟,秒,毫秒中文文字
     * </pre>
     * 
     * </blockquote>
     * 
     * @param beginDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @return 将两日期之间的间隔,转换成直观的表示方式
     * @see #getIntervalForView(long)
     * @see DateUtil#getIntervalTime(Date, Date)
     */
    public static String getIntervalForView(Date beginDate,Date endDate){
        long spaceTime = DateUtil.getIntervalTime(beginDate, endDate);
        return getIntervalForView(spaceTime);
    }

    //******************************************************************************************

    /**
     * Do with one day interval.
     *
     * @param inDate
     *            the in date
     * @param nowDate
     *            the now date
     * @return the string
     * @since 1.4.0
     */
    private static String doWithOneDayInterval(Date inDate,Date nowDate){
        return DateUtil.isEquals(DateUtil.addDay(inDate, 1), nowDate, DatePattern.COMMON_DATE)
                        ? YESTERDAY + " " + DateUtil.date2String(inDate, DatePattern.COMMON_TIME_WITHOUT_SECOND)
                        : THEDAY_BEFORE_YESTERDAY + " " + DateUtil.date2String(inDate, DatePattern.COMMON_TIME_WITHOUT_SECOND);
    }

    /**
     * Do with two days interval.
     *
     * @param inDate
     *            the in date
     * @param nowDate
     *            the now date
     * @param isSameYear
     *            the is same year
     * @return the string
     * @since 1.4.0
     */
    private static String doWithTwoDaysInterval(Date inDate,Date nowDate,boolean isSameYear){
        if (DateUtil.isEquals(DateUtil.addDay(inDate, 2), nowDate, DatePattern.COMMON_DATE)){
            return THEDAY_BEFORE_YESTERDAY + " " + DateUtil.date2String(inDate, DatePattern.COMMON_TIME_WITHOUT_SECOND);
        }
        return isSameYear ? DateUtil.date2String(inDate, DatePattern.COMMON_DATE_AND_TIME_WITHOUT_YEAR_AND_SECOND)
                        : DateUtil.date2String(inDate, DatePattern.COMMON_DATE_AND_TIME_WITHOUT_SECOND);
    }

    /**
     * Do with zero day interval.
     *
     * @param inDate
     *            the in date
     * @param nowDate
     *            the now date
     * @param spaceTime
     *            the space time
     * @return the string
     * @since 1.4.0
     */
    private static String doWithZeroDayInterval(Date inDate,Date nowDate,long spaceTime){
        int spaceHour = DateUtil.getIntervalHour(spaceTime); // 相差小时数
        if (spaceHour == 0){// 小时间隔
            int spaceMinute = DateUtil.getIntervalMinute(spaceTime);
            return spaceMinute == 0 ? DateUtil.getIntervalSecond(spaceTime) + SECOND + "前" : spaceMinute + MINUTE + "前";
        }
        // 传过来的日期的日
        int inDay = DateUtil.getDayOfMonth(inDate);
        // 当前时间的日
        int currentDayOfMonth = DateUtil.getDayOfMonth(nowDate);
        return inDay == currentDayOfMonth ? spaceHour + HOUR + "前"
                        : YESTERDAY + " " + DateUtil.date2String(inDate, DatePattern.COMMON_TIME_WITHOUT_SECOND);
    }
}