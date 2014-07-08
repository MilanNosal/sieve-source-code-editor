/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.kpi.ssce.core.utilities;

import java.util.*;

/**
 * Trieda predstavuje nastroj pre pracu so zamermi ulozenymi v komentari.
 * @author Matej Nosal
 */
//SsceIntent:Komentar uchovavajuci zamer;
@Deprecated
public class IntentsUtilities {

    /**
     * Vrati mnozinu zamerov ulozenych v komentari intentsComment.
     * @param intentsComment komentar uchovavajuci zamer.
     * @return mnozinu zamerov ulozenych v komentari.
     */
    
    public Set<String> getIntents(String intentsComment) {
        Set<String> intents = new HashSet<String>();
        if (intentsComment != null) {
            String[] parts = intentsComment.split(":");
            if (parts.length == 2) {
                String[] intentStrings = parts[1].split("\\s*;\\s*");
                intents.addAll(Arrays.asList(intentStrings));
            }
        }

        return intents;
    }

    /**
     * Vrati text, reprezentujuci zamery ulozene v komentari ako jeden citaleny retazec.
     * @param intentsComment komentar uchovavajuci zamer.
     * @param start prefix retazca.
     * @param separator odelovac medzi zamermi.
     * @param end sufix retazca.
     * @return text, reprezentujuci zamery ulozene v komentari ako jeden citaleny retazec.
     */
    public String getIntents(String intentsComment, String start, String separator, String end) {
        if (start == null) {
            start = "";
        }
        if (separator == null) {
            separator = ", ";
        }
        if (end == null) {
            end = "";
        }
        List<String> intents = new ArrayList<String>(getIntents(intentsComment));
        Collections.sort(intents);
        StringBuilder builder = new StringBuilder(start);
        for (int i = 0; i < intents.size(); i++) {
            builder.append(intents.get(i));
            if (i < intents.size() - 1) {
                builder.append(separator);
            }
        }
        builder.append(end);
        return builder.toString();
    }

    /**
     * Vytvori telo komentara uchovavajuceho zamer.
     * @param intents mnozina zamerov.
     * @param withNewLine ci sa ma na konci koemntara generovat novy riadok.
     * @return telo komentara uchovavajuceho zamer.
     */
    public String getIntentsComment(Set<String> intents, boolean withNewLine) {
        if (intents.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("//SsceIntent:");
        for (Iterator<String> it = intents.iterator(); it.hasNext();) {
            String intent = it.next();
            builder.append(intent.trim()).append(";");
        }
        if (withNewLine) {
            builder.append("\n");
        }
        return builder.toString();
    }
}
