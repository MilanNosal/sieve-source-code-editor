package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Code analysis for the purposes of the projection creation.
 * @author Milan
 */
public @interface CodeAnalysis {
    public RepresentationOf[] output() default {RepresentationOf.PROJECTION, RepresentationOf.VIEW};
}
