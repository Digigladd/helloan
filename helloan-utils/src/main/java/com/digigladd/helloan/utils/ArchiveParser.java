/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class ArchiveParser {
	private static final Logger log = LoggerFactory.getLogger(ArchiveParser.class);
	
	public static Metadonnees getCRI(String ref) {
		return getFromArchive(ref, Constants.CRI_PREFIX);
	}
	
	public static Metadonnees getAAA(String ref) {
		return getFromArchive(ref, Constants.AAA_PREFIX);
	}
	
	private static Metadonnees getFromArchive(String ref, String prefix) {
		Metadonnees metadonnees = null;
		log.info("getFromArchive {}, {}", ref, prefix);
		if (prefix.equalsIgnoreCase(Constants.AAA_PREFIX) || prefix.equalsIgnoreCase(Constants.CRI_PREFIX)) {
			
			Path uploadPath = Constants.getDatasetPath(ref);
			try (InputStream fi = Files.newInputStream(uploadPath);
				 InputStream bi = new BufferedInputStream(fi);
				 InputStream ci = new CompressorStreamFactory()
						 .createCompressorInputStream(bi);
				 ArchiveInputStream i = new ArchiveStreamFactory()
						 .createArchiveInputStream(new BufferedInputStream(ci))) {
				ArchiveEntry entry;
				
				while ((entry = i.getNextEntry()) != null) {
					log.info("getFromArchive, entry {}, {}", entry.getName(), entry.getSize());
					if (entry.getName().indexOf("breaks") < 0) {
						if (entry.getName().startsWith(prefix)) {
							if (!i.canReadEntryData(entry)) {
								// log something?
								continue;
							}
							metadonnees = parsePublication(i);
						} else {
							log.info("getFromArchive skip {}", entry.getName());
						}
					}
					if (entry.getName().startsWith("PARU")) {
						metadonnees = parseParution(i);
					}
				}
				return metadonnees;
			} catch (Exception e) {
				log.error("getFromArchive error {}: {}", ref, e.getMessage());
				if (e instanceof CompressorException) {
					return getFromArchive(ref, prefix, false);
				} else {
					return metadonnees;
				}
			}
		}
		return null;
	}
	
	private static Metadonnees getFromArchive(String ref, String prefix, boolean compressor) {
		log.info("getFromArchive {}, {}, {}", ref, prefix, compressor);
		if (compressor) {
			return getFromArchive(ref, prefix);
		} else {
			Metadonnees metadonnees = null;
			if (prefix.equalsIgnoreCase(Constants.AAA_PREFIX) || prefix.equalsIgnoreCase(Constants.CRI_PREFIX)) {
				Path uploadPath = Constants.getDatasetPath(ref);
				try (InputStream fi = Files.newInputStream(uploadPath);
					 InputStream bi = new BufferedInputStream(fi);
					 ArchiveInputStream i = new ArchiveStreamFactory()
							 .createArchiveInputStream(bi)
					 ) {
					ArchiveEntry entry;
					
					while ((entry = i.getNextEntry()) != null) {
						log.info("getFromArchive, entry {}, {}", entry.getName(), entry.getSize());
						if (entry.getName().indexOf(".tar") >= 0) {
							return parseTarArchive(ref, prefix, i);
						}
						if (entry.getName().startsWith(prefix)) {
							if (!i.canReadEntryData(entry)) {
								// log something?
								continue;
							}
							metadonnees = parsePublication(i);
						} else {
							log.info("getFromArchive skip {}", entry.getName());
						}
					}
					return metadonnees;
				} catch (Exception e) {
					log.error("getFromArchive error {}: {}", ref, e.getMessage());
					return metadonnees;
				}
			}
			return null;
		}
	}
	
	private static Metadonnees parseTarArchive(String ref, String prefix, InputStream is) {
		log.info("parseTarArchive {}, {}", ref, prefix);
		Metadonnees metadonnees = null;
		try (ArchiveInputStream i = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is))) {
			ArchiveEntry entry;
			
			while ((entry = i.getNextEntry()) != null) {
				log.info("parseTarArchive, entry {}, {}", entry.getName(), entry.getSize());
				if (entry.getName().indexOf("breaks") < 0) {
					if (entry.getName().startsWith(prefix)) {
						if (!i.canReadEntryData(entry)) {
							// log something?
							continue;
						}
						metadonnees = parsePublication(i);
					} else {
						log.info("parseTarArchive skip {}", entry.getName());
					}
				}
				if (entry.getName().startsWith("PARU")) {
					metadonnees = parseParution(i);
				}
			}
			return metadonnees;
		} catch (Exception e) {
			log.error("parseTarArchive error {}: {}", ref, e.getMessage());
			return metadonnees;
		}
	}
	
	private static Metadonnees parseParution(InputStream is) {
		final XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isCoalescing",Boolean.TRUE);
		try {
			if (is != null) {
				final XMLEventReader reader = factory.createXMLEventReader(is);
				
				Metadonnees metadonnees = null;
				while (reader.hasNext()) {
					final XMLEvent event = reader.nextEvent();
					
					if (event.isStartElement()) {
						final StartElement element = event.asStartElement();
						final String elementName = element.getName().getLocalPart();
						switch (elementName.toLowerCase()) {
							case Constants.ELEMENT_PARUTION:
								metadonnees = parseMetadonnees(reader);
								break;
						}
					}
				}
				//reader.close();
				return metadonnees;
			}
		} catch (Exception e) {
			log.info("parseParution error: {}", e.getMessage());
		}
		return null;
	}
	
	private static Metadonnees parsePublication(InputStream is) {
		final XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isCoalescing",Boolean.TRUE);
		boolean inCompteRendu = false;
		boolean oldSchema = true;
		try {
			if (is != null) {
				final XMLEventReader reader = factory.createXMLEventReader(is);
				
				Metadonnees metadonnees = null;
				while (reader.hasNext()) {
					final XMLEvent event = reader.nextEvent();
					
					if (event.isStartElement()) {
						final StartElement element = event.asStartElement();
						final String elementName = element.getName().getLocalPart();
						switch (elementName.toLowerCase()) {
							case Constants.ELEMENT_PUBLICATIONDANBLANC:
								Iterator it = element.getAttributes();
								while (it.hasNext()) {
									Attribute att = (Attribute)it.next();
									oldSchema = att.getValue().equalsIgnoreCase(Constants.OLD_SCHEMA);
								}
								log.info("parsePublication, old schema {}", oldSchema);
								break;
							case Constants.ELEMENT_METADONNEES:
								if (!inCompteRendu) {
									metadonnees = parseMetadonnees(reader);
								} else {
									Metadonnees newMetadonnees = parseMetadonnees(reader);
									if (!oldSchema) {
										metadonnees.setDateParution(newMetadonnees.getDateSeance().plusDays(1));
										metadonnees.setNumParution(newMetadonnees.getNumParution());
										metadonnees.setDateSeance(newMetadonnees.getDateSeance());
										if(metadonnees.getNumSeance() == 0) {
											metadonnees.setNumSeance(newMetadonnees.getNumSeance());
										}
									}
								}
								break;
							case Constants.ELEMENT_COMPTERENDU:
								
								inCompteRendu = true;
								metadonnees.setNrSeance(metadonnees.getNrSeance()+1);
								break;
								
						}
					}
				}
				//reader.close();
				log.info("parsePublication, metadonnees {}", metadonnees);
				return metadonnees;
			}
		} catch (Exception e) {
			log.info("parsePublication error: {}", e.getMessage());
		}
		return null;
	}
	
	private static Metadonnees parseMetadonnees(final XMLEventReader reader) throws XMLStreamException {
		
		Metadonnees metadonnees = new Metadonnees();
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_METADONNEES)) {
				break;
			}
			if (event.isStartElement()) {
				final StartElement element = event.asStartElement();
				final String elementName = element.getName().getLocalPart();
				
				switch (elementName.toLowerCase()) {
					case Constants.ELEMENT_NUMSEANCE:
						metadonnees.setNumSeance(Integer.parseInt(reader.getElementText()));
						break;
					case Constants.ELEMENT_DATEPARUTION:
						final String dateParution = reader.getElementText();
						metadonnees.setDateParution(parseDate(dateParution));
						break;
					case Constants.ELEMENT_DATESEANCE:
						final String dateSeance = reader.getElementText();
						metadonnees.setDateSeance(parseDate(dateSeance));
						break;
					case Constants.ELEMENT_ENDPERIODE:
						final String endPeriode = reader.getElementText();
						metadonnees.setPeriodeAu(parseDate(endPeriode));
						break;
					case Constants.ELEMENT_GREBICHE:
						metadonnees.setNumeroGrebiche(reader.getElementText());
						break;
					case Constants.ELEMENT_NUMPARUTION:
						metadonnees.setNumParution(Integer.parseInt(reader.getElementText()));
						break;
					case Constants.ELEMENT_STARTPERIODE:
						final String startPeriode = reader.getElementText();
						metadonnees.setPeriodeDu(parseDate(startPeriode));
						break;
					case Constants.ELEMENT_TYPESESSION:
						metadonnees.setTypeSession(reader.getElementText());
						break;
					case Constants.ELEMENT_PARUTION:
						metadonnees.setNumParution(Integer.parseInt(reader.getElementText()));
						break;
					case Constants.ELEMENT_SESSIONPARLEMENTAIRE:
						String session = reader.getElementText();
						String[] years = session.split("-");
						metadonnees.setPeriodeDu(LocalDate.of(Integer.parseInt(years[0]), 1, 1));
						metadonnees.setPeriodeAu(LocalDate.of(Integer.parseInt(years[1]), 1, 1));
						break;
					case Constants.ELEMENT_SESSION:
						metadonnees.setTypeSession(reader.getElementText());
						break;
				}
			}
		}
		return metadonnees;
	}
	
	public static CompteRendu getCompteRendu(String ref, Integer session) {
		log.info("getCompteRendu {}, {}", ref, session);
		Path uploadPath = Constants.getDatasetPath(ref);
		CompteRendu compteRendu = null;
		try (InputStream fi = Files.newInputStream(uploadPath);
			 InputStream bi = new BufferedInputStream(fi);
			 InputStream ci = new CompressorStreamFactory()
					 .createCompressorInputStream(bi);
			 ArchiveInputStream i = new ArchiveStreamFactory()
					 .createArchiveInputStream(new BufferedInputStream(ci))) {
			ArchiveEntry entry;
			while ((entry = i.getNextEntry()) != null) {
				log.info("getCompteRendu entry {}, {}", entry.getName(), entry.getSize());
				if (entry.getName().startsWith(Constants.CRI_PREFIX)) {
					if (!i.canReadEntryData(entry)) {
						// log something?
						continue;
					}
					compteRendu = parseComptesRendus(i, session);
				} else {
					log.info("getCompteRendu skip {}", entry.getName());
				}
			}
		} catch (Exception e) {
			log.error("getCompteRendu error {}: {}", ref, e.getMessage());
			if (e instanceof CompressorException) {
				return getCompteRendu(ref, session, false);
			}
		}
		return compteRendu;
	}
	
	public static CompteRendu getCompteRendu(String ref, Integer session, boolean compressor) {
		log.info("getCompteRendu {}, {}, {}", ref, session, compressor);
		if (compressor) {
			return getCompteRendu(ref, session);
		} else {
			Path uploadPath = Constants.getDatasetPath(ref);
			CompteRendu compteRendu = null;
			try (InputStream fi = Files.newInputStream(uploadPath);
				 InputStream bi = new BufferedInputStream(fi);
				 ArchiveInputStream i = new ArchiveStreamFactory()
						 .createArchiveInputStream(new BufferedInputStream(bi))) {
				ArchiveEntry entry;
				while ((entry = i.getNextEntry()) != null) {
					log.info("getCompteRendu entry {}, {}", entry.getName(), entry.getSize());
					if (entry.getName().indexOf(".tar") >= 0) {
						return parseTarArchive(ref, session, i);
					}
				}
			} catch (Exception e) {
				log.error("getCompteRendu error {}: {}", ref, e.getMessage());
			}
			return compteRendu;
		}
		
	}
	
	public static CompteRendu parseTarArchive(String ref, Integer session, InputStream is) {
		log.info("parseTarArchive {}, {}", ref, session);
		CompteRendu compteRendu = null;
		try (
			 ArchiveInputStream i = new ArchiveStreamFactory()
					 .createArchiveInputStream(new BufferedInputStream(is))) {
			ArchiveEntry entry;
			while ((entry = i.getNextEntry()) != null) {
				log.info("parseTarArchive entry {}, {}", entry.getName(), entry.getSize());
				if (entry.getName().startsWith(Constants.CRI_PREFIX)) {
					if (!i.canReadEntryData(entry)) {
						// log something?
						continue;
					}
					compteRendu = parseComptesRendus(i, session);
				} else {
					log.info("parseTarArchive skip {}", entry.getName());
				}
			}
		} catch (Exception e) {
			log.error("parseTarArchive error {}: {}", ref, e.getMessage());
		}
		return compteRendu;
	}
	
	private static CompteRendu parseComptesRendus(InputStream is, Integer session) {
		final XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isCoalescing",Boolean.TRUE);
		CompteRendu compteRendu = null;
		int currentSession = 0;
		try {
			if (is != null) {
				final XMLEventReader reader = factory.createXMLEventReader(is,"UTF-8");
				while (reader.hasNext()) {
					final XMLEvent event = reader.nextEvent();
					if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_COMPTERENDU)) {
						currentSession += 1;
						if (currentSession == session) {
							compteRendu = parseCompteRendu(reader);
						}
					}
				}
				reader.close();
			}
		} catch (Exception e) {
			log.info("parseComptesRendus error: {}", e.getMessage());
		}
		return compteRendu;
	}
	
	private static CompteRendu parseCompteRendu(final XMLEventReader reader) throws XMLStreamException {
		boolean inSommaire = false;
		boolean inSection = false;
		boolean inSousSection1 = false;
		boolean inSousSection2 = false;
		CompteRendu compteRendu = new CompteRendu();
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_COMPTERENDU)) {
				break;
			}
			if (event.isStartElement()) {
				final StartElement element = event.asStartElement();
				final String elementName = element.getName().getLocalPart();
				
				switch (elementName.toLowerCase()) {
					case Constants.ELEMENT_PRESIDENTSEANCE:
						if (inSommaire) {
							compteRendu.setPresidentSeance(reader.getElementText());
						}
						break;
					case Constants.ELEMENT_QUALITEPRESIDENT:
						compteRendu.setQualitePresident(reader.getElementText());
						break;
					case Constants.ELEMENT_PARA:
						if (!inSommaire) {
							compteRendu.addEvenement(parsePara(reader));
						}
						break;
					case Constants.ELEMENT_NOTA:
						if(!inSommaire) {
							compteRendu.addEvenement(parseNota(reader));
						}
						break;
					case Constants.ELEMENT_SOMMAIRE:
						inSommaire = true;
						break;
					case Constants.ELEMENT_SECTION:
						inSection = true;
						break;
					case Constants.ELEMENT_SOUSSECTION1:
						inSousSection1 = true;
						break;
					case Constants.ELEMENT_SOUSSECTION2:
						inSousSection2 = true;
						break;
					case Constants.ELEMENT_TITRE:
						if (!inSommaire) {
							compteRendu.addEvenement(parseTitre(reader, inSection, inSousSection1, inSousSection2));
						}
						break;
					case Constants.ELEMENT_RESULTATVOTE:
						compteRendu.addEvenement(parseResultatVote(reader));
						break;
				}
			}
			if (event.isEndElement()) {
				final EndElement element = event.asEndElement();
				final String elementName = element.getName().getLocalPart();
				switch (elementName.toLowerCase()){
					case Constants.ELEMENT_SOMMAIRE:
						inSommaire = false;
						break;
					case Constants.ELEMENT_SECTION:
						inSection = false;
						break;
					case Constants.ELEMENT_SOUSSECTION1:
						inSousSection1 = false;
						break;
					case Constants.ELEMENT_SOUSSECTION2:
						inSousSection2 = false;
						break;
				}
			}
		}
		return compteRendu;
	}
	
	private static Para parsePara(final XMLEventReader reader) throws XMLStreamException {
		Para para = new Para();
		boolean inOrateur = false;
		
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_PARA)) {
				break;
			}
			if (event.isStartElement()) {
				final StartElement element = event.asStartElement();
				final String elementName = element.getName().getLocalPart();
				
				switch (elementName.toLowerCase()) {
					case Constants.ELEMENT_ORATEUR:
						inOrateur = true;
						break;
					case Constants.ELEMENT_NOM:
						para.setOrateur(reader.getElementText());
						break;
					case Constants.ELEMENT_QUALITEMOUVEMENT:
						para.setQualite(reader.getElementText());
						break;
				}
			}
			if (event.isEndElement()) {
				final EndElement element = event.asEndElement();
				final String elementName = element.getName().getLocalPart();
				
				switch (elementName.toLowerCase()) {
					case Constants.ELEMENT_ORATEUR:
						inOrateur = false;
						break;
				}
			}
			if (event.isCharacters()) {
				Characters element = event.asCharacters();
				String data = clean(element.getData());
				if (!inOrateur && !data.isEmpty()) {
					para.setText(para.getText()+data);
				}
			}
		}
		log.info("para text: {}", para.getText());
		para.setText(para.getText().replaceAll("\\&#\\d*+;",""));
		
		return para;
	}
	
	private static String clean(String data) {
		
		data = data.replaceAll("\n","").trim();
		if (data.startsWith(".")) {
			data = data.replaceFirst(".","").trim();
		}
		return data;
	}
	
	private static Nota parseNota(final XMLEventReader reader) throws XMLStreamException {
		String data = "";
		while(reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_NOTA)) {
				break;
			}
			if (event.isCharacters()) {
				Characters element = event.asCharacters();
				data += clean(element.getData());
			}
		}
		return new Nota(data);
	}
	
	private static Object parseTitre(final XMLEventReader reader, boolean section, boolean soussection1, boolean soussection2) throws XMLStreamException {
		String data = "";
		
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_TITRE)) {
				break;
			}
			if (event.isCharacters()) {
				Characters element = event.asCharacters();
				data += clean(element.getData());
			}
		}
		Object titre = new Section(data);
		if (soussection1) {
			titre = new SousSection1(data);
		}
		if (soussection2) {
			titre = new SousSection2(data);
		}
		
		return titre;
	}
	
	private static ResultatVote parseResultatVote(final XMLEventReader reader) throws XMLStreamException  {
		ResultatVote vote = new ResultatVote();
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			
			if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(Constants.ELEMENT_RESULTATVOTE)) {
				break;
			}
			if (event.isStartElement()) {
				final StartElement element = event.asStartElement();
				final String elementName = element.getName().getLocalPart();
				
				switch(elementName.toLowerCase()) {
					case Constants.ELEMENT_NOMBREVOTANT:
						vote.addValeur(parseLibelleValeur(reader));
						break;
					case Constants.ELEMENT_NOMBRESUFFRAGE:
						vote.addValeur(parseLibelleValeur(reader));
						break;
					case Constants.ELEMENT_MAJORITESUFFRAGE:
						vote.addValeur(parseLibelleValeur(reader));
						break;
					case Constants.ELEMENT_POUR:
						vote.addValeur(parseLibelleValeur(reader));
						break;
					case Constants.ELEMENT_CONTRE:
						vote.addValeur(parseLibelleValeur(reader));
						break;
				}
			}
		}
		return vote;
	}
	
	private static LibelleValeur parseLibelleValeur(XMLEventReader reader) throws XMLStreamException {
		LibelleValeur valeur = new LibelleValeur();
		while (reader.hasNext()) {
			final XMLEvent event = reader.nextEvent();
			
			if (event.isEndElement()) {
				final EndElement element = event.asEndElement();
				final String elementName = element.getName().getLocalPart();
				
				if (!elementName.equals(Constants.ELEMENT_LIBELLE) && !elementName.equalsIgnoreCase(Constants.ELEMENT_VALEUR)) {
					break;
				}
			}
			if (event.isStartElement()) {
				final StartElement element = event.asStartElement();
				final String elementName = element.getName().getLocalPart();
				
				switch(elementName.toLowerCase()) {
					case Constants.ELEMENT_LIBELLE:
						valeur.setLibelle(reader.getElementText().trim());
						break;
					case Constants.ELEMENT_VALEUR:
						valeur.setValeur(Integer.parseInt(reader.getElementText().trim()));
						break;
					
				}
			}
		}
		return valeur;
	}
	
	private static LocalDate parseDate(String date) {
		LocalDate theDate = LocalDate.MIN;
		try {
			if (date.contains("+")) {
				theDate = LocalDate.parse(date, DateTimeFormatter.ISO_OFFSET_DATE);
			} else {
				theDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
			}
		} catch (Exception e) {
			log.error("parseDate error {}, {}", date, e.getMessage());
		}
		return theDate;
	}
}
