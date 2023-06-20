

package brainight.java.sharepoint.api.v1.model;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class SPItem {

    protected final String name;
    protected final String serverRelativeUrl;
    protected final String uniqueId;

    public SPItem(String name, String serverRelativeUrl, String uniqueId) {
        this.name = name;
        this.serverRelativeUrl = serverRelativeUrl;
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getServerRelativeUrl() {
        return serverRelativeUrl;
    }

    public String getUniqueId() {
        return uniqueId;
    }
    
    
    
    
    
}
