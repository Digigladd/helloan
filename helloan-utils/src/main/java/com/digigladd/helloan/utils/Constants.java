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
	public static final String ELEMENT_METADONNEES = "Metadonnees";
	public static final String ELEMENT_DATEPARUTION = "dateParution";
	public static final String ELEMENT_NUMPARUTION = "numParution";
	public static final String ELEMENT_GREBICHE = "numeroGrebiche";
	public static final String ELEMENT_DATESEANCE = "dateSeance";
	public static final String ELEMENT_STARTPERIODE = "dateACompterDu";
	public static final String ELEMENT_ENDPERIODE = "dateJusquau";
	public static final String ELEMENT_TYPESESSION = "sessionOrd";
	public static final String ELEMENT_NUMSEANCE = "numSeance";
	public static final String ELEMENT_COMPTERENDU = "CompteRendu";
	public static final String ELEMENT_PRESIDENTSEANCE = "PresidentSeance";
	public static final String ELEMENT_QUALITEPRESIDENT = "QualitePresident";
	public static final String ELEMENT_PARA = "Para";
	public static final String ELEMENT_NOTA = "Nota";
	public static final String ELEMENT_SOMMAIRE = "Sommaire";
	public static final String ELEMENT_ORATEUR = "Orateur";
	public static final String ELEMENT_NOM = "Nom";
	public static final String ELEMENT_SECTION = "Section";
	public static final String ELEMENT_SOUSSECTION1 = "SousSection1";
	public static final String ELEMENT_SOUSSECTION2 = "SousSection2";
	public static final String ELEMENT_TITRE = "TitreStruct";
	public static final String ELEMENT_QUALITEMOUVEMENT = "QualiteMouvement";
	public static final String ELEMENT_RESULTATVOTE = "ResultatVote";
	public static final String ELEMENT_NOMBREVOTANT = "NombreVotants";
	public static final String ELEMENT_NOMBRESUFFRAGE = "NombreSuffrageExprime";
	public static final String ELEMENT_MAJORITESUFFRAGE = "MajoriteSuffrageExprime";
	public static final String ELEMENT_POUR = "Pour";
	public static final String ELEMENT_CONTRE = "Contre";
	public static final String ELEMENT_LIBELLE = "Libelle";
	public static final String ELEMENT_VALEUR = "Valeur";
	
	public static Path getDatasetPath(String ref) {
		return Paths.get(USER_DIR, DATASET_DIR, ref+DATASET_EXTENSION);
	}
	
	
	
	//regex
	public static final String HTML_A_HREF_PATTERN = "\\s*(?i)href\\s*=\\s*(\\\"([^\"]*\\\")|'[^']*'|([^'\">\\s]+))";
	public static final Pattern linkPattern = Pattern.compile(HTML_A_HREF_PATTERN);
}
