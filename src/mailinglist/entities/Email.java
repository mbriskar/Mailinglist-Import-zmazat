/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist.entities;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Date;
import javax.naming.Reference;



/**
 *
 * @author matej
 */
public class Email extends BasicDBObject{

    public static final String ID_MONGO_TAG = "_id";
    public static final String ROOT_MONGO_TAG = "root";
    public static final String IN_REPLY_TO_MONGO_TAG = "in-reply-to";
    public static final String REPLIES_MONGO_TAG = "replies";
    public static final String ATTACHMENTS_MONGO_TAG = "attachments";
    public static final String MAILINGLIST_MONGO_TAG = "mailinglist";
    public static final String MESSAGE_ID_MONGO_TAG = "message_id";
    public static final String SUBJECT_MONGO_TAG = "subject";
    public static final String DATE_MONGO_TAG = "date";
    public static final String FROM_MONGO_TAG = "from";
    public static final String MAIN_CONTENT_MONGO_TAG = "mainContent";
    
    
    public String getId() {
        return getString(ID_MONGO_TAG);
        
    }

    public String getRoot() {
        return getString(ROOT_MONGO_TAG);
    }

    public void setRoot(String root) {
        put(ROOT_MONGO_TAG, root);
    }


    public String getInReplyTo() {
        return getString(IN_REPLY_TO_MONGO_TAG);
    }
    
    public void addReply(String replyId) {
        ArrayList<String> list = (ArrayList<String>)get(REPLIES_MONGO_TAG);
        if(list == null) {
            put(REPLIES_MONGO_TAG,new ArrayList());
            list = (ArrayList<String>)get(REPLIES_MONGO_TAG);
        }
        list.add(replyId);
    }

    public void setInReplyTo(String inReplyTo) {
        put(IN_REPLY_TO_MONGO_TAG, inReplyTo);
    }

    public void addAttachment(ContentPart part) {
        ArrayList<ContentPart> list = (ArrayList<ContentPart>)get(ATTACHMENTS_MONGO_TAG);
        if(list == null) {
            put(ATTACHMENTS_MONGO_TAG,new ArrayList());
            list = (ArrayList<ContentPart>)get(ATTACHMENTS_MONGO_TAG);
        }
        list.add(part);
    }

    public void addMailingList(String mailinglist) {
        ArrayList<String>list = (ArrayList<String>)get(MAILINGLIST_MONGO_TAG);
        if(list == null) {
            append(MAILINGLIST_MONGO_TAG,new ArrayList<String>());
            list = (ArrayList<String>)get(MAILINGLIST_MONGO_TAG);
        }
        list.add(mailinglist);
    }

    public String getMessageId() {
        return getString(MESSAGE_ID_MONGO_TAG);
    }

    public void setMessageId(String messageId) {
        put(MESSAGE_ID_MONGO_TAG, messageId);
    }

    public String getSubject() {
        return getString(SUBJECT_MONGO_TAG);
    }

    public void setSubject(String subject) {
         put(SUBJECT_MONGO_TAG, subject);
    }

    public Date getSentDate() {
        return getDate(DATE_MONGO_TAG);
    }

    public void setSentDate(Date sentDate) {
         put(DATE_MONGO_TAG, sentDate);
    }

    public ArrayList<String> getMessageMailingLists() {
       ArrayList<String> list = (ArrayList<String>)get(MAILINGLIST_MONGO_TAG);
       return list;
       
    }

    public String getFrom() {
        return getString(FROM_MONGO_TAG);
    }

    public void setFrom(String from) {
         put(FROM_MONGO_TAG, from);
    }


    public ContentPart getMainContent() {
        return (ContentPart)get(MAIN_CONTENT_MONGO_TAG);
    }

    public void setMainContent(ContentPart mainContent) {
         put(MAIN_CONTENT_MONGO_TAG, mainContent);
    }

    public ArrayList<ContentPart> getAttachments() {
        ArrayList<ContentPart> list = (ArrayList<ContentPart>)get(ATTACHMENTS_MONGO_TAG);
        return list;
    }


}
