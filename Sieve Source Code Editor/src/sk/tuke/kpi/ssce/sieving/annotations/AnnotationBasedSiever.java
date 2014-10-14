package sk.tuke.kpi.ssce.sieving.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CompilationInfo;
import sk.tuke.kpi.ssce.annotations.concerns.AnnotationBasedProjections;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationBasedConcern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;

/**
 *
 * @author Milan
 */
@AnnotationBasedProjections
public class AnnotationBasedSiever implements CodeSiever<AnnotationBasedConcern>,
        CurrentProjection.CurrentProjectionChangeListener<AnnotationBasedConcern> {

    private AnnotationValue getAV(Map<? extends ExecutableElement, ? extends AnnotationValue> params, ExecutableElement get) {
        for (ExecutableElement e : params.keySet()) {
            if (e.getSimpleName().toString().equals(get.getSimpleName().toString())) {
                return params.get(e);
            }
        }
        return null;
    }

    public static enum OPERATION {

        EQUALS("equals"),
        EQUALS_IC("equalsIC"),
        STARTS_WITH("startsWith"),
        ENDS_WITH("endsWith"),
        CONTAINS("contains"),
        MATCHES("matches"),
        TRY_GT(">"),
        TRY_GE(">="),
        TRY_LT("<"),
        TRY_LE("<=");

        OPERATION(String representation) {
            this.representation = representation;
        }

        private final String representation;

        public String getRepresentation() {
            return this.representation;
        }

        public static OPERATION getValueOf(String operation) {
            for (OPERATION op : OPERATION.values()) {
                if (op.representation.equals(operation)) {
                    return op;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.representation;
        }
    }

    public static class Parameter {

        private final String path;
        private final OPERATION operation;
        private final Object value;

        public Parameter(String path, OPERATION operation, Object value) {
            this.path = path;
            this.operation = operation;
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public OPERATION getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Parameter{" + "path='" + path + "', operation=" + operation + ", value='" + value + "'}'";
        }
    }

    private final Map<String, List<Parameter>> cache = new HashMap<String, List<Parameter>>();
    private final Map<String, List<ExecutableElement>> cache2 = new HashMap<String, List<ExecutableElement>>();

    @Override
    public boolean sieveCode(
            Stack<Set<AnnotationBasedConcern>> contextOfConcerns,
            CurrentProjection<AnnotationBasedConcern> currentProjection,
            ConcernExtractor<AnnotationBasedConcern> extractor,
            CompilationInfo info) {
        List<AnnotationBasedConcern> codeConcerns = getConcernsForCode(contextOfConcerns);

        boolean match;

        Set<AnnotationBasedConcern> selectedConcerns
                = new HashSet<AnnotationBasedConcern>(currentProjection.getCurrentlySelectedConcerns());
        if (selectedConcerns.isEmpty()) {
            match = false;
        } else {
            if ("AND".equals(currentProjection.getParams().get("mode"))) {
                match = true;
                if (selectedConcerns.contains(extractor.getNilConcern())) { //selectedConcerns.contains(extractor.getNilConcern())
                    if (!codeConcerns.isEmpty()) {
                        match = false;
                    }
                    selectedConcerns.remove(extractor.getNilConcern());
                }
                for (AnnotationBasedConcern selectedConcern : selectedConcerns) {
                    if (!conforms(codeConcerns, currentProjection.getParams(), selectedConcern, info)) {
                        match = false;
                        break;
                    }
                }
            } else {//if (CurrentProjection.MODE_OR.equals(currentProjection.getParams())) {
                match = false;
                if (selectedConcerns.contains(extractor.getNilConcern())) {
                    if (codeConcerns.isEmpty()) {
                        match = true;
                    }
                    // aby nam to potom pri porovnani nerobilo problemy
                    selectedConcerns.remove(extractor.getNilConcern());
                }
                for (AnnotationBasedConcern selectedConcern : selectedConcerns) {
                    if (conforms(codeConcerns, currentProjection.getParams(), selectedConcern, info)) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }

    private boolean conforms(
            List<AnnotationBasedConcern> codeConcerns, Map<String, Object> params,
            AnnotationBasedConcern selectedConcern, CompilationInfo info) {
        boolean match;

        int indexOfTested = codeConcerns.lastIndexOf(selectedConcern);
        if (indexOfTested == -1) {
            return false;
        }

        AnnotationBasedConcern concernTested
                = (AnnotationBasedConcern) codeConcerns.get(indexOfTested);

        String prefix = ((AnnotationBasedConcern) selectedConcern).getUniquePresentation();

        List<Parameter> parameters = cache.get(prefix);
        if (parameters == null || parameters.isEmpty()) {
            return true;
        }

        List<ExecutableElement> path;

        if ("AND".equals(params.get("mode"))) {
            match = true; // otocit ak co i len jedno neplati
            for (Parameter p : parameters) {
                String pathOfparamater = p.path;
                if (!cache2.containsKey(pathOfparamater)) {
                    buildPath(pathOfparamater, info.getElements().getElementValuesWithDefaults(concernTested.getAnnotation()), info, concernTested);
                }
                path = cache2.get(pathOfparamater);
                if (path == null || !parameterConforms(path, p, concernTested, info)) {
                    return false;
                }
            }
        } else {
            match = false; // otocit ak aspon jedno plati
            for (Parameter p : parameters) {
                String pathOfparamater = p.path;
                if (!cache2.containsKey(pathOfparamater)) {
                    buildPath(pathOfparamater, info.getElements().getElementValuesWithDefaults(concernTested.getAnnotation()), info, concernTested);
                }
                path = cache2.get(pathOfparamater);
                if (path != null && parameterConforms(path, p, concernTested, info)) {
                    return true;
                }
            }
        }
        return match;
    }

    private boolean parameterConforms(List<ExecutableElement> path, Parameter parameter, AnnotationBasedConcern concern, CompilationInfo info) {
        if (path.isEmpty()) {
            return true;
        }

        Map<? extends ExecutableElement, ? extends AnnotationValue> params = info.getElements().getElementValuesWithDefaults(concern.getAnnotation());
        AnnotationValue av = null;
        int size = path.size();
        for (int i = 0; i < size; i++) {
            av = getAV(params, path.get(i)); //params.get(path.get(i));
            if (i < size - 1) {
                params = info.getElements().getElementValuesWithDefaults((AnnotationMirror) av);
            }
        }

        if (av == null) {
            System.out.println(">>>>>>>>>>>>> Something really weird just happened.");
            return false;
        }
        try {
            switch (parameter.operation) {
                case EQUALS: {
                    return parameter.value.toString().equals(av.getValue().toString());
                }
                case EQUALS_IC: {
                    return parameter.value.toString().equalsIgnoreCase(av.getValue().toString());
                }
                case CONTAINS: {
                    return av.getValue().toString().contains(parameter.value.toString());
                }
                case STARTS_WITH: {
                    return av.getValue().toString().startsWith(parameter.value.toString());
                }
                case ENDS_WITH: {
                    return av.getValue().toString().endsWith(parameter.value.toString());
                }
                case MATCHES: {
                    return av.getValue().toString().matches(parameter.value.toString());
                }
                case TRY_GT: {
                    return Long.valueOf(av.getValue().toString()) > Long.valueOf(parameter.value.toString());
                }
                case TRY_GE: {
                    return Long.valueOf(av.getValue().toString()) >= Long.valueOf(parameter.value.toString());
                }
                case TRY_LT: {
                    return Long.valueOf(av.getValue().toString()) < Long.valueOf(parameter.value.toString());
                }
                case TRY_LE: {
                    return Long.valueOf(av.getValue().toString()) <= Long.valueOf(parameter.value.toString());
                }
                default:
                    throw new RuntimeException("Encountered unknown enum constant for annotation value comparison.");
            }
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void buildPath(String key, Map<? extends ExecutableElement, ? extends AnnotationValue> params, CompilationInfo info, AnnotationBasedConcern ab) {
        try {
            List<ExecutableElement> retVal = new LinkedList<ExecutableElement>();

            String currentParam;
            if (!key.contains(".")) {
                cache2.put(key, retVal);
                return;
            }
            String param = key.substring(key.indexOf(".") + 1);

            while (!param.equals("")) {

                int nextDot = param.indexOf(".");
                currentParam = param.substring(0, nextDot == -1 ? param.length() : param.indexOf("."));
                ExecutableElement currentElement = null;

                for (ExecutableElement method : params.keySet()) {
                    if (currentParam.equals(method.getSimpleName().toString())) {
                        currentElement = method;
                    }
                }

                if (currentElement == null) {
                    return;
                } else {
                    retVal.add(currentElement);
                    AnnotationValue currentAV = params.get(currentElement);
                    if (currentAV instanceof AnnotationMirror) {
                        params = info.getElements().getElementValuesWithDefaults((AnnotationMirror) currentAV);
                    }

                    if (nextDot > 0) {
                        param = param.substring(nextDot + 1);
                    } else {
                        break;
                    }
                }
            }

            cache2.put(key, retVal);
        } catch (Exception ex) {
            System.out.println(">>>>>> " + ex.toString());
        }
    }

    private List<AnnotationBasedConcern> getConcernsForCode(Stack<Set<AnnotationBasedConcern>> contextOfConcerns) {
        List<AnnotationBasedConcern> concerns = new ArrayList<AnnotationBasedConcern>();
        for (Set<AnnotationBasedConcern> contextOfConcern : contextOfConcerns) {
            concerns.addAll(contextOfConcern);
        }
        return concerns;
    }

    @Override
    public void projectionChanged(CurrentProjection.CurrentProjectionChangedEvent<AnnotationBasedConcern> event) {
        cache2.clear();
        cache.clear();

        Map<String, Object> params = event.getConfiguration().getParams();

        for (AnnotationBasedConcern concern : event.getConfiguration().getCurrentlySelectedConcerns()) {
            //Map<String, Object> concernParams = new HashMap<String, Object>();
            String prefix = concern.getUniquePresentation();
            List<Parameter> parameters = (List<Parameter>) params.get(prefix);
            cache.put(prefix, parameters);
        }
    }

}
