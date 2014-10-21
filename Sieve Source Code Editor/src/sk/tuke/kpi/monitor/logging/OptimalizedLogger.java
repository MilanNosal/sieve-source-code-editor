package sk.tuke.kpi.monitor.logging;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import sk.tuke.kpi.monitor.UserInteractionMonitorPanel;

/**
 *
 * @author Milan
 */
final class OptimalizedLogger extends Logger {

    private final File logFile;
    private final StringBuilder builder = new StringBuilder(131072); // 128 kB
    private boolean inSession = false;

    private final Calendar calendar = Calendar.getInstance();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final WriterTask writerTask = new WriterTask();
    private final ScheduledFuture<?> writerTaskHandle;

    public OptimalizedLogger() {
        String filePath = UserInteractionMonitorPanel.getOutputPath();
        logFile = new File(filePath);
        this.writerTaskHandle = this.scheduler.scheduleAtFixedRate(this.writerTask, 90, 90, TimeUnit.SECONDS);
    }

    @Override
    public void startSession(String userId, String sessionName) {
        synchronized (builder) {
            if (inSession) {
                builder.append(endSessionString());
            }

            long millis = System.currentTimeMillis();
            calendar.setTimeInMillis(millis);
            builder.append(startSessionString(sessionName, userId, calendar, millis));
            inSession = true;
        }
    }

    @Override
    public void endSession() {
        synchronized (builder) {
            if (inSession) {
                builder.append(endSessionString());
            }

            inSession = false;
        }
    }

    @Override
    public void logEntry(EventType eventType, String project, String sourceId) {
        synchronized (builder) {
            if (inSession) {
                long millis = System.currentTimeMillis();
                calendar.setTimeInMillis(millis);
                builder.append(logEntryString(eventType, project, sourceId, calendar, millis));
            }
        }
    }

    @Override
    public void endLogging() {
        synchronized (builder) {
            if (inSession) {
                builder.append(endSessionString());
            }
            builder.append(endLogFileString());
            inSession = false;
        }
        this.writerTaskHandle.cancel(false);
        // just in case
        this.writerTask.save();
    }

    // <editor-fold desc="Writer periodic task" defaultstate="collapsed">
    private class WriterTask implements Runnable {

        private RandomAccessFile raf;

        /**
         * Metoda spracovava zmeny v java suboroch a posle iba jednu namiesto
         * mnohych. Ukonci sa az po zavolani stop().
         */
        @Override
        public void run() {
            try {
                synchronized (builder) {
                    if (builder.length() > 1000) {
                        if (raf == null) {
                            prepareLogFile();
                        }
                        raf.write(builder.toString().getBytes("UTF-8"));
                        builder.setLength(0);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void save() {
            try {
                synchronized (builder) {
                    if (raf == null) {
                        prepareLogFile();
                    }
                    raf.write(builder.toString().getBytes("UTF-8"));
                    builder.setLength(0);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void prepareLogFile() throws IOException {
            if (raf == null) {
                if (logFile.exists()) { // append
                    raf = new RandomAccessFile(logFile, "rw");
                    if (raf.length() < 49) { // arcane constant for determining valid log file
                        raf.seek(0);
                        raf.write(startLogFileString().getBytes("UTF-8"));
                    } else {
                        raf.seek(raf.length() - endLogFileString().length());
                    }
                } else { // create new
                    raf = new RandomAccessFile(logFile, "rw");
                    raf.write(startLogFileString().getBytes("UTF-8"));
                }
            } // here it should be ready for writing a session
        }
    }
    // </editor-fold>
}
