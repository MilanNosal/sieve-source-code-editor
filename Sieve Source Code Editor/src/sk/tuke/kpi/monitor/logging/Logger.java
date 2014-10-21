package sk.tuke.kpi.monitor.logging;

import java.util.Calendar;

/**
 *
 * @author Milan
 */
public abstract class Logger {

    public enum EventType {

        PROJECTIONS_STARTED,
        PROJECTIONS_ENDED,
        CURRENT_PROJECTIONS_CHANGED,
        EDITOR_FOCUS_GAINED,
        DOCUMENT_EDITED,
        FOCUS_GAINED
    }

    public abstract void startSession(String userId, String sessionName);

    public abstract void endSession();

    public abstract void logEntry(EventType eventType, String project, String sourceId);

    public abstract void endLogging();

    // <editor-fold desc="Logfile format" defaultstate="collapsed">
    protected String logEntryString(EventType eventType, String project,
            String sourceId, Calendar timestamp, long millis) {
        if (project == null) {
            project = "UNKNOWN";
        }
        return String.format(
                "\t\t<event date=\"%1$tF %1$tT\" timestamp=\"%5$d\">\n"
                + "\t\t\t<type>%2$s</type>\n"
                + "\t\t\t<project>%3$s</project>\n"
                + "\t\t\t<source>%4$s</source>\n"
                + "\t\t</event>\n",
                timestamp, eventType, project, sourceId, millis);
    }

    protected String startSessionString(String sessionName, String userId,
            Calendar timestamp, long millis) {
        return String.format(
                "\t<session user=\"%2$s\" session=\"%1$s\" date=\"%3$tF %3$tT\" timestamp=\"%4$d\">\n",
                sessionName, userId, timestamp, millis);
    }

    protected String endSessionString() {
        return "\t</session>\n";
    }

    protected String startLogFileString() {
        return "<userInteractionMonitor>\n";
    }

    protected String endLogFileString() {
        return "</userInteractionMonitor>\n";
    }
    // </editor-fold>
}
