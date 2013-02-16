/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author matej
 */
public class MessageSaver {

   
    
     public  void saveMessage(Message m, DBCollection coll) throws MessagingException, IOException {
        MimeMessage mime = (MimeMessage) m;

        BasicDBObject doc = new BasicDBObject().
                append("id", mime.getMessageID()).
                append("sent", m.getSentDate()).
                append("received", m.getReceivedDate()).
                append("subject", m.getSubject()).
                append("content", m.getContent().toString());
        if (m.getReplyTo() != null) {
            ArrayList replyToList = new ArrayList();
            for (Address ad : m.getReplyTo()) {
                replyToList.add(ad.toString());
            }
            doc.append("replyTo", replyToList);
        }
        if (m.getAllRecipients() != null) {
            ArrayList recipientList = new ArrayList();
            for (InternetAddress ad : (InternetAddress[])m.getAllRecipients()) {
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
            ArrayList replyList = new ArrayList();
            replyList.addAll(Arrays.asList(m.getHeader("In-Reply-To")));
            doc.append("in-reply-to", replyList);
        }


        coll.insert(doc);
        if(m.getHeader("In-Reply-To")!= null) {
            addReply(mime, coll);
        }
        writeMessage(m);
        
    }
    public  void writeMessage(Message m) throws MessagingException {
        MimeMessage mime = (MimeMessage) m;

        if (m.getFrom() != null) {
        }
        System.out.println("////////////////////////////////////////");
        System.out.println(" ID: " +  m.getHeader("Message-ID")[0].toString());
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

        for (InternetAddress ad : (InternetAddress[])m.getAllRecipients()) {
            System.out.println("recipient: " + ad.getAddress());
        }


        for (Address ad : m.getReplyTo()) {
            System.out.println("reply to " + ad.toString());
        }
    }

    public  void addReply(MimeMessage m, DBCollection coll) throws MessagingException {
            BasicDBObject whichDocToUpdate = new BasicDBObject();
            whichDocToUpdate.put("id", m.getHeader("In-Reply-To")[0].toString());
            ArrayList recipientList = new ArrayList();
            for (InternetAddress ad : (InternetAddress[])m.getAllRecipients()) {
                recipientList.add(ad.getAddress());
            }
            BasicDBObject recipientQuery=new BasicDBObject("$in",recipientList);

            whichDocToUpdate.put("recipients",recipientQuery);
            
            BasicDBObject docToInsert = new BasicDBObject("id", m.getMessageID());
            BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("replies", docToInsert));
            coll.update(whichDocToUpdate, updateCommand);
           //example : db.test.update({id:"<1877776408.191361046448968.JavaMail.matej@localhost.localdomain>"}, {"$push":{"replies": {"id":"1"}}})


    }
}
