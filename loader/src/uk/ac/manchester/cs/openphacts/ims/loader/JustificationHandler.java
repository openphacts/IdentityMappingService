/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.manchester.cs.openphacts.ims.loader;

import org.bridgedb.uri.lens.Lens;

/**
 *
 * @author christian
 */
public class JustificationHandler {
    
    public static final String IS_UNCHARGED_COUNTER_PART_OF = "http://semanticscience.org/resource/CHEMINF_000495";
    public static final String HAS_UNCHARGED_COUNTERPART = "http://semanticscience.org/resource/CHEMINF_000460";
    public static final String HAS_ISOTOPICALLY_UNSPECIFIED_PARENT = "http://semanticscience.org/resource/CHEMINF_000459";
    public static final String IS_ISOTOPICALLY_UNSPECIFIED_PARENT_OF = "http://semanticscience.org/resource/CHEMINF_000489";
    public static final String HAS_STEREO_UNDEFINED_PARENT = "http://semanticscience.org/resource/CHEMINF_000456";
    public static final String IS_STEREO_UNDEFINED_PARENT_OF = "http://semanticscience.org/resource/CHEMINF_000491";
    public static final String  HAS_OPS_NORMALIZED_COUNTER_PART = "http://semanticscience.org/resource/CHEMINF_000458";
    public static final String  IS_OPS_NORMALIZED_COUNTER_PART_OF = "http://semanticscience.org/resource/CHEMINF_000494";
    public static final String  PROTEIN_CODING_GENE = "http://semanticscience.org/resource/SIO_000985";

    public static String getInverse(String justification) {
        if (justification.equals(IS_UNCHARGED_COUNTER_PART_OF)){
            return HAS_UNCHARGED_COUNTERPART;
        }
        if (justification.equals(HAS_UNCHARGED_COUNTERPART)){
            return IS_UNCHARGED_COUNTER_PART_OF;
        }
        if (justification.equals(HAS_ISOTOPICALLY_UNSPECIFIED_PARENT)){
            return IS_ISOTOPICALLY_UNSPECIFIED_PARENT_OF;
        }
        if (justification.equals(IS_ISOTOPICALLY_UNSPECIFIED_PARENT_OF)){
            return HAS_ISOTOPICALLY_UNSPECIFIED_PARENT;
        }
        if (justification.equals(HAS_STEREO_UNDEFINED_PARENT)){
            return IS_STEREO_UNDEFINED_PARENT_OF;
        }
        if (justification.equals(IS_STEREO_UNDEFINED_PARENT_OF)){
            return HAS_STEREO_UNDEFINED_PARENT;
        }
        if (justification.equals(HAS_OPS_NORMALIZED_COUNTER_PART)){
            return IS_OPS_NORMALIZED_COUNTER_PART_OF;
        }
        if (justification.equals(IS_OPS_NORMALIZED_COUNTER_PART_OF)){
            return HAS_OPS_NORMALIZED_COUNTER_PART;
        }
        if (justification.equals("http://purl.obolibrary.org/obo#part_of")){
            return "http://purl.obolibrary.org/obo#has_part";
        }
        if (justification.equals("http://purl.obolibrary.org/obo#has_part")){
            return "http://purl.obolibrary.org/obo#part_of";
        }
        //if (justification.equals("http://example.com/ConceptWikiGene")){
        //    return PROTEIN_CODING_GENE;
        //}
        //if (justification.equals("http://example.com/ConceptWikiProtein")){
        //    return PROTEIN_CODING_GENE;
        //}
        if (justification.equals(Lens.getTestJustifictaion() + "Forward")){
            return Lens.getTestJustifictaion() + "BackWard";
        }
        if (justification.equals(Lens.getTestJustifictaion() + "BackWard")){
            return Lens.getTestJustifictaion() + "Forward";
        }
        return justification;
    }
    
    public static String getForward(String justification) {
        if (justification.equals("http://example.com/ConceptWikiGene")){
            return PROTEIN_CODING_GENE;
        }
        if (justification.equals("http://example.com/ConceptWikiProtein")){
            return PROTEIN_CODING_GENE;
        }
        return justification;
    }
}
