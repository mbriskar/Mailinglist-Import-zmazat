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
import java.util.Arrays;
import java.util.List;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author matej
 */
public class MessageSaver {

    private int mongoPort = 27017;
    private String mongoUrl = "localhost";
    private String databaseName = "test";
    DBCollection coll;

    

    public MessageSaver() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(mongoUrl, mongoPort);
        DB db = mongoClient.getDB(databaseName);
        mongoClient.setWriteConcern(WriteConcern.SAFE);
        coll = db.getCollection("test");
    }

    public boolean saveMessage(Message m) throws MessagingException, IOException {
        MimeMessage mime = (MimeMessage) m;
        if (containMessage(mime)) {
            return false;
        }
        BasicDBObject doc = new BasicDBObject().
                append("id", mime.getMessageID()).
                append("sent", m.getSentDate()).
                append("received", m.getReceivedDate()).
                append("subject", m.getSubject());

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

        // getting others parameters
        if (m.getReplyTo() != null) {
            //who do I reply to
            ArrayList replyToList = new ArrayList();
            for (Address ad : m.getReplyTo()) {
                replyToList.add(ad.toString());
            }
            doc.append("replyTo", replyToList);
        }

        if (m.getAllRecipients() != null) {
            ArrayList recipientList = new ArrayList();
            for (InternetAddress ad : (InternetAddress[]) m.getAllRecipients()) {
                recipientList.add(ad.getAddress());
            }
            doc.append("recipients", recipientList);
        }
        if (m.getFrom() != null) {
            ArrayList fromList = new ArrayList();
            for (Address ad : m.getFrom()) {
                fromList.add(ad.toString());
            }
            doc.append("from", fromList);
        }
        if (m.getHeader("In-Reply-To") != null) {
            //which email is this message replying to
            ArrayList replyList = new ArrayList();
            replyList.addAll(Arrays.asList(m.getHeader("In-Reply-To")));
            doc.append("in-reply-to", replyList);
        }

        WriteResult result = coll.insert(doc);
        if (!result.getLastError().ok()) {
            return false;
        }


        if (m.getHeader("In-Reply-To") != null) {

            addReply(mime, coll);
        }
        writeMessage(m);
        return true;



    }

    private boolean containMessage(MimeMessage mime) throws MessagingException {
        BasicDBObject docToFind = new BasicDBObject();
        docToFind.put("id", mime.getMessageID());
        DBObject myDoc = coll.findOne(docToFind);
        if (myDoc != null) {
            return true;
        } else {
            return false;
        }

    }

    public void writeMessage(Message m) throws MessagingException {
        MimeMessage mime = (MimeMessage) m;

        if (m.getFrom() != null) {
        }
        System.out.println("////////////////////////////////////////");
        System.out.println(" ID: " + m.getHeader("Message-ID")[0].toString());
        System.out.println(" Subject: " + mime.getSubject());
        if (m.getHeader("In-Reply-To") != null) {

            System.out.println(" in reply to " + m.getHeader("In-Reply-To")[0].toString());
        }


        for (Address ad : m.getFrom()) {
            System.out.println("From: " + ad.toString());
        }

        System.out.println(" dlzka " + mime.getSize());
        if (mime.getFlags().getUserFlags().length > 0) {
            System.out.println(" flags " + (mime.getFlags()).getUserFlags()[0]);
        }

        if (mime.getFlags().getSystemFlags().length > 0) {
            System.out.println(" systemflags " + (mime.getFlags()).getSystemFlags()[0]);
        }

        for (InternetAddress ad : (InternetAddress[]) m.getAllRecipients()) {
            System.out.println("recipient: " + ad.getAddress());
        }


        for (Address ad : m.getReplyTo()) {
            System.out.println("reply to " + ad.toString());
        }
    }

    //register the email as a "reply" to parent email
    public void addReply(MimeMessage m, DBCollection coll) throws MessagingException {
        BasicDBObject whichDocToUpdate = new BasicDBObject();
        whichDocToUpdate.put("id", m.getHeader("In-Reply-To")[0].toString());
        ArrayList recipientList = new ArrayList();
        for (InternetAddress ad : (InternetAddress[]) m.getAllRecipients()) {
            recipientList.add(ad.getAddress());
        }
        BasicDBObject recipientQuery = new BasicDBObject("$in", recipientList);

        whichDocToUpdate.put("recipients", recipientQuery);

        BasicDBObject docToInsert = new BasicDBObject("id", m.getMessageID());
        BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("replies", docToInsert));
        coll.update(whichDocToUpdate, updateCommand);
        //example : db.test.update({id:"<1877776408.191361046448968.JavaMail.matej@localhost.localdomain>"}, {"$push":{"replies": {"id":"1"}}})


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

    void setCollection(DBCollection coll) {
        this.coll=coll;
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