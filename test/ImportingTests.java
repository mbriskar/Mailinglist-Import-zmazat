/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.net.UnknownHostException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import mailinglist.MboxImporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author matej
 */
public class ImportingTests {
    private static DBCollection coll;
    private int mongoPort = 27017;
    private String mongoUrl = "localhost";
    private String databaseName = "test";

    
    public ImportingTests() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(mongoUrl, mongoPort);
        DB db = mongoClient.getDB(databaseName);
        mongoClient.setWriteConcern(WriteConcern.SAFE);
        coll = db.getCollection("testdb");

    }
    
    @BeforeClass
    public static void setUpClass() throws UnknownHostException {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
     public void testMboxImport() throws UnknownHostException, NoSuchProviderException, MessagingException {
        MboxImporter mbox = new MboxImporter("test");
        mbox.setCollection(coll);
        mbox.importMbox("3mails");
        assertEquals(coll.find().count(), 3);
        mbox.importMbox("3mails");
        assertEquals(coll.find().count(), 3);
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
