/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import java.io.IOException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author matej
 */
public class MessageReceiver {

    
    public static void main(String[] args) throws MessagingException, IOException {

        Session s = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(s, System.in);

        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message content: " + message.getContent().toString());
        DbClient messageSaver;
        if(args.length == 4) {
            messageSaver= new DbClient(args[0], args[1], Integer.valueOf(args[2]), args[3]);
        } else {
            messageSaver = new DbClient();
        }
        messageSaver.saveMessage(message);
    }
}
