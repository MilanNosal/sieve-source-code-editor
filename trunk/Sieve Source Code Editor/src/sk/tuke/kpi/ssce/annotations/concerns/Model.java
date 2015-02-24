package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Model of the view or of the possible projections in the project.
 * @author Milan
 */
public @interface Model {
    public RepresentationOf model(); 
}
