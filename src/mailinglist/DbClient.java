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
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import mailinglist.entities.ContentPart;
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
        coll.setObjectClass(Email.class);
        coll.setInternalClass(Email.MAIN_CONTENT_MONGO_TAG, ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".0" , ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".1" , ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".2" , ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".3" , ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".4" , ContentPart.class);
        coll.setInternalClass(Email.ATTACHMENTS_MONGO_TAG + ".5" , ContentPart.class);
    }

    public boolean saveMessage(Email email) throws MessagingException, IOException {

        if (getId(email.getMessageId(), email.getMessageMailingLists()) != null) {
            return false;
        } 
        coll.insert(email);
        if ( email.getInReplyTo() != null) {

            Email parent =(Email)coll.findOne(new ObjectId(email.getInReplyTo()));
            parent.addReply(email.getId());
            coll.save(parent);
        }
         return true;
        }
       
    public DBCollection getColl() {
        return coll;
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

    public List<Email> getAllEmails() {
        coll.setObjectClass(Email.class);
         
        DBCursor cursor = coll.find();
        List<Email> objects = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                objects.add((Email) cursor.next());
            }
        } finally {
            cursor.close();
        }
        return objects;
    }

    public String getId(String messageId, ArrayList<String> mailinglistOneCommon) {
        BasicDBObject emailObject = new BasicDBObject("message_id", messageId);
        BasicDBObject mailingListQuery = new BasicDBObject("$in", mailinglistOneCommon);
        if(mailinglistOneCommon == null) {
            mailinglistOneCommon= new ArrayList<String>();
        }
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