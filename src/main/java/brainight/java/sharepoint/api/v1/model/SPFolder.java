package brainight.java.sharepoint.api.v1.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class SPFolder extends SPItem {

    protected List<SPItem> items;

    public SPFolder(String name, String serverRelativeUrl, String uniqueId) {
        super(name, serverRelativeUrl, uniqueId);
        this.items = new LinkedList<>();
    }

    public List<SPItem> getItems() {
        return items;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        getTree(this, sb, 0);
        return sb.toString();

    }

    private void getTree(SPFolder sp, StringBuilder sb, int depth) {
        
        List<SPFolder> folders = sp.getItems().stream().filter(i -> i instanceof SPFolder).map(i -> (SPFolder)i).collect(Collectors.toList());
        List<SPFile> files =  sp.getItems().stream().filter(i -> i instanceof SPFile).map(i -> (SPFile)i).collect(Collectors.toList());
        
        sb.append(" ".repeat(depth * 2) + sp.getServerRelativeUrl() + "\n");
        for(SPFile f : files){
            sb.append(" ".repeat(depth * 2) + "|\n");
            sb.append(" ".repeat(depth * 2) + " - " + f.getName() + "\n");
        }
        
        depth++;
        for(SPFolder f : folders){
            getTree(f, sb, depth);
        }
    }

}
