/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader.transative;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bridgedb.loader.LinksetListener;
import org.bridgedb.loader.RdfParser;
import org.bridgedb.loader.transative.TransativeFinder;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class TransativeFinderIMS extends TransativeFinder{
    
    public TransativeFinderIMS(StoreType storeType) throws BridgeDBException{
        super(storeType);      
    }
    
    protected File doTransativeIfPossible(MappingSetInfo left, MappingSetInfo right) throws BridgeDBException, IOException{
        return TransativeCreatorIMS.doTransativeIfPossible(left, right, storeType);
    }

    protected int loadLinkset(String absolutePath, String predicate, String justification, Set<String> viaLabels, 
            HashSet<Integer> chainIds) throws BridgeDBException {
        Loader loader = new Loader(storeType);
        File file = new File(absolutePath);
        try {
            return loader.load(file, viaLabels, chainIds);
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error loading transative file", ex);
        }
    }

}
