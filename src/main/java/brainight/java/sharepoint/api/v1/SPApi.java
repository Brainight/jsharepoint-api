package brainight.java.sharepoint.api.v1;

import brainight.java.sharepoint.api.v1.auth.SAMLAuthenticator;
import brainight.java.sharepoint.api.v1.auth.SPUserSession;
import brainight.java.sharepoint.api.v1.model.SPFile;
import brainight.java.sharepoint.api.v1.model.SPFolder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class SPApi implements ISpApi {

    public static SPApi instance = new SPApi();

    public static void main(String... args) throws IOException {

    }

    @Override
    public SPFolder getSiteFolderTree(SPUserSession spus, String site, String folder) throws IOException {
        SPFolder spFolder = getFolder(spus, site, "/sites/" + site + "/" + folder);
        getContentsRecursive(spFolder, spus, site);
        return spFolder;
    }

    @Override
    public SPFolder createFolder(SPUserSession spus, String site, String folder, boolean overwrite) throws IOException {
        String encodedRelativeURL = String.format("/sites/%s/%s", site, folder.replace(" ", "%20"));
        String sufix = String.format("AddUsingPath(DecodedUrl=@a1,overwrite=@a2)?@a1='%s'&@a2=%s&$Expand=ListItemAllFields/PermMask", encodedRelativeURL, overwrite);
        URL url = new URL(String.format("https://%s/sites/%s/_api/web/folders/%s", spus.getDomain(), site, sufix));
        HttpsURLConnection conn = this.getAuthConnection(url, spus);
        conn.addRequestProperty("Content-Type", "application/json");
        conn.addRequestProperty("Authorization", "Bearer");
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.getOutputStream().flush();

        String data = read(conn.getInputStream());
        JSONObject json = new JSONObject(data).getJSONObject("d");
        String rUrl = (String) json.get("ServerRelativeUrl");
        String uniqId = (String) json.get("UniqueId");
        String name = (String) json.get("Name");
        SPFolder spf = new SPFolder(name, rUrl, uniqId);
        return spf;
    }

    @Override
    public SPFile createEmptyFile(SPUserSession spus, String site, String parentFolder, String filename, boolean overwrite) throws IOException {
        return this.createFile(spus, site, parentFolder, filename, null, overwrite);
    }

    @Override
    public SPFile createFile(SPUserSession spus, String site, String parentFolder, String filename, InputStream is, boolean overwrite) throws IOException {
        String encodedRelativeURL = String.format("/sites/%s/%s", site, parentFolder.replace(" ", "%20"));
        String sUrl = String.format("https://%s/sites/%s/_api/web/GetFolderByServerRelativeUrl('%s')/Files/add(url='%s',overwrite=%s)",
                spus.getDomain(), site, encodedRelativeURL, filename, overwrite);
        URL url = new URL(sUrl);

        HttpsURLConnection conn = this.getAuthConnection(url, spus);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Authorization", "Bearer");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        if (is != null) {

            byte[] chunk = new byte[1024];
            for (int l; (l = is.read(chunk, 0, chunk.length)) > 0;) {
                dos.write(chunk, 0, l);
            }
        }
        dos.flush();

        String data = read(conn.getInputStream());

        JSONObject json = new JSONObject(data);
        json = json.getJSONObject("d");
        String name = (String) json.get("Name");
        String rUrl = (String) json.get("ServerRelativeUrl");
        String uniqId = (String) json.get("UniqueId");
        return new SPFile(name, rUrl, uniqId);
    }

    @Override
    public InputStream getFileContent(SPUserSession spus, String site, String file) throws IOException {
        String encodedRelativeURL = String.format("/sites/%s/%s", site, file);
        return this._getFileContent(spus, site, encodedRelativeURL);
    }

    @Override
    public InputStream getFileContent(SPUserSession spus, String site, SPFile file) throws IOException {
        return this._getFileContent(spus, site, file.getServerRelativeUrl());
    }

    private InputStream _getFileContent(SPUserSession spus, String site, String relativeUrl) throws IOException {
        String encodedRelUrl = relativeUrl.replace(" ", "%20");
        String sUrl = String.format("https://%s/sites/%s/_api/web/GetFileByServerRelativePath(decodedurl='%s')/$value",
                spus.getDomain(), site, encodedRelUrl);
        URL url = new URL(sUrl);

        HttpURLConnection conn = this.getAuthConnection(url, spus);
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Authorization", "Bearer");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.getOutputStream().write(new byte[0]);

        DataInputStream dis = new DataInputStream(conn.getInputStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        for (int l; (l = dis.read(chunk, 0, chunk.length)) > 0;) {
            baos.write(chunk, 0, l);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public void deleteFile(SPUserSession spus, String site, String filePath) throws IOException {
        String relativeURL = String.format("/sites/%s/%s", site, filePath.replace(" ", "%20"));
        URL url = new URL(String.format("https://%s/sites/%s/_api/web/GetFileByServerRelativeUrl('%s')", spus.getDomain(), site, relativeURL));
        HttpsURLConnection conn = this.getAuthConnection(url, spus);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/json");
        conn.addRequestProperty("Authorization", "Bearer");
        conn.addRequestProperty("If-Match", "{etag or *}");
        conn.addRequestProperty("X-Http-Method", "DELETE");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.getOutputStream().flush();
    }

    @Override
    public void deleteFile(SPUserSession spus, String site, SPFile file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deleteFolder(SPUserSession spus, String site, SPFolder folder) throws IOException {
        this._deleteFolder(spus, site, folder.getServerRelativeUrl());
    }

    @Override
    public void deleteFolder(SPUserSession spus, String site, String folderName) throws IOException {
        String relativeURL = String.format("/sites/%s/%s", site, folderName);
        this._deleteFolder(spus, site, relativeURL);
    }

    private void _deleteFolder(SPUserSession spus, String site, String relativeUrl) throws IOException {
        String encodedRelUrl = relativeUrl.replace(" ", "%20");
        URL url = new URL(String.format("https://%s/sites/%s/_api/web/GetFolderByServerRelativeUrl('%s')", spus.getDomain(), site, encodedRelUrl));
        HttpsURLConnection conn = this.getAuthConnection(url, spus);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/json");
        conn.addRequestProperty("Authorization", "Bearer");
        conn.addRequestProperty("If-Match", "*");
        conn.addRequestProperty("X-Http-Method", "DELETE");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.getOutputStream().flush();
    }

    private void getContentsRecursive(SPFolder parent, SPUserSession spus, String site) throws IOException {
        List<SPFolder> f = getFolders(spus, site, parent.getServerRelativeUrl());
        List<SPFile> f1 = getFiles(spus, site, parent.getServerRelativeUrl());
        parent.getItems().addAll(f);
        parent.getItems().addAll(f1);
        if (f.size() > 0) {
            for (SPFolder item : f) {
                getContentsRecursive(item, spus, site);
            }
        }
    }

    public SPFolder getFolder(SPUserSession spus, String site, String relativeURL) throws IOException {
        String encodedRelativeURL = relativeURL.replace(" ", "%20");
        String sUrl = String.format("https://%s/sites/%s/_api/web/GetFolderByServerRelativePath(decodedurl='%s')/", spus.getDomain(), site, encodedRelativeURL);
        String data = get(spus, sUrl);
        JSONObject json = new JSONObject(data).getJSONObject("d");
        String rUrl = (String) json.get("ServerRelativeUrl");
        String uniqId = (String) json.get("UniqueId");
        String name = (String) json.get("Name");
        SPFolder spf = new SPFolder(name, rUrl, uniqId);
        return spf;
    }

    public List<SPFolder> getFolders(SPUserSession spus, String site, String relativeURL) throws IOException {
        String encodedRelativeURL = relativeURL.replace(" ", "%20");
        String sUrl = String.format("https://%s/sites/%s/_api/web/GetFolderByServerRelativePath(decodedurl='%s')/Folders", spus.getDomain(), site, encodedRelativeURL);
        String data = get(spus, sUrl);
        JSONObject json = new JSONObject(data);
        JSONArray results = json.getJSONObject("d").getJSONArray("results");
        List<SPFolder> list = new LinkedList<>();
        results.forEach((r) -> {
            JSONObject o = (JSONObject) r;
            String rUrl = (String) o.get("ServerRelativeUrl");
            String uniqId = (String) o.get("UniqueId");
            String name = (String) o.get("Name");

            SPFolder spf = new SPFolder(name, rUrl, uniqId);
            list.add(spf);
        });

        return list;
    }

    public List<SPFile> getFiles(SPUserSession spus, String site, String relativeURL) throws IOException {
        String encodedRelativeURL = relativeURL.replace(" ", "%20");
        String sUrl = String.format("https://%s/sites/%s/_api/web/GetFolderByServerRelativePath(decodedurl='%s')/Files", spus.getDomain(), site, encodedRelativeURL);
        String data = get(spus, sUrl);
        JSONObject json = new JSONObject(data);
        JSONArray results = json.getJSONObject("d").getJSONArray("results");
        List<SPFile> list = new LinkedList<>();
        results.forEach((r) -> {
            JSONObject o = (JSONObject) r;
            String rUrl = (String) o.get("ServerRelativeUrl");
            String uniqId = (String) o.get("UniqueId");
            String name = (String) o.get("Name");

            SPFile spf = new SPFile(name, rUrl, uniqId);
            list.add(spf);
        });

        return list;
    }

    public String get(SPUserSession spus, String url) throws IOException {
        HttpsURLConnection c = this.getAuthConnection(new URL(url), spus);
        c.setRequestMethod("GET");
        String data = read(c.getInputStream());
        return data;
    }

    public String post(SPUserSession spus, String url) throws IOException {
        HttpsURLConnection c = this.getAuthConnection(new URL(url), spus);
        c.setRequestMethod("GET");
        String data = read(c.getInputStream());
        return data;
    }

    protected HttpsURLConnection getAuthConnection(URL url, SPUserSession spus) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("Cookie", spus.getAuthCookies().getAsHeaderValue());
        conn.addRequestProperty("X-RequestDigest", spus.getContextInfoDigest().getValue());
        conn.addRequestProperty("Accept", "application/json;odata=verbose");
        return conn;
    }
    
    protected HttpURLConnection __getAuthConnection(URL url, SPUserSession spus) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("Cookie", spus.getAuthCookies().getAsHeaderValue());
        conn.addRequestProperty("X-RequestDigest", spus.getContextInfoDigest().getValue());
        conn.addRequestProperty("Accept", "application/json;odata=verbose");
        return conn;
    }

    public static String read(InputStream is) throws IOException {
        Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];

        for (int l; (l = in.read(c, 0, c.length)) > 0;) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }

}
