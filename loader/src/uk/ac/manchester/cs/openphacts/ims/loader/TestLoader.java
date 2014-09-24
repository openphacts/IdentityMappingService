/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.File;
import java.net.URI;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.ConfigReader;
import org.bridgedb.utils.Reporter;
import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class TestLoader {
    
    
    public static void main(String[] args) throws BridgeDBException, VoidValidatorException  {
        ConfigReader.useTest();
        RdfReader reader = RdfFactoryIMS.getReader();
        Loader loader = new Loader();
        File voidFile = new File("C:/Dropbox/linksets/Ops1_3_alpha2/Ensembl_71.ttl");
        reader.loadFile(voidFile);
        Reporter.println("Loaded " + voidFile);
        File linkFile = new File("C:/Dropbox/linksets/Ops1_3_alpha2/homo_sapiens_core_71_37_ensembl_UniGeneLinkSets.ttl");
        loader.load(linkFile);
        Reporter.println("Loaded " + linkFile);
    }

 }
