package sk.tuke.kpi.ssce.nbinterface.file;

import java.awt.Component;
import java.awt.Graphics;
import java.beans.BeanInfo;
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


//@NbBundle.Messages({
//    "LBL_Something_LOADER=Sieved Java files"
//})
//@DataObject.Registration(
//        mimeType = "text/x-sieve-java",
//        iconBase = "sk/tuke/kpi/ssce/nbinterface/file/ssce.png",
//        displayName = "#LBL_Something_LOADER",
//        position = 300
//)
//@ActionReferences({
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
//            position = 100,
//            separatorAfter = 200
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
//            position = 300
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
//            position = 400,
//            separatorAfter = 500
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
//            position = 600
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
//            position = 700,
//            separatorAfter = 800
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//            position = 900,
//            separatorAfter = 1000
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//            position = 1100,
//            separatorAfter = 1200
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//            position = 1300
//    ),
//    @ActionReference(
//            path = "Loaders/text/something/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
//            position = 1400
//    )
//})
/**
 * Datovy objekt reprezentujuci pomocny subor .sj, teda SieveJava subor.
 * @author Matej Nosal, Milan Nosal
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
        super(pf, loader);//, LanguageRegistry.getInstance().getLanguageByMimeType("text/x-sieve-java"));
        
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        CookieSet cookies = getCookieSet();
//        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    /**
     * Vrati lookup pre tento datovy obejkt.
     * @return lookup pre tento datovy obejkt.
     */
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    private boolean modif = false;
    
    @Override
    public boolean isModified() {
        return this.modif;
    }
    
    @Override
    public void setModified(boolean modif) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (isModified() != modif) {
            this.modif = modif; // setModified
            //Savable present = getLookup().lookup(AbstractSavable.class);
            if (modif) {
                new SJDOSavable(this).add();
                super.setModified(modif);
            } else {
                super.setModified(modif);
                new SJDOSavable(this).remove();
            }
        }
    }
    
//    public void setModified(boolean modif) {
//        if (this.modif != modif) {
//            this.modif = modif;
//            Savable present = getLookup().lookup(AbstractSavable.class);
//            if (modif) {
//                syncModified.add (this);
//                if (present == null) {
//                    new DataObject.DOSavable(this).add();
//                }
//            } else {
//                syncModified.remove (this);
//                if (present == null) {
//                    new DataObject.DOSavable(this).remove();
//                } 
//                Unmodify un = getLookup().lookup(Unmodify.class);
//                if (un != null) {
//                    un.unmodify();
//                }
//            }
//            firePropertyChange(DataObject.PROP_MODIFIED,
//                               !modif ? Boolean.TRUE : Boolean.FALSE,
//                               modif ? Boolean.TRUE : Boolean.FALSE);
//        }
//    }
    
    /**
     * Taken from DataObject implementation.
     */
    private static final class SJDOSavable extends AbstractSavable 
    implements Icon {
        final DataObject obj;

        public SJDOSavable(DataObject obj) {
            this.obj = obj;
        }

        @Override
        public String findDisplayName() {
            return obj.getNodeDelegate().getDisplayName();
        }

        @Override
        protected void handleSave() throws IOException {
            System.out.println(">>>>>>>>>>>>>>> hoooray milnako");
            SaveCookie sc = obj.getLookup().lookup(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof SJDOSavable) {
                SJDOSavable dos = (SJDOSavable)other;
                return obj.equals(dos.obj);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        final void remove() {
            unregister();
        }

        final void add() {
            register();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            icon().paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return icon().getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return icon().getIconHeight();
        }
        
        private Icon icon() {
            return ImageUtilities.image2Icon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        }
    }
}
