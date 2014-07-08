package sk.tuke.kpi.ssce.core.configuration;

import java.io.Serializable;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionChange;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionComposition;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionConfiguration;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 * Konfiguracia zamerov, ktora v podstate predstavuje dopyt na zdrojovy kod s urcitym zamerom.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
@ProjectionConfiguration
public class CurrentProjection implements Serializable {

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation=true)
    private final Set<CurrentProjectionChangeListener> listeners = new HashSet<CurrentProjectionChangeListener>();

    /**
     * Prida listener, ktory bude reagovat na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory bude reagovat na zmeny.
     * @return true, ak listener je pridany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation=true)
    public boolean addConfigurationChangeListener(CurrentProjectionChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * Odoberie listener, ktory reaguje na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory reaguje na zmeny.
     * @return true, ak listener je odobrany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation=true)
    public boolean removeConfigurationChangeListener(CurrentProjectionChangeListener listener) {
        return listeners.remove(listener);
    }

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation=true)
    private void fireConfigurationChanged(IntentsConfigurationChangedEvent event) {
        if (event == null) {
            return;
        }
        for (CurrentProjectionChangeListener listener : listeners) {
            listener.configurationChanged(event);
        }
    }

    /**
     * Konstanta pre mod AND, pri vytvarani dopytu (konfiguracie zamerov).
     */
    //SsceIntent:Konstanta;
    @ProjectionConfiguration
    @ProjectionComposition
    public static final String MODE_AND = "And";
     /**
     * Konstanta pre mod OR, pri vytvarani dopytu (konfiguracie zamerov).
     */
    //SsceIntent:Konstanta;
    @ProjectionConfiguration
    @ProjectionComposition
    public static final String MODE_OR = "Or";
        
    @ProjectionConfiguration
    @ProjectionComposition
    private String mode = MODE_AND;
    
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @ProjectionConfiguration
    private final Set<Searchable> currentlySelectedConcerns = new HashSet<Searchable>();

    /**
     * Vrati zvolene zamery ako nemodifikovatelnu mnozinu.
     * @return zvolene zamery ako nemodifikovatelnu mnozinu.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @ProjectionConfiguration
    public Set<Searchable> getCurrentlySelectedConcerns() {
        return Collections.unmodifiableSet(currentlySelectedConcerns);
    }

    /**
     * Nastavi novu mnozinu zamerov.
     * @param selectedIntents nova mnozina zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfiguration
    @ProjectionChange
    public void setSelectedIntents(Set<Searchable> selectedIntents) {
        this.currentlySelectedConcerns.clear();
        this.currentlySelectedConcerns.addAll(selectedIntents);
        fireConfigurationChanged(new IntentsConfigurationChangedEvent(this));
    }

    /**
     * Vrati mod konfiguracie zamerov (dopytu).
     * @return mod konfiguracie zamerov (dopytu).
     */
    @ProjectionConfiguration
    public String getMode() {
        return mode;
    }

    /**
     * Nastavi mod konfiguracie zamerov (dopytu).
     * @param mode mod konfiguracie zamerov (dopytu).
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfiguration
    @ProjectionChange
    public void setMode(String mode) {
        this.mode = mode;
        fireConfigurationChanged(new IntentsConfigurationChangedEvent(this));
    }

    /**
     * Event pre zmenu v konfiguracii zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation = true)
    public static class IntentsConfigurationChangedEvent {

        @ProjectionConfiguration
        private final CurrentProjection newCurrentProjection;

        /**
         * Vytvori event pre zmenu v konfiguracii zamerov.
         * @param configuration nova konfiguracia zamerov.
         */
        @ProjectionChange(propagation = true)
        public IntentsConfigurationChangedEvent(CurrentProjection configuration) {
            this.newCurrentProjection = configuration;
        }

        /**
         * Vrati novu konfiguraciu zamerov.
         * @return novu konfiguraciu zamerov.
         */
        public CurrentProjection getConfiguration() {
            return newCurrentProjection;
        }
    }

    /**
     * Listener reagujuci na zmeny v konfiguracii zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionChange(propagation = true)
    @ProjectionConfiguration
    public static interface CurrentProjectionChangeListener extends EventListener {

        /**
         * Volana ked dojde k zmene v konfiguracii zamerov.
         * @param event event
         */
        public void configurationChanged(CurrentProjection.IntentsConfigurationChangedEvent event);
    }
}