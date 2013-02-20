/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import mailinglist.MboxImporter;
import mailinglist.DbClient;
import mailinglist.MessageReceiver;
import org.bson.types.BasicBSONList;
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

    public ImportingTests() throws UnknownHostException {
    }

    @BeforeClass
    public static void setUpClass() throws UnknownHostException {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws UnknownHostException {
        dbClient = new DbClient(mongoUrl, databaseName, mongoPort);
    }

    @After
    public void tearDown() {
        dbClient.dropTable();
    }

    @Test
    public void testMboxNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException {
        MboxImporter mbox = new MboxImporter("test", dbClient);
        mbox.importMbox("test-mails");
        assertEquals(dbClient.emailCount(), 62);
        mbox.importMbox("test-mails");
        assertEquals(dbClient.emailCount(), 62);
    }

    @Test
    public void testMboxMessageAttributes() throws UnknownHostException, NoSuchProviderException, MessagingException {
        MboxImporter mbox = new MboxImporter("test", dbClient);
        mbox.importMbox("test-mails");

        DBObject testObj = dbClient.findMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertTrue((testObj).get("messageId").equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue((testObj).get("from").toString().equals("Martin Kyrc <martin.kyrc@gmail.com>"));


        testObj = dbClient.findMessageWithMessageId("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>");
        assertTrue((testObj).get("messageId").equals("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>"));
        assertTrue((testObj).get("from").toString().equals("Juraj Remenec <remenec@gmail.com>"));
        assertTrue((testObj).get("in-reply-to").equals("<d7f794ac9a203ebc1d49776968da0d61@localhost>"));
        //assertEquals(((BasicBSONList) (testObj).get("replies")).size(), 1); V principe tam su, ale nie je v In-reply-to

        testObj = dbClient.findMessageWithMessageId("<4F2A6865.3030805@lavabit.com>");
        assertTrue((testObj).get("messageId").equals("<4F2A6865.3030805@lavabit.com>"));
        assertTrue((testObj).get("from").toString().equals("rabgulo <rabgulo@lavabit.com>"));

        testObj = dbClient.findMessageWithMessageId("<20120214202407.GI6838@ksp.sk>");
        assertTrue((testObj).get("messageId").equals("<20120214202407.GI6838@ksp.sk>"));
        assertTrue((testObj).get("from").toString().equals("Michal Petrucha <michal.petrucha@ksp.sk>"));
        assertTrue((testObj).get("in-reply-to").equals("<20120203104407.GA27369@fantomas.sk>"));

        testObj = dbClient.findMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
        assertTrue((testObj).get("messageId").equals("<20120203104407.GA27369@fantomas.sk>"));
        assertTrue((testObj).get("from").toString().equals("Matus UHLAR - fantomas <uhlar@fantomas.sk>"));
        assertEquals(1, ((BasicBSONList) testObj.get("replies")).size());

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
         String[] args = {mongoUrl, databaseName, String.valueOf(mongoPort)};
         MessageReceiver.main(args);
         assertEquals(1, dbClient.emailCount());
         DBObject testObj = dbClient.findMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
         assertTrue((testObj).get("messageId").equals("<4E7CA9DA.9040904@gmail.com>"));
         assertTrue((testObj).get("from").toString().equals("Matej B <matej@email.com>"));
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
