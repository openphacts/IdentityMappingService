/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.net.URI;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.bridgedb.uri.loader.transative.TransativeFinder;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;

/**
 *
 * @author Christian
 */
public class SetupWithTestData {
    
    public static void main(String[] args) throws BridgeDBException  {
        LogManager.getLogger(TransativeFinder.class).setLevel(Level.DEBUG);
        ConfigReader.logToConsole();
        File file = new File("test-data/load.xml");
        URI uri = file.toURI();
        String[] loadArgs = new String[1];
        loadArgs[0] = uri.toString();
        RunLoader.main(loadArgs);
    }

 }
