/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Constants {
	//SYNC SERVICE CONSTANTS
	public static final String SYNC_ENTITY_ID = "debats";
	public static final String SYNC_EVENTS = "sync-events";
	//PUBLICATION SERVICE CONSTANTS
	public static final String PUBLICATION_EVENTS = "publication-events";
	
	//DATA JO CONSTANTS
	public static final String START_YEAR = "2011";
	public static final String DATA_ROOT_URL = "http://data.journal-officiel.gouv.fr/index.php";
	public static final String DATA_ROOT_DIR = "?dir=Debats/AN";
	public static final String DATASET_DIR = "datasets";
	public static final String DATASET_EXTENSION = ".taz";
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String CRI_PREFIX = "CRI";
	public static final String AAA_PREFIX = "AAA";
	
	//PUBLICATION PARSING CONSTANTS
	public static final String ELEMENT_PUBLICATIONDANBLANC = "publicationdanblanc";
	public static final String ELEMENT_METADONNEES = "metadonnees";
	public static final String ELEMENT_DATEPARUTION = "dateparution";
	public static final String ELEMENT_NUMPARUTION = "numparution";
	public static final String ELEMENT_PARUTION = "parution";
	public static final String ELEMENT_GREBICHE = "numerogrebiche";
	public static final String ELEMENT_DATESEANCE = "dateseance";
	public static final String ELEMENT_STARTPERIODE = "dateacompterdu";
	public static final String ELEMENT_ENDPERIODE = "datejusquau";
	public static final String ELEMENT_SESSIONPARLEMENTAIRE = "sessionparlementaire";
	public static final String ELEMENT_TYPESESSION = "sessionord";
	public static final String ELEMENT_SESSION = "sessionnom";
	public static final String ELEMENT_NUMSEANCE = "numseance";
	public static final String ELEMENT_COMPTERENDU = "compterendu";
	public static final String ELEMENT_PRESIDENTSEANCE = "presidentseance";
	public static final String ELEMENT_QUALITEPRESIDENT = "qualitepresident";
	public static final String ELEMENT_PARA = "para";
	public static final String ELEMENT_NOTA = "nota";
	public static final String ELEMENT_SOMMAIRE = "sommaire";
	public static final String ELEMENT_ORATEUR = "orateur";
	public static final String ELEMENT_NOM = "nom";
	public static final String ELEMENT_SECTION = "section";
	public static final String ELEMENT_SOUSSECTION1 = "soussection1";
	public static final String ELEMENT_SOUSSECTION2 = "soussection2";
	public static final String ELEMENT_TITRE = "titrestruct";
	public static final String ELEMENT_QUALITEMOUVEMENT = "qualitemouvement";
	public static final String ELEMENT_RESULTATVOTE = "resultatvote";
	public static final String ELEMENT_NOMBREVOTANT = "nombrevotants";
	public static final String ELEMENT_NOMBRESUFFRAGE = "vombresuffrageexprime";
	public static final String ELEMENT_MAJORITESUFFRAGE = "majoritesuffrageexprime";
	public static final String ELEMENT_POUR = "pour";
	public static final String ELEMENT_CONTRE = "contre";
	public static final String ELEMENT_LIBELLE = "libelle";
	public static final String ELEMENT_VALEUR = "valeur";
	public static final String OLD_SCHEMA = "..\\..\\..\\schemas_Debats\\publications\\pub_DAN_Fasciculeblanc_V01.xsd";
	public static final String NEW_SCHEMA = "CahierBlanc_Etalab.xsd";
	
	public static Path getDatasetPath(String ref) {
		return Paths.get(USER_DIR, DATASET_DIR, ref+DATASET_EXTENSION);
	}
	
	
	
	//regex
	public static final String HTML_A_HREF_PATTERN = "\\s*(?i)href\\s*=\\s*(\\\"([^\"]*\\\")|'[^']*'|([^'\">\\s]+))";
	public static final Pattern linkPattern = Pattern.compile(HTML_A_HREF_PATTERN);
}
