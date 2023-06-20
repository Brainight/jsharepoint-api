

package brainight.java.sharepoint.api.v1.auth;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class SPAuthCookies {

    public final String RTFA;
    public final String FED_AUTH;
    public String SIMI;

    public SPAuthCookies(String RTFA, String FED_AUTH) {
        this.RTFA = RTFA;
        this.FED_AUTH = FED_AUTH;
    }
    
    public boolean isValid(){
        return this.RTFA != null && this.FED_AUTH != null;
    }
    
    public String getAsHeaderValue(){
        return RTFA + ";" + FED_AUTH + ((this.SIMI != null) ? ";" + this.SIMI : "");
    }
    
    public String getAsHeaderValue(String...additionalCookies){
        String cookies = this.getAsHeaderValue();
        for(String c : additionalCookies){
            cookies += c + ";";
        }
        
        return cookies;
    }

    public String getSIMI() {
        return SIMI;
    }

    public void setSIMI(String SIMI) {
        this.SIMI = SIMI;
    }
    
    
}
