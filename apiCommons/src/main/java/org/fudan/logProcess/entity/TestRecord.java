package org.fudan.logProcess.entity;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author Xu Rui
 * @date 2021/1/26 10:26
 */
public class TestRecord {
    private int  size;
    private long start;
    private long end;
    private Date startDate;

    public int getSize () {
        return size;
    }

    public void setSize (int size) {
        this.size = size;
    }

    public long getStart () {
        return start;
    }

    public void setStart (long start) {
        this.start = start;
    }

    public long getEnd () {
        return end;
    }

    public void setEnd (long end) {
        this.end = end;
    }

    public void showResult () {
        System.out.println(new DateTime(this.startDate).toString("HH:mm:ss.SSS"));
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
