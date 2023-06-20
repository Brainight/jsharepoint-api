

package brainight.java.sharepoint.api.v1.auth;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class SPFormDigest {

    protected String value;
    protected int timeout;

    public SPFormDigest(String value, int timeout) {
        this.value = value;
        this.timeout = timeout;
    }

    public String getValue() {
        return value;
    }

    public int getTimeout() {
        return timeout;
    }
    
    
   
    
}
