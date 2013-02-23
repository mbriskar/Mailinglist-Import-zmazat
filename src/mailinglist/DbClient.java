/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.bson.types.ObjectId;

/**
 *
 * @author matej
 */
public class DbClient {

    private static String DATABASE_PROPERTIES_FILE_NAME = "database.properties";
    private static String MAILINGLISTS_PROPERTIES_FILE_NAME = "mailinglists.properties";
    List<String> mailingLists;
    DBCollection coll;

    public DbClient() throws UnknownHostException, IOException {
        Properties prop = new Properties();
        prop.load(DbClient.class.getClassLoader().getResourceAsStream((DATABASE_PROPERTIES_FILE_NAME)));
        Integer defaultPort = Integer.valueOf(prop.getProperty("defaultMongoPort"));
        String databaseUrl = prop.getProperty("defaultMongoUrl");
        String defaultDatabaseName = prop.getProperty("defaultDatabaseName");
        String defaultCollectionName = prop.getProperty("defaultCollection");
        connect(databaseUrl, databaseUrl, defaultPort, defaultCollectionName);

    }

    public DbClient(String mongoUrl, String databaseName, int mongoPort, String collectionName) throws UnknownHostException {
        connect(mongoUrl, databaseName, mongoPort, collectionName);
    }

    private void connect(String mongoUrl, String databaseName, int mongoPort, String collectionName) throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(mongoUrl, mongoPort);
        DB db = mongoClient.getDB(databaseName);
        mongoClient.setWriteConcern(WriteConcern.SAFE);
        coll = db.getCollection(collectionName);
    }

    public boolean saveMessage(Message m) throws MessagingException, IOException {
        MimeMessage mime = (MimeMessage) m;
        if (getId(mime.getMessageID(),getMailingListAddresses(mime)) != null) {
            return false;
        }
        BasicDBObject doc = new BasicDBObject().
                append("message_id", mime.getMessageID()).
                append("sent", m.getSentDate()).
                append("received", m.getReceivedDate()).
                append("subject", m.getSubject());

        doc.append("from", m.getFrom()[0].toString());

        //getting the body =maincontent + attachements
        List<Pair<String, String>> list = getText(m);
        BasicDBObject mainContent = new BasicDBObject("type", list.get(0).left).
                append("text", list.get(0).right);

        doc.append("mainContent", mainContent);
        List<BasicDBObject> attachments = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            BasicDBObject attachment = new BasicDBObject();
            attachment.append("type", list.get(i).left);
            attachment.append("text", list.get(i).right);
            attachments.add(attachment);
        }
        if (!attachments.isEmpty()) {
            doc.append("attachments", attachments);
        }


        List<String> messageMailingList = new ArrayList<>();
        if (m.getAllRecipients() != null) {

            if (mailingLists == null) {
                getMailingLists();
            }
            for (InternetAddress ad : (InternetAddress[]) m.getAllRecipients()) {
                if (mailingLists.contains(ad.getAddress())) {
                    messageMailingList.add(ad.getAddress());
                }

            }
            doc.append("mailinglist", messageMailingList);
        }
        String parentId = "";
        if (m.getHeader("In-Reply-To") != null) {
            //which email is this message replying to
            String inReplyToAddress = m.getHeader("In-Reply-To")[0];
            parentId = getId(inReplyToAddress, messageMailingList);
            doc.append("in-reply-to", parentId);
            BasicDBObject parent;
            if(parentId != null) {
               parent= (BasicDBObject) coll.findOne(new BasicDBObject("_id", new ObjectId(parentId)));
            
            
            if ( "true".equals(parent.getString("root"))) {
                doc.append("root", parentId);
            } else {
                doc.append("root", doc.getString("root"));
            } }
        } else {
            doc.append("root", "true");
        }

        WriteResult result = coll.insert(doc);

        if (!result.getLastError().ok()) {
            return false;
        }


        if (parentId != null && m.getHeader("In-Reply-To") != null) {
            BasicDBObject parentObjectParams = new BasicDBObject("_id", new ObjectId(parentId));
            BasicDBObject docToInsert = new BasicDBObject("id", doc.get("_id"));
            BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("replies", docToInsert));
            coll.update(parentObjectParams, updateCommand);
        }

        return true;

    }

    public DBCollection getColl() {
        return coll;
    }

    // in future maybe in separate objects MailingListManager+ MailingList(if more attributes)
    public List<String> getMailingLists() throws IOException {

        mailingLists = new ArrayList<String>();
        Properties prop = new Properties();
        prop.load(DbClient.class.getClassLoader().getResourceAsStream((MAILINGLISTS_PROPERTIES_FILE_NAME)));
        String mailinglist = "";
        int i = 1;
        while (mailinglist != null) {
            mailinglist = prop.getProperty("mailinglist." + i);
            mailingLists.add(mailinglist);
            i++;
        }

        return mailingLists;

    }


    private List<String> getMailingListAddresses(Message m) throws MessagingException, IOException {
        ArrayList<String> list = new ArrayList();
        if(mailingLists == null) {getMailingLists();}
        for (InternetAddress ad : (InternetAddress[]) m.getAllRecipients()) {
            if (mailingLists.contains(ad.getAddress())) {
                list.add(ad.getAddress());
            }
        }
        return list;
    }

    private List getText(Part p) throws
            MessagingException, IOException {
        List<Pair<String, String>> list = new ArrayList();
        if (p.isMimeType("text/*")) {

            String s = (String) p.getContent();
            if (p.isMimeType("text/html")) {
                list.add(new Pair("text/html", s));
                return list;
            } else {
                list.add(new Pair("text/plain", s));
                return list;
            }

        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    list.add(new Pair("text/plain", bp.getContent().toString()));

                } else if (bp.isMimeType("text/html")) {
                    list.add(new Pair("text/html", bp.getContent().toString()));

                } else {
                    list.addAll(getText(bp));
                }
            }
            return list;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {

                list.addAll(getText(mp.getBodyPart(i)));


            }
            return list;
        }

        return list;
    }

    public void dropTable() {
        this.coll.drop();
    }

    public long emailCount() {
        return coll.count();
    }

    public DBObject findMessageWithMessageId(String messageId) {
        BasicDBObject idObj = new BasicDBObject("message_id", messageId);
        return coll.findOne(idObj);

    }

    public List<BasicDBObject> getAllEmails() {
        DBCursor cursor = coll.find();
        List<BasicDBObject> objects = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                objects.add((BasicDBObject)cursor.next());
            }
        } finally {
            cursor.close();
        }
        return objects;
    }

    public String getId(String messageId, List<String> mailinglistOneCommon) {
        BasicDBObject emailObject = new BasicDBObject("message_id", messageId);
        BasicDBObject mailingListQuery = new BasicDBObject("$in", mailinglistOneCommon);
        emailObject.put("mailinglist", mailingListQuery);
        BasicDBObject findOne = (BasicDBObject) coll.findOne(emailObject);
        if (findOne == null) {
            return null;
        }
        return findOne.getString("_id");
    }

    private class Pair<L, R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }
    }
}
//        if (!(m.getContent() instanceof String)) {
//            Multipart multipart = (Multipart) m.getContent();
//            List attachments = new ArrayList();
//            List bodies = new ArrayList();
//
//            for (int x = 0; x < multipart.getCount(); x++) {
//                BodyPart bodyPart = multipart.getBodyPart(x);
//                String disposition = bodyPart.getDisposition();
//
//                if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
//                    BasicDBObject attachment = new BasicDBObject("type",bodyPart.getContentType()).
//                            append("attachment",bodyPart.getContent().toString());
//                    attachments.add(attachment);
//
//                } else {
//                    BasicDBObject body =new BasicDBObject("type",bodyPart.getContentType()).
//                            append("body",bodyPart.getContent().toString());
//                    bodies.add(body);
//                }
//            }
//            if (!bodies.isEmpty()) {
//                doc.append("content", bodies);
//            }
//            if (!attachments.isEmpty()) {
//                doc.append("attachment", attachments);
//            }
//        } else {
//            doc.append("content", m.getContent().toString());
//        }