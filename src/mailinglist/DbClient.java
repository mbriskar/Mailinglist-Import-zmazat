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
import javax.mail.MessagingException;
import mailinglist.entities.Email;
import org.bson.types.ObjectId;

/**
 *
 * @author matej
 */
public class DbClient {

    private static String DATABASE_PROPERTIES_FILE_NAME = "database.properties";
    
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

    public boolean saveMessage(Email email) throws MessagingException, IOException {

        if (getId(email.getMessageId(), email.getMailinglists()) != null) {
            return false;
        }
        BasicDBObject doc = new BasicDBObject().
                append("message_id", email.getMessageId()).
                append("sent", email.getSentDate()).
                append("subject", email.getSubject()).
                append("from", email.getFrom());

        BasicDBObject mainContent = new BasicDBObject("type", email.getMainContent().getType()).
                append("text", email.getMainContent().getContent());
        //getting the body =maincontent + attachements
        doc.append("mainContent", mainContent);
        List<BasicDBObject> attachments = new ArrayList<>();
        for (int i = 1; i < email.getAttachments().size(); i++) {
            BasicDBObject attachment = new BasicDBObject();
            attachment.append("type", email.getAttachments().get(i).getType());
            attachment.append("text", email.getAttachments().get(i).getContent());
            attachments.add(attachment);
        }
        if (!attachments.isEmpty()) {
            doc.append("attachments", attachments);
        }

        doc.append("mailinglist", email.getMailinglists());
        doc.append("in-reply-to", email.getInReplyTo());
        doc.append("root", email.getRoot());

        WriteResult result = coll.insert(doc);
        if (!result.getLastError().ok()) {
            return false;
        }
        
        email.setId(doc.getString("_id"));

        //set REPLY
        if ( email.getInReplyTo() != null) {
            addReply(email.getInReplyTo(),email.getId());
        }

        return true;

    }

    public DBCollection getColl() {
        return coll;
    }
    
    
    public void addReply(String parentId,String replyId) {
        BasicDBObject parentObjectParams = new BasicDBObject("_id", new ObjectId(parentId));
        BasicDBObject docToInsert = new BasicDBObject("id", replyId);
        BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("replies", docToInsert));
        coll.update(parentObjectParams, updateCommand);
    }
    
    public BasicDBObject getMessage(String mongoId) {
        return (BasicDBObject) coll.findOne(new BasicDBObject("_id",new ObjectId(mongoId)));
    }


  

    public void dropTable() {
        this.coll.drop();
    }

    public long emailCount() {
        return coll.count();
    }

    public DBObject findFirstMessageWithMessageId(String messageId) {
        BasicDBObject idObj = new BasicDBObject("message_id", messageId);
        return coll.findOne(idObj);

    }

    public List<BasicDBObject> getAllEmails() {
        DBCursor cursor = coll.find();
        List<BasicDBObject> objects = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                objects.add((BasicDBObject) cursor.next());
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

    public String getRootAttribute(String id) {
        BasicDBObject emailObject = new BasicDBObject("_id", new ObjectId(id));
        BasicDBObject findOne = (BasicDBObject) coll.findOne(emailObject);
        if (findOne == null) {
            return null;
        }
        return findOne.getString("root");
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