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

import java.sql.SQLException;
import java.sql.Statement;
import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.utils.BridgeDBException;

/**
 *
 * @author christian
 */
public class ImsMapper extends SQLUriMapper{
    
    private static ImsMapper mapper = null;

    //Table names
    private static final String LINKSET_TABLE_NAME = "linkset";
    private static final String DATASET_TABLE_NAME = "dataset";
    private static final String DISTRIBUTION_TABLE_NAME = "distribution";
    private static final String VOID_URIS_TABLE_NAME = "voidUris";
    private static final String VOID_DATES_TABLE_NAME = "voidDates";
    
    //Column Names
    private static final String DATASET_URI_COLUMN_NAME = "URI";
    private static final String DESCRIPTION_COLUMN_NAME = "description";
    private static final String DISTRIBUTION_COLUMN_NAME = "distribution";
    private static final String DISTRIBUTION_URI_COLUMN_NAME = "URI";
    private static final String LINKSET_URI_COLUMN_NAME = "URI";
    private static final String OBJECT_DATATYPE_COLUMN_NAME = "objectType";
    private static final String OBJECT_COLUMN_NAME = "object";
    private static final String OBJECT_SPECIES_COLUMN_NAME = "objectSpecies";
    private static final String OBJECT_URI_COLUMN_NAME = "objectURI";
    private static final String PREDICATE_COLUMN_NAME = "predicate";
    private static final String SIZE_COLUMN_NAME = "size";
    private static final String SUBJECT_COLUMN_NAME = "subject";
    private static final String SUBJECT_DATATYPE_COLUMN_NAME = "subjectType";
    private static final String SUBJECT_SPECIES_COLUMN_NAME = "subjectSpecies";
    private static final String SUBJECT_URI_COLUMN_NAME = "subjectURI";
    private static final String TITLE_COLUMN_NAME = "title";
    private static final String VERSION_COLUMN_NAME = "VERSION";
    //Size Variable
            //This could be anything so needs to be long
    private static final int ID_URI_LENGTH = 200;
            //These are from controled vocabularies so can be shorter
    private static final int URI_LENGTH = 70;
    private static final int TITLE_LENGTH = 70;
    private static final int VERSION_LENGTH = 25;

    public static ImsMapper getExisting() throws BridgeDBException {
        if (mapper == null) {
            CodeMapper codeMapper = new RdfBasedCodeMapper();
            mapper = new ImsMapper(false, codeMapper);
        }
        return mapper;
    }

    public static ImsMapper createNew() throws BridgeDBException {
        CodeMapper codeMapper = new RdfBasedCodeMapper();
        mapper = new ImsMapper(true, codeMapper);
        return mapper;
    }

    /**
     * Creates a new UriMapper including BridgeDB implementation based on a
     * connection to the SQL Database.
     *
     * @param dropTables Flag to determine if any existing tables should be
     * dropped and new empty tables created.
     */
    private ImsMapper(boolean dropTables, CodeMapper codeMapper) throws BridgeDBException {
        super(dropTables, codeMapper);
    }

    @Override
    protected void dropSQLTables() throws BridgeDBException
    {
        super.dropSQLTables();
        dropTable(LINKSET_TABLE_NAME);
        dropTable(DATASET_TABLE_NAME);
        dropTable(DISTRIBUTION_TABLE_NAME);
        dropTable(VOID_URIS_TABLE_NAME);
        dropTable(VOID_DATES_TABLE_NAME);
    }

    @Override
    protected void createSQLTables() throws BridgeDBException {
        super.createSQLTables();
        Statement sh = null;
        try {
            sh = createStatement();
            sh.execute("CREATE TABLE " + LINKSET_TABLE_NAME
                    + "( " + MAPPING_SET_ID_COLUMN_NAME + " INT, "
                    + "  " + LINKSET_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + TITLE_COLUMN_NAME + " VARCHAR(" + TITLE_LENGTH + ") NOT NULL, "
                    + "  " + DESCRIPTION_COLUMN_NAME + " TEXT, "
                    + "  " + SUBJECT_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + SUBJECT_DATATYPE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_DATATYPE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL, "
                    + "  " + SUBJECT_SPECIES_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_SPECIES_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + DATASET_TABLE_NAME
                    + "  (  " + DATASET_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + TITLE_COLUMN_NAME + " VARCHAR(" + TITLE_LENGTH + ") NOT NULL, "
                    + "  " + DESCRIPTION_COLUMN_NAME + " TEXT, "
                    + "  " + VERSION_COLUMN_NAME + " VARCHAR(" + VERSION_LENGTH + ") NOT NULL, "
                    + "  " + DISTRIBUTION_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + DISTRIBUTION_TABLE_NAME
                    + "  (  " + DISTRIBUTION_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + SIZE_COLUMN_NAME + " VARCHAR(" + VERSION_LENGTH + ") "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + VOID_URIS_TABLE_NAME
                    + "  (  " + SUBJECT_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + PREDICATE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + VOID_DATES_TABLE_NAME
                    + "  (  " + SUBJECT_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + PREDICATE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_COLUMN_NAME + " DATETIME NOT NULL "
                    + "  ) " + SqlFactory.engineSetting());

        } catch (SQLException e) {
            throw new BridgeDBException("Error creating the tables ", e);
        } finally {
            close(sh, null);
        }
    }

}
