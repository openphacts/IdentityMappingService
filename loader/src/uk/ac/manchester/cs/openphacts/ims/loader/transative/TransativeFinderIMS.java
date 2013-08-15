// OpenPHACTS RDF Validator,
// A tool for validating and storing RDF.
//
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  University of Manchester
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package uk.ac.manchester.cs.openphacts.ims.loader.transative;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.bridgedb.statistics.MappingSetInfo;
import org.bridgedb.uri.loader.transative.TransativeFinder;
import org.bridgedb.utils.BridgeDBException;
import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;

/**
 *
 * @author Christian
 */
public class TransativeFinderIMS extends TransativeFinder{
    
    public TransativeFinderIMS() throws BridgeDBException{
        super();      
    }
    
    @Override
    protected File doTransativeIfPossible(MappingSetInfo left, MappingSetInfo right) throws BridgeDBException, IOException{
        return TransativeCreatorIMS.doTransativeIfPossible(left, right);
    }

    @Override
   protected int loadLinkset(String absolutePath, String sourceDataType, String predicate, String justification, 
            String targetDataType, Set<String> viaLabels, 
            HashSet<Integer> chainIds) throws BridgeDBException {
         Loader loader = new Loader();
        File file = new File(absolutePath);
        try {
            return loader.load(file, sourceDataType, targetDataType, viaLabels, chainIds);
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException ("Error loading transative file", ex);
        }
    }

}
