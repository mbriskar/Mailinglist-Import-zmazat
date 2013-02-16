/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

/**
 *
 * @author matej
 */
public class Main {
    public static final String MBOX_FOLDER = "/home/matej/NetBeansProjects/";
    public static final String MONGO_ADDRESS = "localhost";
    public static final int MONGO_PORT = 27017;
    public static final String MONGO_DATABASE = "test";

     public static void main(String[] args) throws NoSuchProviderException, MessagingException, IOException {
        MboxImporter mbox = new MboxImporter(MBOX_FOLDER, MONGO_ADDRESS, MONGO_PORT, MONGO_DATABASE);
        mbox.importMbox("sk-linux");
    }
}
