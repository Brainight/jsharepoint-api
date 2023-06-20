/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package brainight.java.sharepoint.api.v1;

import brainight.java.sharepoint.api.v1.auth.SPUserSession;
import brainight.java.sharepoint.api.v1.model.SPFile;
import brainight.java.sharepoint.api.v1.model.SPFolder;
import java.io.IOException;
import java.io.InputStream;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public interface ISpApi {

    SPFolder getSiteFolderTree(SPUserSession spus, String site, String folder) throws IOException;

    SPFolder createFolder(SPUserSession spus, String site, String folder, boolean overwrite) throws IOException;

    SPFile createEmptyFile(SPUserSession spus, String site, String parentFolder, String filename, boolean overwrite) throws IOException;

    SPFile createFile(SPUserSession spus, String site, String parentFolder, String filename, InputStream is, boolean overwrite) throws IOException;

    void deleteFolder(SPUserSession spus, String site, String folder) throws IOException;

    void deleteFolder(SPUserSession spus, String site, SPFolder folder) throws IOException;

    void deleteFile(SPUserSession spus, String site, String filePath) throws IOException;

    void deleteFile(SPUserSession spus, String site, SPFile file) throws IOException;

    InputStream getFileContent(SPUserSession spus, String site, String file) throws IOException;

    InputStream getFileContent(SPUserSession spus, String site, SPFile file) throws IOException;

}
