/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import com.mongodb.DBCollection;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 *
 * @author matej
 */
public class MboxImporter {
    public static final String MBOX_DIRECTORY = "/home/matej/NetBeansProjects";


    
    private DbClient messageSaver;
    private String mboxDirectory;

   
    /**
     * @param args the command line arguments
     */
   
    public static void main(String[] args) throws NoSuchProviderException, MessagingException, IOException {
        DbClient msgSaver = new DbClient();
        MboxImporter mbox = new MboxImporter(MBOX_DIRECTORY,msgSaver);
        mbox.importMbox("sk-linux");
    }
    
    public MboxImporter(String mboxDirectory,DbClient msgSaver) throws UnknownHostException {
        this.mboxDirectory=mboxDirectory;
        messageSaver= msgSaver;
    }

    public  void importMbox(String mboxFile) throws NoSuchProviderException, MessagingException   {
        Properties props = new Properties();
        props.setProperty("mstor.mbox.metadataStrategy", "none");
        props.setProperty("mail.store.protocol", "mstor");
        props.setProperty("mstor.mbox.cacheBuffers", "disabled");
        props.setProperty("mstor.mbox.bufferStrategy", "mapped");
        props.setProperty("mstor.metadata", "disabled");
        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore(new URLName("mstor:" + mboxDirectory));
        store.connect();

        Folder inbox = store.getDefaultFolder().getFolder(mboxFile);
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
         System.out.println("Importing" +messages.length + "messages.");
        
        for (Message m : messages) {
            try {
                messageSaver.saveMessage(m);
            } catch (IOException ex) {
                
            }
        }
        System.out.println("Done.");
    }
    
     public void setMessageSaver(DbClient messageSaver) {
        this.messageSaver = messageSaver;
    }



   
}
