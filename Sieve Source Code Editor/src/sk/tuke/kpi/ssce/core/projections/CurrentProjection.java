package sk.tuke.kpi.ssce.core.projections;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sk.tuke.kpi.ssce.annotations.concerns.CurrentProjectionChange;
import sk.tuke.kpi.ssce.annotations.concerns.Disposal;
import sk.tuke.kpi.ssce.annotations.concerns.Listening;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionComposition;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.annotations.concerns.enums.MonitoringRole;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Konfiguracia zamerov, ktora v podstate predstavuje dopyt na zdrojovy kod s urcitym zamerom.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
@sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
public class CurrentProjection<T extends Concern> implements Serializable {

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation=true)
    @Listening(monitoringRole = MonitoringRole.PUBLISHER)
    private final List<CurrentProjectionChangeListener<T>> listeners = new LinkedList<CurrentProjectionChangeListener<T>>();

    /**
     * Prida listener, ktory bude reagovat na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory bude reagovat na zmeny.
     * @return true, ak listener je pridany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation=true)
    @Listening(monitoringRole = MonitoringRole.PUBLISHER)
    public boolean addCurrentProjectionChangeListener(CurrentProjectionChangeListener<T> listener) {
        return listeners.add(listener);
    }
    
    @CurrentProjectionChange(propagation=true)
    @Listening(monitoringRole = MonitoringRole.PUBLISHER)
    public boolean addCurrentProjectionChangeListener(int position, CurrentProjectionChangeListener<T> listener) {
        listeners.add(position, listener);
        return true;
    }

    /**
     * Odoberie listener, ktory reaguje na zmeny v konfiguracii zamerov (dopytu na zdrojovy kod).
     * @param listener listener, ktory reaguje na zmeny.
     * @return true, ak listener je odobrany, v opacnom pripade false.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation=true)
    @Listening(monitoringRole = MonitoringRole.PUBLISHER)
    public boolean removeCurrentProjectionChangeListener(CurrentProjectionChangeListener<T> listener) {
        return listeners.remove(listener);
    }

    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation=true)
    @Listening(monitoringRole = MonitoringRole.PUBLISHER)
    private void fireCurrentProjectionChange(CurrentProjectionChangedEvent<T> event) {
        if (event == null) {
            return;
        }
        for (CurrentProjectionChangeListener listener : listeners) {
            listener.projectionChanged(event);
        }
    }
        
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @ProjectionComposition
    private Map<String, Object> params = new HashMap<String, Object>();
    
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @SourceCodeSieving
    private final Set<T> currentlySelectedConcerns = new HashSet<T>();

    /**
     * Vrati zvolene zamery ako nemodifikovatelnu mnozinu.
     * @return zvolene zamery ako nemodifikovatelnu mnozinu.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @SourceCodeSieving
    public Set<T> getCurrentlySelectedConcerns() {
        return Collections.unmodifiableSet(currentlySelectedConcerns);
    }

    /**
     * Vrati mod konfiguracie zamerov (dopytu).
     * @return mod konfiguracie zamerov (dopytu).
     */
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @SourceCodeSieving
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Nastavi novu mnozinu zamerov.
     * @param selectedConcerns nova mnozina zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @CurrentProjectionChange
    @Deprecated
    public void setSelectedConcerns(Set<T> selectedConcerns) {
        this.currentlySelectedConcerns.clear();
        this.currentlySelectedConcerns.addAll(selectedConcerns);
        fireCurrentProjectionChange(new CurrentProjectionChangedEvent(this));
    }

    /**
     * Nastavi parametre konfiguracie zamerov (dopytu). Prefer 
     * setSelectedConcerns(Set<T> selectedConcerns, Map<String, Object> params).
     * @param params mod konfiguracie zamerov (dopytu).
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @CurrentProjectionChange
    @Deprecated
    public void setParams(Map<String, Object> params) {
        this.params = params;
        fireCurrentProjectionChange(new CurrentProjectionChangedEvent(this));
    }
    
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    @CurrentProjectionChange
    public void setSelectedConcerns(Set<T> selectedConcerns, Map<String, Object> params) {
        this.currentlySelectedConcerns.clear();
        this.currentlySelectedConcerns.addAll(selectedConcerns);
        this.params = params;
        fireCurrentProjectionChange(new CurrentProjectionChangedEvent(this));
    }
    
    @Disposal
    public void dispose() {
        this.listeners.clear();
        this.currentlySelectedConcerns.clear();
    }

    /**
     * Event pre zmenu v konfiguracii zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation = true)
    public static class CurrentProjectionChangedEvent<T extends Concern> {

        @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
        private final CurrentProjection<T> newCurrentProjection;

        /**
         * Vytvori event pre zmenu v konfiguracii zamerov.
         * @param newProjection nova konfiguracia zamerov.
         */
        @CurrentProjectionChange(propagation = true)
        public CurrentProjectionChangedEvent(CurrentProjection<T> newProjection) {
            this.newCurrentProjection = newProjection;
        }

        /**
         * Vrati novu konfiguraciu zamerov.
         * @return novu konfiguraciu zamerov.
         */
        public CurrentProjection<T> getConfiguration() {
            return newCurrentProjection;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Concern concern : this.newCurrentProjection.getCurrentlySelectedConcerns()) {
                builder.append(concern.toString()).append(";");
            }
            return builder.toString();
        }
    }

    /**
     * Listener reagujuci na zmeny v konfiguracii zamerov.
     */
    //SsceIntent:Notifikacia zmeny dopytu na zdrojovy kod;
    @CurrentProjectionChange(propagation = true)
    @sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection
    public static interface CurrentProjectionChangeListener<T extends Concern> {

        /**
         * Volana ked dojde k zmene v konfiguracii zamerov.
         * @param event event
         */
        public void projectionChanged(CurrentProjection.CurrentProjectionChangedEvent<T> event);
    }
}