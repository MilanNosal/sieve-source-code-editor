/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.kpi.ssce.file;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;

/**
 * Datovy objekt reprezentujuci pomocny subor .sj, teda SieveJava subor.
 * @author Matej Nosal
 */
//SsceIntent:Praca s pomocnym suborom;
public class SieveJavaDataObject extends MultiDataObject {

    /**
     * Vytvori datovy objekt pre pomocny SieveJava subor.
     * @param pf fileObject
     * @param loader multiFileLoader
     * @throws DataObjectExistsException
     * @throws IOException
     */
    public SieveJavaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    /**
     * Vytvori delegata uzla.
     * @return datovy uzol.
     */
    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    /**
     * Vrati lookup pre tento datovy obejkt.
     * @return lookup pre tento datovy obejkt.
     */
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
