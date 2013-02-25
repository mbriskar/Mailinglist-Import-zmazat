/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.MessagingException;

/**
 *
 * @author matej
 */
public class Email {

    private String id;
    private String from;
    private String messageId;
    private String subject;
    private Date sentDate;
    private List<String> messageMailingLists = new ArrayList<>();
    private ContentPart mainContent;
    private String inReplyTo;
    private String root;
    private List<ContentPart> attachments = new ArrayList<>();

    public String getId() {
        return id;
    }

    
  
    public void setId(String id) {
        this.id = id;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }


    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public Email() throws MessagingException, IOException {
    }

    public void addAttachment(ContentPart part) {
        attachments.add(part);
    }

    public void addMailingList(String mailinglist) {
        messageMailingLists.add(mailinglist);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public List<String> getMessageMailingLists() {
        return messageMailingLists;
    }

    public void setMessageMailingLists(List<String> messageMailingLists) {
        this.messageMailingLists = messageMailingLists;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getMailinglists() {
        return messageMailingLists;
    }

    public void setMailinglists(List<String> mailinglists) {
        this.messageMailingLists = mailinglists;
    }

    public ContentPart getMainContent() {
        return mainContent;
    }

    public void setMainContent(ContentPart mainContent) {
        this.mainContent = mainContent;
    }

    public List<ContentPart> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ContentPart> attachments) {
        this.attachments = attachments;
    }
}
