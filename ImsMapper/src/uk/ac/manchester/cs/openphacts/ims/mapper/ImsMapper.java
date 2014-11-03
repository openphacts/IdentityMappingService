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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bridgedb.pairs.CodeMapper;
import org.bridgedb.rdf.pairs.RdfBasedCodeMapper;
import org.bridgedb.sql.SQLUriMapper;
import org.bridgedb.sql.SqlFactory;
import org.bridgedb.utils.BridgeDBException;
import org.openrdf.model.URI;

/**
 *
 * @author christian
 */
public class ImsMapper extends SQLUriMapper implements ImsListener{
    
    private static ImsMapper mapper = null;

    private String addLinksetVoidQuery = null;
    private String addDatasetVoidQuery = null;
    private String addDistributionVoidQuery = null;
    private String addLinksetURIQuery = null;
    private String addLinksetDateQuery = null;

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
                    + "  " + TITLE_COLUMN_NAME + " VARCHAR(" + TITLE_LENGTH + ") , "
                    + "  " + DESCRIPTION_COLUMN_NAME + " TEXT, "
                    + "  " + SUBJECT_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + SUBJECT_DATATYPE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") , "
                    + "  " + OBJECT_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + OBJECT_DATATYPE_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") , "
                    + "  " + SUBJECT_SPECIES_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") , "
                    + "  " + OBJECT_SPECIES_COLUMN_NAME + " VARCHAR(" + URI_LENGTH + ") "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + DATASET_TABLE_NAME
                    + "  (  " + DATASET_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + TITLE_COLUMN_NAME + " VARCHAR(" + TITLE_LENGTH + ") , "
                    + "  " + DESCRIPTION_COLUMN_NAME + " TEXT, "
                    + "  " + VERSION_COLUMN_NAME + " VARCHAR(" + VERSION_LENGTH + ") , "
                    + "  " + DISTRIBUTION_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") "
                    + "  ) " + SqlFactory.engineSetting());
            sh.execute("CREATE TABLE " + DISTRIBUTION_TABLE_NAME
                    + "  (  " + DISTRIBUTION_URI_COLUMN_NAME + " VARCHAR(" + ID_URI_LENGTH + ") NOT NULL, "
                    + "  " + VERSION_COLUMN_NAME + " VARCHAR(" + VERSION_LENGTH + ") , "
                    + "  " + SIZE_COLUMN_NAME + " INT "
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

    public void addLinksetVoid(int mappingSetId, URI linksetId, String linksetTitle, String linksetDescription, 
            URI linksetSubjectsTarget, URI linksetSubjectsType, URI linksetObjectsTarget, URI linksetObjectsType, 
            URI linksetSubjectSpecies, URI linksetObjectsSpecies) throws BridgeDBException{
        PreparedStatement statement = null;
        if (addLinksetVoidQuery == null){
           addLinksetVoidQuery = "INSERT INTO " + LINKSET_TABLE_NAME
                    + " ( " + MAPPING_SET_ID_COLUMN_NAME + " , " //1
                    + LINKSET_URI_COLUMN_NAME + " , "  //2
                    + TITLE_COLUMN_NAME + " , "  //3
                    + DESCRIPTION_COLUMN_NAME + " , " //4
                    + SUBJECT_URI_COLUMN_NAME + " , " //5
                    + SUBJECT_DATATYPE_COLUMN_NAME + " , "  //6
                    + OBJECT_URI_COLUMN_NAME + " , "  //7
                    + OBJECT_DATATYPE_COLUMN_NAME + " , "  //8
                    + SUBJECT_SPECIES_COLUMN_NAME + " , "  //9
                    + OBJECT_SPECIES_COLUMN_NAME + " ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ?) "; //10
        }
        try {
            statement = createPreparedStatement(addLinksetVoidQuery);   
            statement.setInt(1, mappingSetId);
            statement.setString(2, toString(linksetId));
            statement.setString(3, linksetTitle);
            statement.setString(4, linksetDescription);
            statement.setString(5, toString(linksetSubjectsTarget));
            statement.setString(6, toString(linksetSubjectsType));
            statement.setString(7, toString(linksetObjectsTarget));
            statement.setString(8, toString(linksetObjectsType));
            statement.setString(9, toString(linksetSubjectSpecies));
            statement.setString(10, toString(linksetObjectsSpecies));
            statement.executeUpdate();
        } catch (BridgeDBException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error updating using " + statement, ex);
        } finally {
            close(statement, null);
        }
    }
    
    public void addDatasetVoid(URI dataSetId, String title, String description, String version, URI distribution) throws BridgeDBException {
        PreparedStatement statement = null;
        if (addDatasetVoidQuery == null){
           addDatasetVoidQuery = "INSERT INTO " + DATASET_TABLE_NAME
                    + " ( " + DATASET_URI_COLUMN_NAME + " , " //1
                    + TITLE_COLUMN_NAME + " , "  //2
                    + DESCRIPTION_COLUMN_NAME + " , " //3
                    + VERSION_COLUMN_NAME + " , " //4
                    + DISTRIBUTION_COLUMN_NAME + " ) VALUES ( ? , ? , ? , ?, ?) "; //5
        } 
        try {
            statement = createPreparedStatement(addDatasetVoidQuery);   
            statement.setString(1, toString(dataSetId));
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4,version);
            statement.setString(5, toString(distribution));
            statement.executeUpdate();
        } catch (BridgeDBException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error updating using " + statement, ex);
        } finally {
            close(statement, null);
        }
    }
    
    private String toString(URI uri){
        if (uri == null){
            return null;
        } else {
            return uri.stringValue();
        }
    }
    
    public void loadStatement(URI linksetId, URI predicate, URI object) throws BridgeDBException {
        if (addLinksetURIQuery == null){
            addLinksetURIQuery = "INSERT INTO " + VOID_URIS_TABLE_NAME
                    + "  (  " + SUBJECT_COLUMN_NAME + " , "
                    + PREDICATE_COLUMN_NAME + " , "
                    + OBJECT_COLUMN_NAME + ") VALUES ( ?, ?, ?)";
        } 
        PreparedStatement statement = null;
        try {
            statement = createPreparedStatement(addLinksetURIQuery);   
            statement.setString(1, toString(linksetId));
            statement.setString(2, toString(predicate));
            statement.setString(3, toString(object));
            statement.executeUpdate();
        } catch (BridgeDBException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error updating using " + statement, ex);
        } finally {
            close(statement, null);
        }    
    }

    public void loadStatement(URI linksetId, URI predicate, XMLGregorianCalendar xmlDate) throws BridgeDBException {
        if (addLinksetDateQuery == null){
            addLinksetDateQuery = "INSERT INTO " + VOID_DATES_TABLE_NAME
                    + "  (  " + SUBJECT_COLUMN_NAME + " , "
                    + PREDICATE_COLUMN_NAME + " , "
                    + OBJECT_COLUMN_NAME + ") VALUES ( ?, ?, ?)";
        } 
        PreparedStatement statement = null;
        try {
            statement = createPreparedStatement(addLinksetDateQuery);   
            statement.setString(1, toString(linksetId));
            statement.setString(2, toString(predicate));
            java.util.Date date = xmlDate.toGregorianCalendar().getTime();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime()); 
            statement.setDate(3, sqlDate);
            statement.executeUpdate();
        } catch (BridgeDBException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new BridgeDBException ("Error updating using " + statement, ex);
        } finally {
            close(statement, null);
        }    
    }
    
}
