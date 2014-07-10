package sk.tuke.kpi.ssce.core.model.view.importshandling;

import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Trieda modeluje jeden import.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Zobrazenie importov v pomocnom subore;Model pre synchronizaciu kodu;
@Model(model = RepresentationOf.VIEW)
public class Import implements Comparable<Import> {

    private final boolean isStatic;
    private final String packageString;
    private final String classString;
    private boolean editable = true;

    /**
     * Vytvori reprezentaciu importu.
     *
     * @param packageString balickova cast importu.
     * @param classString typova (class) cast importu.
     * @param isStatic urcuje ci je modelovany import staticky.
     */
    public Import(String packageString, String classString, boolean isStatic) {
        this.packageString = packageString;
        this.classString = classString;
        this.isStatic = isStatic;
    }

    /**
     * Vrati typovu (class) cast modelovaneho importu.
     *
     * @return typovu (class) cast modelovaneho importu.
     */
    public String getClassString() {
        return classString;
    }

    /**
     * Vrati balickovu cast modelovaneho importu.
     *
     * @return balickovu cast modelovaneho importu.
     */
    public String getPackageString() {
        return packageString;
    }

    /**
     * Vrati ci je modelovany import staticky alebo ine.
     *
     * @return true, ak je staticky, inak false.
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Vrati ci je modelovany import zmenitelny.
     *
     * @return true, ak je, inak false.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Nastavi ci modelovany import je zmenitelny.
     *
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Import)) {
            return false;
        }
        return this.compareTo((Import) object) == 0;
//            return ((Import) object).getClassString().equals(this.getClassString())
//                    && ((Import) object).getPackageString().equals(this.getPackageString()) && ((Import) object).isStatic() == this.isStatic();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.isStatic ? 1 : 0);
        hash = 83 * hash + (this.packageString != null ? this.packageString.hashCode() : 0);
        hash = 83 * hash + (this.classString != null ? this.classString.hashCode() : 0);
        return hash;
    }

    //SsceIntent:Zobrazenie importov v pomocnom subore;
    @Override
    public String toString() {
        return "import " + this.packageString + "." + this.classString + ";";
    }

    @Override
    public int compareTo(Import o) {
        if (this.packageString == null) {
            return -1;
        }
        int result = this.packageString.compareTo(o.getPackageString());
        if (result != 0) {
            return result;
        } else {
            if (this.classString == null) {
                return -1;
            }
            result = this.classString.compareTo(o.getClassString());

            if (result != 0) {
                return result;
            } else {
                if (this.isStatic == o.isStatic) {
                    return 0;
                } else if (this.isStatic == false) {
                    return -1;
                } else {
                    return +1;
                }

            }
        }
    }
}
