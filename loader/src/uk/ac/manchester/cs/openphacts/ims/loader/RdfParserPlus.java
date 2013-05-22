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
package uk.ac.manchester.cs.openphacts.ims.loader;

import java.io.InputStream;
import org.bridgedb.loader.RdfParser;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.rio.RDFHandler;
import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;
import uk.ac.manchester.cs.openphacts.valdator.utils.UrlReader;

/**
 *
 * @author Christian
 */
public class RdfParserPlus extends RdfParser{
    
     public RdfParserPlus(RDFHandler rdfHandler){
        super(rdfHandler);
    }
    
    @Override
    public InputStream getInputStream(String uri) throws BridgeDBException {
        try {
            UrlReader reader = new UrlReader(uri);
            return reader.getInputStream();
        } catch (VoidValidatorException ex) {
            throw new BridgeDBException("Inable to get inputstream form URI " + uri, ex);
        }
    }
}
