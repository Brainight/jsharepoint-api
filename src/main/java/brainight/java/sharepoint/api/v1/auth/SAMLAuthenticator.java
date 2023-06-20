package brainight.java.sharepoint.api.v1.auth;

import brainight.java.sharepoint.api.v1.SPApi;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import org.json.XML;

/**
 * Github: https://github.com/Brainight
 *
 * For more info see:
 * https://learn.microsoft.com/hu-hu/sharepoint/authentication
 * https://sharepoint.stackexchange.com/questions/239128/connect-to-sharepoint-online-rest-api-using-java?rq=1
 *
 * @author Brainight
 */
public class SAMLAuthenticator {

    public static final String SAML_AUTH_TEMPLATE = "<s:Envelope \n"
            + "xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" \n"
            + "xmlns:a=\"http://www.w3.org/2005/08/addressing\">\n"
            + "<s:Header>\n"
            + "    <a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</a:Action>\n"
            + "    <a:ReplyTo>\n"
            + "        <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>\n"
            + "    </a:ReplyTo>\n"
            + "    <a:To s:mustUnderstand=\"1\">https://login.microsoftonline.com/extSTS.srf</a:To>\n"
            + "    <o:Security s:mustUnderstand=\"1\" \n"
            + "        xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
            + "        <o:UsernameToken>\n"
            + "            <o:Username>[[username]]</o:Username>\n"
            + "            <o:Password>[[password]]</o:Password>\n"
            + "        </o:UsernameToken>\n"
            + "    </o:Security>\n"
            + "</s:Header>\n"
            + "<s:Body>\n"
            + "    <t:RequestSecurityToken \n"
            + "        xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">\n"
            + "        <wsp:AppliesTo \n"
            + "            xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n"
            + "            <a:EndpointReference>\n"
            + "                <a:Address>[[tenant]]</a:Address>\n"
            + "            </a:EndpointReference>\n"
            + "        </wsp:AppliesTo>\n"
            + "        <t:KeyType>http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey</t:KeyType>\n"
            + "        <t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType>\n"
            + "        <t:TokenType>urn:oasis:names:tc:SAML:1.0:assertion</t:TokenType>\n"
            + "    </t:RequestSecurityToken>\n"
            + "</s:Body>\n"
            + "</s:Envelope>";

    public SPUserSession login(String user, String passwd, String tenant) throws IOException {

        String securityToken = this.getSecurityToken(user, passwd, tenant);
        SPAuthCookies spAc = this.getSessionAuthCookies(tenant, securityToken);
        SPFormDigest contextInfo = this.getContextInfo(tenant, spAc);
        SPUserSession us = new SPUserSession(spAc, user, tenant, contextInfo);
        return us;
    }

    private String getSAMLContent(String user, String password, String tenant) {
        return SAML_AUTH_TEMPLATE.replace("[[username]]", user)
                .replace("[[password]]", password)
                .replace("[[tenant]]", "https://" + tenant);
    }

    private String getSecurityToken(String user, String password, String tenant) throws IOException {
        String securityToken = null;

        URL url = new URL("https://login.microsoftonline.com/extSTS.srf");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        String saml = getSAMLContent(user, password, tenant);
        osw.write(saml);
        osw.flush();

        String data = SPApi.read(conn.getInputStream());
        osw.close();

        JSONObject json = XML.toJSONObject(data);
        JSONObject bst = json.getJSONObject("S:Envelope")
                .getJSONObject("S:Body")
                .getJSONObject("wst:RequestSecurityTokenResponse")
                .getJSONObject("wst:RequestedSecurityToken")
                .getJSONObject("wsse:BinarySecurityToken");

        securityToken = bst.getString("content");

        return securityToken;

    }

    private SPAuthCookies getSessionAuthCookies(String tenant, String securityToken) throws IOException {
        List<String> cookies = null;

        URL url = new URL("https://" + tenant + "/_forms/default.aspx?wa=wsignin1.0");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        osw.write(securityToken);
        osw.flush();

        cookies = conn.getHeaderFields().get("Set-Cookie");

        if (cookies == null) {
            throw new IOException("Could not sign in into SharePoint");
        }

        String rtFa = null, fedAuth = null, simi = null;
        for (String cookie : cookies) {
            if (cookie.startsWith("rtFa=")) {
                rtFa = cookie.split(";")[0];
            } else if (cookie.startsWith("FedAuth=")) {
                fedAuth = cookie.split(";")[0];
            } else if (cookie.startsWith("SIMI=")) {
                simi = cookie.split(";")[0];
            }
        }
        SPAuthCookies ac = new SPAuthCookies(rtFa, fedAuth);
        ac.setSIMI(simi);
        return ac;
    }

    private SPFormDigest getContextInfo(String tenant, SPAuthCookies cookies) throws IOException {
        URL url = new URL("https://" + tenant + "/_api/contextinfo");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Cookie", cookies.getAsHeaderValue());
        conn.addRequestProperty("Accept", "application/json;odata=verbose");
        conn.getOutputStream().flush();

        String data = SPApi.read(conn.getInputStream());

        JSONObject json = new JSONObject(data);
        JSONObject gcwi = json.getJSONObject("d")
                .getJSONObject("GetContextWebInformation");

        int formDigestTimeout = (Integer) gcwi.get("FormDigestTimeoutSeconds");
        String forDigestValue = (String) gcwi.get("FormDigestValue");

        return new SPFormDigest(forDigestValue, formDigestTimeout);
    }

}
