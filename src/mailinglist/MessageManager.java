/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
        List<ContentPart> list = getContentParts(message);
        email.setSubject(message.getSubject());
        email.setMainContent(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            email.addAttachment(list.get(i));
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
    
    private List<ContentPart> getContentParts(Part p) throws
            MessagingException, IOException {
        List<ContentPart> list = new ArrayList<ContentPart>();
        if (p.isMimeType("text/*")) {

            String s = (String) p.getContent();
            if (p.isMimeType("text/html")) {
                ContentPart cp= new ContentPart("text/html", s);
                list.add(cp);
                return list;
            } else {
                ContentPart cp= new ContentPart("text/plain", s);
                list.add(cp);
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
                     ContentPart cp= new ContentPart("alternative_text/plain", bp.getContent().toString());
                     list.add(cp);
                    

                } else if (bp.isMimeType("text/html")) {
                    ContentPart cp= new ContentPart("alternative_text/html", bp.getContent().toString());
                    list.add(cp);

                } else {
                    list.addAll(getContentParts(bp));
                }
            }
            return list;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {

                list.addAll(getContentParts(mp.getBodyPart(i)));


            }
            return list;
        }

        return list;
    }

    
}
