/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress; // tie adresy su internetadress
import javax.mail.internet.MimeMessage;

/**
 *
 * @author matej
 */
public class MboxImporter {

    private String mboxPath ;
    private  int mongoPort = 27017;
    private  String mongoUrl = "localhost";
    private  String databaseName = "test";
    private MessageSaver messageSaver;

    /**
     * @param args the command line arguments
     */
   
    
    public MboxImporter(String mboxPath,String mongoUrl,int mongoPort, String databaseName) {
        this.mboxPath=mboxPath;
        this.mongoUrl=mongoUrl;
        this.mongoPort=mongoPort;
        this.databaseName=databaseName;
        messageSaver= new MessageSaver();
    }

    public  void importMbox(String mboxFile) throws NoSuchProviderException, MessagingException, UnknownHostException, IOException {
        Properties props = new Properties();
        props.setProperty("mstor.mbox.metadataStrategy", "none");
        props.setProperty("mail.store.protocol", "mstor");
        props.setProperty("mstor.mbox.cacheBuffers", "disabled");
        props.setProperty("mstor.mbox.bufferStrategy", "mapped");
        props.setProperty("mstor.metadata", "disabled");
        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore(new URLName("mstor:" + mboxPath));
        store.connect();

        Folder inbox = store.getDefaultFolder().getFolder(mboxFile);
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        MongoClient mongoClient = new MongoClient(mongoUrl, mongoPort);
        DB db = mongoClient.getDB(databaseName);
        mongoClient.setWriteConcern(WriteConcern.SAFE);
        DBCollection coll = db.getCollection("test");
        for (Message m : messages) {
            
            messageSaver.saveMessage(m, coll);
        }
    }

   
}
