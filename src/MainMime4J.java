
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.james.mime4j.MimeException;

import org.apache.james.mime4j.stream.MimeTokenStream;

/**
 *
 * @author matej
 */
public class MainMime4J {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        method();
        FileInputStream fis = new FileInputStream("/home/matej/dovecot-crlf");
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        //Create message with stream from file  
        //If you want to parse String, you can use:  
        //Message mimeMsg = new Message(new ByteArrayInputStream(mimeSource.getBytes())); 

        while (fis.available() > 0) {
            MimeMessage msg = new MimeMessage(session, new BufferedInputStream(fis));

            System.out.println(msg.getSize());
        }

    }

    public static void method() throws MimeException, IOException {
        MimeTokenStream stream = new MimeTokenStream();
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream("/home/matej/dovecot-crlf"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainMime4J.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (bufferedInputStream.available() > 0) {
            stream.parse(bufferedInputStream);

            stream.next();

            System.out.println(stream.getState());
            System.out.println("---------------------------------------------");
        }


    }
}
