package sk.tuke.kpi.ssce.core.model.view.importshandling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Trieda modeluje importy v java subore.
 * @author Matej Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
@Model(model = RepresentationOf.VIEW)
@Synchronization
public class Imports {

    //SsceIntent:Zobrazenie importov v pomocnom subore;
    private boolean insertNewLineBeforeAfterImports = false;
    private final Set<Import> imports = new TreeSet<Import>();

    /**
     * Zisti ci je nutne vlozit novy riadok pred/za importy pri exporte do jedneho retazca.
     * @return true, ak je potrebne vlozit novy riadok, v opacnom pripade false.
     */
    //SsceIntent:Zobrazenie importov v pomocnom subore;
    public boolean isInsertNewLineBeforeAfterImports() {
        return insertNewLineBeforeAfterImports;
    }

    /**
     * Nastavi ci je nutne vlozit novy riadok pred/za importy pri exporte do jedneho retazca.
     * @param insertNewLineBeforeAfterImports 
     */
    //SsceIntent:Zobrazenie importov v pomocnom subore;
    public void setInsertNewLineBeforeAfterImports(boolean insertNewLineBeforeAfterImports) {
        this.insertNewLineBeforeAfterImports = insertNewLineBeforeAfterImports;
    }

    /**
     * Prida do modelu importov dalsi import.
     * @param importString retazec reprezentujuci vkladany import.
     * @param isStatic urcuje ci je vkladany import staticky alebo nie.
     */
    public void addImport(String importString, boolean isStatic) {
        int lastDot = importString.lastIndexOf(".");
        addImport(importString.substring(0, lastDot), importString.substring(lastDot + 1), isStatic);
//        imports.add(new Import(importString.substring(0, lastDot), importString.substring(lastDot + 1,), isStatic));
    }

    /**
     * Prida do modelu importov dalsi import.
     * @param packageString balickova cast importu bez bodky na konci.
     * @param classString typova cast importu (nazov triedy).
     * @param isStatic urcuje ci je vkladany import staticky alebo nie.
     */
    public void addImport(String packageString, String classString, boolean isStatic) {
        imports.add(new Import(packageString, classString, isStatic));
    }

    /**
     * Prida do modelu importov dalsi import.
     * @param import1 vkladany import.
     */
    public void addImport(Import import1) {
        imports.add(import1);
    }

    /**
     * Nastavi, ze vsetky importy mozu byt zmenene alebo naopak nezmenitelne.
     * @param editable
     */
    public void setEditableAllImports(boolean editable) {
        for (Import i : imports) {
            i.setEditable(editable);
        }
    }

    /**
     * Vrati pocet modelovanych importov.
     * @return pocet modelovanych importov.
     */
    public int getCount() {
        return imports.size();
    }

    /**
     * Vyhlada import podla typovej casti (class casti) importu.
     * @param classString typova cast (class cast) importu
     * @return haldany import.
     */
    public Import findImport(String classString) {
        Iterator<Import> iterator = imports.iterator();
        for (Import import1; iterator.hasNext();) {
            import1 = iterator.next();
            if (import1.getClassString().equals(classString)) {
                return import1;
            }
        }
        return null;
    }

    /**
     * Odstradni import z modelu importov.
     * @param import1 import, ktory ma byt odostraneny.
     * @return true, ak je import odstrany, v opacnom pripade false.
     */
    public boolean removeImport(Import import1) {
        return imports.remove(import1);
    }

    /**
     * Odstradni importy z modelu importov.
     * @param imports mnozina modelovanych importov, ktore maju byt odstranene z tohto modelu importov.
     * @return true, ak je odstranenie importov zrealizovane, v opacnom pripade false.
     */
    public boolean removeImports(Imports imports) {
        return this.imports.removeAll(imports.imports);
    }

    /**
     * Prida do modelu importov dalsie importy.
     * @param imports importy, ktore maju byt pridane do tohto modelu importov.
     * @return true, ak pridanie prebehne, v opacnom pripade false.
     */
    public boolean addImports(Imports imports) {
        return this.imports.addAll(imports.imports);
    }

    /**
     * Odstrani vsetky zmenitelne importy z tohto moduelu importov.
     */
    public void removeAllEditableImports() {
        for(Import i:imports.toArray(new Import[]{})){
            if (i.isEditable()) {
                this.imports.remove(i);
            }
        }
    }

    /**
     * Vrati vsetky typove identifikatory z tohto modelu importov.
     * @return vsetky typove identifikatory z tohto modelu importov.
     */
    public Set<String> getAllTypeIdentifiers() {
        Set<String> ids = new HashSet<String>();
        for (Import i : imports) {
            ids.add(i.getClassString());
        }
        return ids;
    }

    //SsceIntent:Zobrazenie importov v pomocnom subore;
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("");
        if (insertNewLineBeforeAfterImports && !imports.isEmpty()) {
            builder.append("\n");
        }

        Iterator<Import> iterator = imports.iterator();
        if (iterator.hasNext()) {
            builder.append(iterator.next().toString());
        }
        while (iterator.hasNext()) {
            builder.append("\n").append(iterator.next().toString());
        }
        if (insertNewLineBeforeAfterImports && !imports.isEmpty()) {
            builder.append("\n");
        }
        return builder.toString();
    }    
}