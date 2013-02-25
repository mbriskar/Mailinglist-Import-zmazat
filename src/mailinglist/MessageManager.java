/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mailinglist.entities.ContentPart;
import mailinglist.entities.Email;

/**
 *
 * @author matej
 */
public class MessageManager {
    private DbClient dbClient;
    private final ArrayList<String> mailingLists;
    private static String MAILINGLISTS_PROPERTIES_FILE_NAME = "mailinglists.properties";
    
    public MessageManager(DbClient dbClient) throws IOException {
        this.dbClient= dbClient;
        
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
    }
    
    public Email createMessage(MimeMessage message) throws MessagingException, IOException {
        Email email=new Email();
        email.setMessageId(message.getMessageID());
        email.setSentDate(message.getSentDate());
        if (message.getHeader("In-Reply-To") != null) {
            String inReplyTo=dbClient.getId(message.getHeader("In-Reply-To")[0], mailingLists);
            email.setInReplyTo(inReplyTo);
        }
        email.setFrom(message.getFrom()[0].toString());
        List<MessageManager.Pair<String, String>> list = getText(message);
        email.setSubject(message.getSubject());
        email.setMainContent(new ContentPart(list.get(0).left, list.get(0).right));
        for (int i = 1; i <= list.size(); i++) {
            ContentPart cp = new ContentPart(list.get(0).left, list.get(0).right);
            email.addAttachment(cp);
        }
        for (InternetAddress ad : (InternetAddress[]) message.getAllRecipients()) {
            if (mailingLists.contains(ad.getAddress())) {
                email.addMailingList(ad.getAddress());
            }

        }
        //setRoot
        
             
         
        
        if(email.getInReplyTo() != null) {
            String root=dbClient.getRootAttribute(email.getInReplyTo());
            if("true".equals(root)) {
                 email.setRoot(email.getInReplyTo());
             } else{
                 email.setRoot(root);
             }
        } else {
            email.setRoot("true");
        }

        return email;
        
    }
    
    public boolean saveMessage(Email message) throws MessagingException, IOException {
        dbClient.saveMessage(message);

        
        return true;
    }
    
    private List<MessageManager.Pair<String, String>> getText(Part p) throws
            MessagingException, IOException {
        List<MessageManager.Pair<String, String>> list = new ArrayList<MessageManager.Pair<String, String>>();
        if (p.isMimeType("text/*")) {

            String s = (String) p.getContent();
            if (p.isMimeType("text/html")) {
                list.add(new MessageManager.Pair("text/html", s));
                return list;
            } else {
                list.add(new MessageManager.Pair("text/plain", s));
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
                    list.add(new MessageManager.Pair("alternative_text/plain", bp.getContent().toString()));

                } else if (bp.isMimeType("text/html")) {
                    list.add(new MessageManager.Pair("alternative_text/html", bp.getContent().toString()));

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
