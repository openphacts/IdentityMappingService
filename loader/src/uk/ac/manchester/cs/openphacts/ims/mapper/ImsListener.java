// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
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
package uk.ac.manchester.cs.openphacts.ims.mapper;

import org.bridgedb.uri.tools.RegexUriPattern;
import org.bridgedb.uri.tools.UriListener;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.Resource;

/**
 *
 * @author christian
 */
public interface ImsListener extends UriListener {
    
    public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String justification,
        RegexUriPattern targetUriPattern, Resource mappingResource, Resource mappingSource, boolean symetric) 
            throws BridgeDBException;

     public int registerMappingSet(RegexUriPattern sourceUriPattern, String predicate, String forwardJustification, 
            String backwardJustification,
            RegexUriPattern targetUriPattern, Resource mappingResource, Resource mappingSource) throws BridgeDBException;
}
