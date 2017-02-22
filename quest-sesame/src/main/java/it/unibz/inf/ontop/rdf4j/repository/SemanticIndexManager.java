package it.unibz.inf.ontop.rdf4j.repository;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.rdf4j.RDF4JRDFIterator;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/***
 * An utility to setup and maintain a semantic index repository independently
 * from a Quest instance.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexManager {

	private final Connection conn;

	private final TBoxReasoner reasoner;
	private final ImmutableOntologyVocabulary voc;

	private final RDBMSSIRepositoryManager dataRepository;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexManager(OWLOntology tbox, Connection connection,
								NativeQueryLanguageComponentFactory nativeQLFactory) throws Exception {
		conn = connection;
		Ontology ontologyClosure = QuestOWL.loadOntologies(tbox);
		voc = ontologyClosure.getVocabulary();

		reasoner = TBoxReasonerImpl.create(ontologyClosure, true);
			
		dataRepository = new RDBMSSIRepositoryManager(reasoner, ontologyClosure.getVocabulary(), nativeQLFactory);
		dataRepository.generateMetadata(); // generate just in case

		log.debug("TBox has been processed. Ready to ");
	}

	public void restoreRepository() throws SQLException {
		dataRepository.loadMetadata(conn);

		log.debug("Semantic Index metadata was found and restored from the DB");
	}

	public void setupRepository(boolean drop) throws SQLException {

		if (drop) {
			log.debug("Droping existing tables");
			try {
				dataRepository.dropDBSchema(conn);
			}
			catch (SQLException e) {
				log.debug(e.getMessage(), e);
			}
		}

		dataRepository.createDBSchemaAndInsertMetadata(conn);

		log.debug("Semantic Index repository has been setup.");
	}
	
	public void dropRepository() throws SQLException {
		dataRepository.dropDBSchema(conn);
	}

	public void updateMetadata() throws SQLException {
		dataRepository.insertMetadata(conn);
		log.debug("Updated metadata in the repository");
	}

	public int insertData(OWLOntology ontology, int commitInterval, int batchSize) throws SQLException {

		OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontology.getOWLOntologyManager().getImportsClosure(ontology), voc);
		int result = dataRepository.insertData(conn, aBoxIter, commitInterval, batchSize);

		log.info("Loaded {} items into the DB.", result);

		return result;
	}

	public int insertDataNTriple(final String ntripleFile, final String baseURI, final int commitInterval, final int batchSize)
			throws SQLException, RDFParseException, RDFHandlerException, FileNotFoundException, IOException {

		final TurtleParser parser = new TurtleParser();
		// NTriplesParser parser = new NTriplesParser();

		final RDF4JRDFIterator aBoxIter = new RDF4JRDFIterator();
		
		parser.setRDFHandler(aBoxIter);


		Thread t = new Thread(() -> {

            try {
                parser.parse(new BufferedReader(new FileReader(ntripleFile)), baseURI);
            } catch (RDFParseException | RDFHandlerException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

		final int[] val = new int[1];

		Thread t2 = new Thread(() -> {
            try {
                val[0] = dataRepository.insertData(conn, aBoxIter, commitInterval, batchSize);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            log.info("Loaded {} items into the DB.", val[0]);

        });

		t.start();
		t2.start();
		try {
			t.join();

			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// EquivalentTriplePredicateIterator newData = new
		// EquivalentTriplePredicateIterator(aBoxIter, equivalenceMaps);

		return val[0];
	}

}