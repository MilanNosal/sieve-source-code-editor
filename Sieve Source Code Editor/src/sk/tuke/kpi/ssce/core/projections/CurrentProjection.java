package sk.tuke.kpi.ssce.core.projections;

import java.io.Serializable;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionConfigurationChange;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionComposition;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionConfiguration;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Konfiguracia zamerov, ktora v podstate predstavuje dopyt na zdrojovy kod s urcitym zamerom.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
@ProjectionConfiguration
public class CurrentProjection implements Serializable {

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfigurationChange(propagation=true)
    private final Set<CurrentProjectionChangeListener> listeners = new HashSet<CurrentProjectionChangeListener>();

    /**
     * Prida listener, ktory bude reagovat na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory bude reagovat na zmeny.
     * @return true, ak listener je pridany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfigurationChange(propagation=true)
    public boolean addCurrentProjectionChangeListener(CurrentProjectionChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * Odoberie listener, ktory reaguje na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory reaguje na zmeny.
     * @return true, ak listener je odobrany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfigurationChange(propagation=true)
    public boolean removeCurrentProjectionChangeListener(CurrentProjectionChangeListener listener) {
        return listeners.remove(listener);
    }

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfigurationChange(propagation=true)
    private void fireCurrentProjectionChange(CurrentProjectionChangedEvent event) {
        if (event == null) {
            return;
        }
        for (CurrentProjectionChangeListener listener : listeners) {
            listener.projectionChanged(event);
        }
    }
        
    @ProjectionConfiguration
    @ProjectionComposition
    private Map<String, Object> params = new HashMap<String, Object>();
    
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @ProjectionConfiguration
    @SourceCodeSieving
    private final Set<Concern> currentlySelectedConcerns = new HashSet<Concern>();

    /**
     * Vrati zvolene zamery ako nemodifikovatelnu mnozinu.
     * @return zvolene zamery ako nemodifikovatelnu mnozinu.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @ProjectionConfiguration
    @SourceCodeSieving
    public Set<Concern> getCurrentlySelectedConcerns() {
        return Collections.unmodifiableSet(currentlySelectedConcerns);
    }

    /**
     * Nastavi novu mnozinu zamerov.
     * @param selectedConcerns nova mnozina zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfiguration
    @ProjectionConfigurationChange
    public void setSelectedConcerns(Set<Concern> selectedConcerns) {
        this.currentlySelectedConcerns.clear();
        this.currentlySelectedConcerns.addAll(selectedConcerns);
        fireCurrentProjectionChange(new CurrentProjectionChangedEvent(this));
    }

    /**
     * Vrati mod konfiguracie zamerov (dopytu).
     * @return mod konfiguracie zamerov (dopytu).
     */
    @ProjectionConfiguration
    @SourceCodeSieving
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Nastavi mod konfiguracie zamerov (dopytu).
     * @param params mod konfiguracie zamerov (dopytu).
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfiguration
    @ProjectionConfigurationChange
    public void setParams(Map<String, Object> params) {
        this.params = params;
        fireCurrentProjectionChange(new CurrentProjectionChangedEvent(this));
    }

    /**
     * Event pre zmenu v konfiguracii zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @ProjectionConfigurationChange(propagation = true)
    public static class CurrentProjectionChangedEvent {

        @ProjectionConfiguration
        private final CurrentProjection newCurrentProjection;

        /**
         * Vytvori event pre zmenu v konfiguracii zamerov.
         * @param newProjection nova konfiguracia zamerov.
         */
        @ProjectionConfigurationChange(propagation = true)
        public CurrentProjectionChangedEvent(CurrentProjection newProjection) {
            this.newCurrentProjection = newProjection;
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
    @ProjectionConfigurationChange(propagation = true)
    @ProjectionConfiguration
    public static interface CurrentProjectionChangeListener extends EventListener {

        /**
         * Volana ked dojde k zmene v konfiguracii zamerov.
         * @param event event
         */
        public void projectionChanged(CurrentProjection.CurrentProjectionChangedEvent event);
    }
}