/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mailinglist.MboxImporter;
import mailinglist.DbClient;
import mailinglist.MessageReceiver;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
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

    private DbClient dbClient;
    private int mongoPort = 27017;
    private String mongoUrl = "localhost";
    private String databaseName = "testdb";
    private String collectionName="test";

    public ImportingTests()   {
    }

    @BeforeClass
    public static void setUpClass()   {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws UnknownHostException {
        dbClient = new DbClient(mongoUrl, databaseName, mongoPort,collectionName);
    }

    @After
    public void tearDown() {
        dbClient.dropTable();
    }

    @Test
    public void testMboxNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException {
        MboxImporter mbox = new MboxImporter(dbClient);
        mbox.importMbox("test/test-mails");
        assertEquals(dbClient.emailCount(), 62);
        mbox.importMbox("test/test-mails");
        assertEquals(dbClient.emailCount(), 62);
        writeDBItems();
        
    }
    
    private void writeDBItems() {
        List<BasicDBObject> objects = dbClient.getAllEmails();
        for (BasicDBObject email : objects) {
            System.out.println("ID: " + email.getString("_id"));
             System.out.println("ID: " + email.get("_id"));
           
            System.out.println("MessageID: " + email.getString("message_id"));
            System.out.println("IN REPLY TO: " + email.getString("in-reply-to"));
            System.out.println("REPLIES: " + email.get("replies"));
            System.out.println("");
        }
    }

    @Test
    public void testMboxMessageAttributes() throws UnknownHostException, NoSuchProviderException, MessagingException {
        MboxImporter mbox = new MboxImporter(dbClient);
        mbox.importMbox("test/test-mails");

        DBObject testObj = dbClient.findMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertTrue((testObj).get("message_id").equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue((testObj).get("from").toString().equals("Martin Kyrc <martin.kyrc@gmail.com>"));


        testObj = dbClient.findMessageWithMessageId("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>");
        assertTrue((testObj).get("message_id").equals("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>"));
        assertTrue((testObj).get("from").toString().equals("Juraj Remenec <remenec@gmail.com>"));
        BasicDBObject replyToDoc =(BasicDBObject)dbClient.findMessageWithMessageId("<d7f794ac9a203ebc1d49776968da0d61@localhost>");
        assertTrue((testObj).get("in-reply-to").equals(replyToDoc.getString("_id")));
        //assertEquals(((BasicBSONList) (testObj).get("replies")).size(), 1); V principe tam su, ale nie je v In-reply-to

        testObj = dbClient.findMessageWithMessageId("<4F2A6865.3030805@lavabit.com>");
        assertTrue((testObj).get("message_id").equals("<4F2A6865.3030805@lavabit.com>"));
        assertTrue((testObj).get("from").toString().equals("rabgulo <rabgulo@lavabit.com>"));

        testObj = dbClient.findMessageWithMessageId("<20120214202407.GI6838@ksp.sk>");
        assertTrue((testObj).get("message_id").equals("<20120214202407.GI6838@ksp.sk>"));
        assertTrue((testObj).get("from").toString().equals("Michal Petrucha <michal.petrucha@ksp.sk>"));
        replyToDoc =(BasicDBObject)dbClient.findMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
        assertTrue((testObj).get("in-reply-to").equals(replyToDoc.getString("_id")));


        testObj = dbClient.findMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
        assertTrue((testObj).get("message_id").equals("<20120203104407.GA27369@fantomas.sk>"));
        assertTrue((testObj).get("from").toString().equals("Matus UHLAR - fantomas <uhlar@fantomas.sk>"));
        assertEquals(1, ((BasicBSONList) testObj.get("replies")).size());
        writeDBItems();

    }

    @Test
    public void testMessageReceiver() throws UnsupportedEncodingException, FileNotFoundException, IOException, MessagingException {

       
        InputStream old = System.in;
        String output;


        FileInputStream stream = new FileInputStream(new File("test/simpleMail"));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            output = Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
         InputStream testInput = new ByteArrayInputStream(output.getBytes("UTF-8"));
         System.setIn( testInput );
         String[] args = {mongoUrl, databaseName, String.valueOf(mongoPort),collectionName};
         MessageReceiver.main(args);
         assertEquals(1, dbClient.emailCount());
         DBObject testObj = dbClient.findMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
         assertTrue((testObj).get("message_id").equals("<4E7CA9DA.9040904@gmail.com>"));
         assertTrue((testObj).get("from").toString().equals("Martin Kyrc <martin.kyrc@gmail.com>"));
         writeDBItems();
    }

    
        
}
