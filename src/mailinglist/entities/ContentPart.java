/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailinglist.entities;

import com.mongodb.BasicDBObject;

/**
 *
 * @author matej
 */
public class ContentPart extends BasicDBObject{

    public ContentPart() {

    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getContent() {
        return getString("text");
    }

    public void setContent(String content) {
        put("text", content);
    }
    
}
