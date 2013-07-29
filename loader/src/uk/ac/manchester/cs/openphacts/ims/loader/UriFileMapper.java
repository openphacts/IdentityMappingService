/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian
 */
public class UriFileMapper {
    
    private static HashMap<String,String> pathToFile;
    
    public static void init(){
        if (pathToFile != null){
            return;
        }
        pathToFile = new HashMap<String,String>();
        pathToFile.put("http://openphacts.cs.man.ac.uk/ims/linkset/", "/var/www/html/ims/linkset/");
        pathToFile.put("http://openphacts.cs.man.ac.uk/ims/linkset", "C:/Dropbox/linksets");
    }
    
    public static File toFile(String uri){
        if (uri.startsWith("file:")){
            URI asUri;
            try {
                asUri = new URI(uri);
                 return new File(asUri);
            } catch (URISyntaxException ex) {
                //ok treat as uri
            }
           
        }
        for (String key:pathToFile.keySet()){
            if (uri.startsWith(key)){
                String path = pathToFile.get(key) + uri.substring(key.length());
                File file = new File(path);
                if (file.exists()){
                    return file;
                } 
                System.out.println ("\t missing: " + file.getAbsolutePath());
            }
        }
        System.out.println ("\t no path ");
        return null;
    }
}
