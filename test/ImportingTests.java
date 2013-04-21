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
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mailinglist.importing.MboxImporter;
import mailinglist.DbClient;
import mailinglist.MessageManager;
import mailinglist.entities.ContentPart;
import mailinglist.importing.MessageReceiver;
import mailinglist.entities.Email;
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
    private String collectionName = "test";

    public ImportingTests() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws UnknownHostException {
        dbClient = new DbClient(mongoUrl, databaseName, mongoPort, collectionName);
    }

    @After
    public void tearDown() {
        dbClient.dropTable();
    }

    @Test
    public void testMboxNumberOfMessages() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(dbClient);
        mbox.importMbox("test/test-mails");
        assertEquals(dbClient.emailCount(), 62);
        mbox.importMbox("test/test-mails");
        assertEquals(dbClient.emailCount(), 62);
        writeDBItems();

    }

    @Test
    public void testSaveMessage() throws AddressException, MessagingException {
        final String address = "address";
        final String text = "abc";
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setFrom(new InternetAddress(address));
        message.setText(text);
        message.setHeader("Message-ID", text);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("linux@lists.linux.sk"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
        try {
            MessageManager manager= new MessageManager(dbClient);
            manager.saveMessage(manager.createMessage(message));
        }catch(IOException ex) {
            fail();
        }
        assertEquals(1, dbClient.emailCount());
        Email email=(Email) dbClient.findFirstMessageWithMessageId(text);
        ContentPart cp= email.getMainContent();
        assertEquals(text,email.getMainContent().getContent());
        assertEquals(text,email.getMessageId());
        assertEquals(address,email.getFrom());
        

    }

    private void writeDBItems() {
        List<Email> objects =dbClient.getAllEmails();
        for (BasicDBObject email : objects) {
            System.out.println("ID: " + email.getString("_id"));
            System.out.println("ID: " + email.get("_id"));

            System.out.println("MessageID: " + email.getString("message_id"));
            System.out.println("IN REPLY TO: " + email.getString("in-reply-to"));
            System.out.println("REPLIES: " + email.get("replies"));
            System.out.println("ROOT: " + email.get("root"));
            System.out.println("SENT DATE: " + email.get("sent"));
            System.out.println("MAIN TEXT: " + email.get("mainContent"));
            System.out.println("ATTACHMENTS: " + email.get("attachments"));
            System.out.println("");
        }
    }

    @Test
    public void testMboxMessageAttributes() throws UnknownHostException, NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(dbClient);
        mbox.importMbox("test/test-mails");

        DBObject testObj = dbClient.findFirstMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("Martin Kyrc <martin.kyrc@gmail.com>"));
        assertEquals(((BasicBSONList) testObj.get(Email.MAILINGLIST_MONGO_TAG)).get(0), "linux@lists.linux.sk");
        assertEquals((testObj.get(Email.ROOT_MONGO_TAG)), "true");
        assertNull(((BasicBSONList) (testObj.get(Email.ATTACHMENTS_MONGO_TAG))));
        assertEquals("text/plain",((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("type"));
        assertTrue(((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("text").toString().startsWith("ahojte,"));

        testObj = dbClient.findFirstMessageWithMessageId("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<CAJ37LfSeBctpzD3WS7Cbm2G_uD7c-eSkcBYJ=FtVRRqXc4GWnw@mail.gmail.com>"));
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("Juraj Remenec <remenec@gmail.com>"));
        BasicDBObject replyToDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<d7f794ac9a203ebc1d49776968da0d61@localhost>");
        assertTrue((testObj).get(Email.IN_REPLY_TO_MONGO_TAG).equals(replyToDoc.getString("_id")));
        assertEquals(((BasicBSONList) testObj.get(Email.MAILINGLIST_MONGO_TAG)).get(0), "linux@lists.linux.sk");
        assertEquals((testObj.get(Email.ROOT_MONGO_TAG)), replyToDoc.getString("_id"));
        assertNull(((BasicBSONList) (testObj.get(Email.ATTACHMENTS_MONGO_TAG))));
        assertEquals("text/plain",((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("type"));
        assertTrue(((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("text").toString().startsWith("Kedysika"));
        //assertEquals(((BasicBSONList) (testObj).get("replies")).size(), 1); V principe tam su, ale nie je v In-reply-to

        testObj = dbClient.findFirstMessageWithMessageId("<4F2A6865.3030805@lavabit.com>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<4F2A6865.3030805@lavabit.com>"));
        replyToDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<20120127193813.GG25134@athena.platon.sk>");
        BasicDBObject rootDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("rabgulo <rabgulo@lavabit.com>"));
        assertTrue((testObj).get(Email.IN_REPLY_TO_MONGO_TAG).equals(replyToDoc.getString("_id")));
        assertEquals((testObj.get(Email.ROOT_MONGO_TAG)), rootDoc.getString("_id"));
        assertEquals(((BasicBSONList) testObj.get(Email.MAILINGLIST_MONGO_TAG)).get(0), "linux@lists.linux.sk");
        assertNull(((BasicBSONList) (testObj.get(Email.ATTACHMENTS_MONGO_TAG))));
        assertEquals("text/plain",((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("type"));
        assertTrue(((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("text").toString().startsWith("On 27.01.2012 20:38"));

        testObj = dbClient.findFirstMessageWithMessageId("<20120214202407.GI6838@ksp.sk>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<20120214202407.GI6838@ksp.sk>"));
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("Michal Petrucha <michal.petrucha@ksp.sk>"));
        replyToDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
        rootDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertEquals((testObj.get(Email.ROOT_MONGO_TAG)), rootDoc.getString("_id"));
        assertEquals(((BasicBSONList) testObj.get(Email.MAILINGLIST_MONGO_TAG)).get(0), "linux@lists.linux.sk");
        assertTrue((testObj).get(Email.IN_REPLY_TO_MONGO_TAG).equals(replyToDoc.getString("_id")));
        assertEquals(((BasicBSONList) (testObj.get(Email.ATTACHMENTS_MONGO_TAG))).size(), 1);
        assertEquals("text/plain",((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("type"));
        assertTrue(((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("text").toString().startsWith("On Fri, Feb 03, 2012 at "));
        // as we dont save the "sign"

        testObj = dbClient.findFirstMessageWithMessageId("<20120203104407.GA27369@fantomas.sk>");
         
        replyToDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<20120201114442.GX6838@ksp.sk>");
        rootDoc = (BasicDBObject) dbClient.findFirstMessageWithMessageId("<CAJ37LfR9GUeEQ=EQJvvZ4BSoL489F=a2DUwAK1r4Ebb4tw=haA@mail.gmail.com>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<20120203104407.GA27369@fantomas.sk>"));
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("Matus UHLAR - fantomas <uhlar@fantomas.sk>"));
        assertEquals(((BasicBSONList) testObj.get(Email.MAILINGLIST_MONGO_TAG)).get(0), "linux@lists.linux.sk");
        assertEquals((testObj.get(Email.ROOT_MONGO_TAG)), rootDoc.getString("_id"));
        assertTrue((testObj).get(Email.IN_REPLY_TO_MONGO_TAG).equals(replyToDoc.getString("_id")));
        assertEquals(1, ((BasicBSONList) testObj.get("replies")).size());
        assertNull(((BasicBSONList) (testObj.get(Email.ATTACHMENTS_MONGO_TAG))));
        assertEquals("text/plain",((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("type"));
        assertTrue(((BasicDBObject)testObj.get(Email.MAIN_CONTENT_MONGO_TAG)).get("text").toString().startsWith("On 01.02.12 12:44"));

        writeDBItems();

    }

    @Test
    public void testMessageReceiver() throws FileNotFoundException, IOException, MessagingException {


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
        System.setIn(testInput);
        String[] args = {mongoUrl, databaseName, String.valueOf(mongoPort), collectionName};
        MessageReceiver.main(args);
        testInput.close();
        assertEquals(1, dbClient.emailCount());
        DBObject testObj = dbClient.findFirstMessageWithMessageId("<4E7CA9DA.9040904@gmail.com>");
        assertTrue((testObj).get(Email.MESSAGE_ID_MONGO_TAG).equals("<4E7CA9DA.9040904@gmail.com>"));
        assertTrue((testObj).get(Email.FROM_MONGO_TAG).toString().equals("Martin Kyrc <martin.kyrc@gmail.com>"));
        writeDBItems();
    }
}
