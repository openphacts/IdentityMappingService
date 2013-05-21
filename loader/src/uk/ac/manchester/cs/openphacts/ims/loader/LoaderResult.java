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

import org.openrdf.model.Resource;

/**
 *
 * @author Christian
 */
public class LoaderResult {
    
    private final int mappingSetID;
    private final Resource linksetId;
    private final Resource linksetContext;

    public LoaderResult(int mappingSetID, Resource linksetId, Resource linksetContext){
        this.mappingSetID = mappingSetID;
        this.linksetId = linksetId;
        this.linksetContext = linksetContext;
    }
    /**
     * @return the mappingSetID
     */
    public int getMappingSetID() {
        return mappingSetID;
    }

    /**
     * @return the linksetId
     */
    public Resource getLinksetId() {
        return linksetId;
    }

    /**
     * @return the linksetContext
     */
    public Resource getLinksetContext() {
        return linksetContext;
    }
}
