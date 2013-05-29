/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.openphacts.ims.loader.transative;

import java.io.File;
import java.io.IOException;
import org.bridgedb.loader.transative.TransativeCreator;
import org.bridgedb.loader.transative.TransativeFinder;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.utils.BridgeDBException;
import org.bridgedb.utils.StoreType;

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

}
