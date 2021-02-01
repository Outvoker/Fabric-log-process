package org.fudan.logProcess.monitor;

import org.apache.commons.lang3.StringUtils;
import org.fudan.logProcess.entity.TestRecord;
import org.fudan.logProcess.service.LogHandler;
import org.fudan.logProcess.service.NewLineHandler;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Xu Rui
 * @date 2021/1/26 10:15
 */
public class Monitor {
    private transient NewLineHandler handler;

    private transient int day = 0;

    private transient int fileNum;

    private transient String fileName;

    private transient BufferedRandomAccessFile[] logFile;

    private transient boolean isFromBegin;

    private transient String fileFmt;

    public Monitor (NewLineHandler handler, String fileFmt, int fileNum, String fileName) throws IOException {
        this(handler, fileFmt, false, fileNum, fileName);
    }

    public Monitor (NewLineHandler handler, String fileFmt, boolean isFromBegin, int fileNum, String fileName) throws IOException {
        this(handler, fileFmt, isFromBegin, fileNum);
        this.fileName = fileName;
    }

    public Monitor (NewLineHandler handler, String fileFmt, boolean isFromBegin, int fileNum) throws IOException {
        this.handler = handler;
        this.isFromBegin = isFromBegin;
        this.fileFmt = fileFmt;
        this.fileNum = fileNum;
        init();
    }

    CountDownLatch begin = new CountDownLatch(fileNum);
    CountDownLatch end   = new CountDownLatch(fileNum);

    public void run () throws IOException {
        List<String>[] lines = new ArrayList[fileNum];
        List<List<TestRecord>> res = new ArrayList<>();
        for (int i = 0; i < fileNum; i++) {
            lines[i] = new ArrayList<>();
            for (; ; ) {
                if (isChange()) {
                    init();
                }

                String newLine = null;
                try {
                    newLine = logFile[i].readLine();
                } catch (Exception e) {
                }

                if (StringUtils.isNotBlank(newLine)) {
                    lines[i].add(newLine);
                }

                if ((lines[i].size() >= MAX_SIZE && isReadFromBegin) || logFile[i].getFilePointer() == logFile[i].length()) {
                    final Integer I = i;
                    new Thread(() -> {
                        try {
                            begin.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        res.add(doTest(lines[I],I));
                        end.countDown();
                    }).start();

                    begin.countDown();
                    break;
                }
            }
        }

        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i = 0;
        for (List<TestRecord> recordList : res) {
            System.out.println("**************** LOG FILE:" + (i + 1) + "*************************");
            List<Integer> x = new ArrayList<>();
            List<Long> y = new ArrayList<>();
            for (TestRecord record : recordList) {
                record.showResult();
                x.add(record.getSize());
                y.add(record.getEnd() - record.getStart());
            }
            System.out.println("x:" + x);
            System.out.println("y:" + y);
            System.out.println("total cost:" + (recordList.get(recordList.size() - 1).getEnd() - recordList.get(0).getStart()));
            i++;
        }

    }

    private static final Integer start    = 1000;
    private static final Integer step     = 1000;
    private static final Integer MAX_SIZE = 10000;
    private static final boolean isReadFromBegin = true;

    CountDownLatch sync = new CountDownLatch(fileNum);

    /**
     * 开始做实验
     */
    private List<TestRecord> doTest (List<String> lines,int taskNumber) {
        List<List<String>> data = splitTestData(lines, isReadFromBegin);

        boolean isFirst = true;
        List<TestRecord> records = new ArrayList<>();
        for (List<String> spiltLines : data) {
            if (isFirst) isFirst = false;
            else {
                try {
                    sync.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            sync = new CountDownLatch(fileNum);
            TestRecord record = new TestRecord();
            record.setSize(spiltLines.size());
            record.setStartDate(new Date());
            record.setStart(System.currentTimeMillis());
            try {
                handler.handle(spiltLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
            record.setEnd(System.currentTimeMillis());

            //每次结束打印一下输出时间
            record.showResult();

            records.add(record);
            try {
                Thread.sleep(30000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sync.countDown();
        }

        return records;
    }

    private List<List<String>> splitTestData (List<String> lines, boolean isFromBegin) {
        if (!isFromBegin) return splitTestData(lines);

        List<List<String>> data = new ArrayList<>();
        for (int i = start; i <= MAX_SIZE; i += step) {
            data.add(lines.subList(0, i));
        }

        return data;
    }

    private List<List<String>> splitTestData (List<String> lines) {
        List<List<String>> data = new ArrayList<>();

        int pNo = 1;
        int preIndex = 0;
        for (; ; ) {
            int nextIndex = start + (pNo - 1)*step + preIndex;

            if (nextIndex > lines.size()) break;

            data.add(lines.subList(preIndex, nextIndex));

            preIndex = nextIndex;
            pNo++;
        }

        return data;
    }

    private void init () throws IOException {
        DateTime dt = new DateTime();
        day = dt.getDayOfMonth();
        if (logFile != null) {
            for (BufferedRandomAccessFile file : logFile) {
                file.close();
            }
        }


        boolean fileIsReady = false;
        for (; !fileIsReady; ) {

            fileIsReady = true;

            logFile = new BufferedRandomAccessFile[fileNum];
            for (int i = 1; i <= fileNum; i++) {
//                String file = String.format(fileFmt, dt.toString("yyyyMMdd") + "_" + i);//根据日期生成文件名
                String file = String.format(fileFmt, "20200725" + "_" + i);//根据日期生成文件名
                if (!new File(file).exists()) {
                    fileIsReady = false;
                    break;
                }
                try {
                    logFile[i - 1] = new BufferedRandomAccessFile(file, "r");
                } catch (IOException e) {
                    fileIsReady = false;
                    continue;
                }

            }

            if (!fileIsReady) continue;

            for (int i = 1; i <= fileNum; i++) {
                if (logFile[i - 1] == null) {
                    fileIsReady = false;
                    continue;
                }
            }

        }
    }

    private boolean isChange () {
        return new DateTime().getDayOfMonth() != day;
    }

    public static void main (String[] args) throws IOException {
//        if(OSinfo.isLinux())
//            logFilePathFmt = "/home/ubuntu/%s";
//        else
//            logFilePathFmt = "D:\\university\\blockchain\\logTest\\%s";

        //Input the path of policy

        String policyPath = "./src/main/java/cn/fudan/lib/ledger/test.yml"; //the path of policy
        int fileNum = 8;    //the number of file that need to be monitored
        String fileName = null;   //if not set, using today's date.
//        String fileName = "20201106";   //if not set, using today's date.

        NewLineHandler handler = new LogHandler(policyPath);
        LogHandler test  = (LogHandler) handler;
        String logFilePathFmt = test.getLogConfig().getInfo().getFilePath() + "%s";
        if (fileName == null)
            new Monitor(handler, logFilePathFmt, true, fileNum).run();
        else
            new Monitor(handler, logFilePathFmt, true, fileNum, fileName).run();
    }
}
