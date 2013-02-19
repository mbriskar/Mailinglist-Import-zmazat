/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        System.out.println("Parsed message:");
        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message content: " + message.getContent().toString());
        
        MessageSaver messageSaver = new MessageSaver();
        // check if message already not EXIST!
        messageSaver.saveMessage(message);
    }
}
