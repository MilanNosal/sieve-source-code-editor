package sk.tuke.kpi.monitor.logging;

import java.util.Calendar;

/**
 *
 * @author Milan
 */
public class SOUTLogger extends Logger {

    private final Calendar calendar = Calendar.getInstance();
    
    @Override
    public void startSession(String userId, String sessionName) {
        long millis = System.currentTimeMillis();
        calendar.setTimeInMillis(millis);
        System.out.print(super.startSessionString(sessionName, userId, calendar, millis));
    }

    @Override
    public void endSession() {
        System.out.print(super.endSessionString());
    }

    @Override
    public void logEntry(EventType eventType, String project, String sourceId) {
        long millis = System.currentTimeMillis();
        calendar.setTimeInMillis(millis);
        System.out.print(super.logEntryString(eventType, project, sourceId, calendar, millis));
    }

    @Override
    public void endLogging() {
        System.out.println("Logging ended");
    }
    
}
