

package brainight.java.sharepoint.api.v1.auth;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class SPUserSession {


    protected SPAuthCookies authCookies;
    protected String username;
    protected String domain;
    protected SPFormDigest contextInfoDigest;

    public SPUserSession(SPAuthCookies authCookies, String username, String domain, SPFormDigest contextInfoDigest) {
        this.authCookies = authCookies;
        this.username = username;
        this.contextInfoDigest = contextInfoDigest;
        this.domain = domain;
    }

    public SPAuthCookies getAuthCookies() {
        return authCookies;
    }

    public void setAuthCookies(SPAuthCookies authCookies) {
        this.authCookies = authCookies;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SPFormDigest getContextInfoDigest() {
        return contextInfoDigest;
    }

    public void setContextInfoDigest(SPFormDigest contextInfoDigest) {
        this.contextInfoDigest = contextInfoDigest;
    }

    public String getDomain() {
        return domain;
    }
    
    
    
    
    
    
}
