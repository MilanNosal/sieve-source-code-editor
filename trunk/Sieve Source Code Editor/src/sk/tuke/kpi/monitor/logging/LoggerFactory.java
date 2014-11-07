package sk.tuke.kpi.monitor.logging;

/**
 *
 * @author Milan
 */
public class LoggerFactory {
    public static Logger getLogger() {
        // return new OptimalizedLogger();
        return new SOUTLogger();
    }
}
