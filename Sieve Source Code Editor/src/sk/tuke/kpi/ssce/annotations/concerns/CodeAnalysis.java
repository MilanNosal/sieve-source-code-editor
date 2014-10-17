package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 *
 * @author Milan
 */
public @interface CodeAnalysis {
    public RepresentationOf[] output() default {RepresentationOf.PROJECTION, RepresentationOf.VIEW};
}