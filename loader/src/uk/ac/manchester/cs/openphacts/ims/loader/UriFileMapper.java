/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.datadesc.validator.utils.UrlReader;

/**
 *
 * @author Christian
 */
public class UriFileMapper {
  
    private static HashMap<String,String> pathToFile;
  
    private static final String PATH_TO_FILE_PREFIX = "pathToFile";
    private static final String URI_PATTERN = "uriPattern";
    private static final String PATH = "path";
    
    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UriFileMapper.class);
    
    public static synchronized void init() throws BridgeDBException{
        if (pathToFile != null){
            return;
        }
        pathToFile = new HashMap<String,String>();
        Properties properties = ConfigReader.getProperties();  
        HashMap<String,String> uris = new HashMap<String,String>();
        HashMap<String,String> paths = new HashMap<String,String>();
        Set<String> keys = properties.stringPropertyNames();
        for (String key:keys){
            if (key.startsWith(PATH_TO_FILE_PREFIX)){
                String[] parts = key.split("\\.");
                if (parts.length == 3){
                    if (parts[2].equals(URI_PATTERN)){                      
                        if (paths.containsKey(parts[1])){
                            addMapping(properties.getProperty(key), paths.get(parts[1]));
                            paths.remove(parts[1]);
                        } else {
                            uris.put(parts[1], properties.getProperty(key));
                        }
                    } else if (parts[2].equals(PATH)){                      
                        if (uris.containsKey(parts[1])){
                            addMapping(uris.get(parts[1]), properties.getProperty(key));
                            uris.remove(parts[1]);
                        } else {
                            paths.put(parts[1], properties.getProperty(key));
                        }
                    } else {
                        throw new BridgeDBException ("Unexpected " + PATH_TO_FILE_PREFIX +  " property." + key );                    
                    }
               } else {
                    throw new BridgeDBException ("Unexpected " + PATH_TO_FILE_PREFIX +  " property. It should be three dot seperated parts." + key );
               }
            }
        }
        if (!paths.isEmpty()){
            String part1 = paths.keySet().iterator().next();
            throw new BridgeDBException ("Found " + PATH_TO_FILE_PREFIX + "." + part1 + "." + PATH + "propery "
                    + "but no matching "+ PATH_TO_FILE_PREFIX + "." + part1 + "." + URI_PATTERN + "propery");
        }
        if (!uris.isEmpty()){
            String part1 = uris.keySet().iterator().next();
            throw new BridgeDBException ("Found " + PATH_TO_FILE_PREFIX + "." + part1 + "." + URI_PATTERN + "propery "
                    + "but no matching "+ PATH_TO_FILE_PREFIX + "." + part1 + "." + PATH + "propery");
        }
    }

    public static org.openrdf.model.URI getUri(File file) throws BridgeDBException{
        init();
        String path = file.getAbsolutePath();
        System.out.println(path);
        for (String value:pathToFile.values()){
            System.out.println("\t" + value);
            if (path.startsWith(value)){
                String uriPrefix = getkey(value);
                String uri = uriPrefix + path.substring(value.length());
                try {
                    return new URIImpl(uri); 
                } catch (Exception ex) {
                    logger.error("Unable to convert file " + path + " to uri " + uri);
                }
            }
        }
        return new URIImpl(file.toURI().toString());
    }
    
    public static File toFile(String uri) throws BridgeDBException {
        init();
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
        return uriToTempFile(uri);
    }

    public static void addMapping(String uriPattern, String path) throws BridgeDBException {
        init();
        if (pathToFile.containsKey(uriPattern)){
            if (!pathToFile.get(uriPattern).equals(path)){
                throw new BridgeDBException("Illegal attempt to map " + path + " to " + uriPattern 
                        + " another path already mapped to this uri");
            }
        } else {
            pathToFile.put(uriPattern, path);
        }
        System.out.println(pathToFile);
     }

    private static File uriToTempFile(String uri) {
        try {
            UrlReader urlReader = new UrlReader(uri);
            return urlReader.getTempTextFile();
        } catch (VoidValidatorException ex) {
            Reporter.println("Ignoring file creation error " + ex + " and returning null");
            return null;
        }
    }

    private static String getkey(String value) throws BridgeDBException {
         for (String key:pathToFile.keySet()){
             if (pathToFile.get(key).equals(value)){
                 return key;
             }
         }
         throw new BridgeDBException("Unable to find key for " + value);
    }
}
