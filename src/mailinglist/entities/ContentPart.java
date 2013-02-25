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
public class ContentPart {
    private String type;
    private String content;

    public ContentPart(String type, String content) {
        this.type=type;
        this.content=content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
