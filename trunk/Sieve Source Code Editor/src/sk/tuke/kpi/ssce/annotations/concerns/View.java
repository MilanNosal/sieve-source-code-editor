package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 * Code implementing view, view creation, extraction and presentation.
 * @author Milan
 */
public @interface View {
    public ViewAspect aspect();
}
