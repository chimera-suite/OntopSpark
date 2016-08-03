package it.unibz.inf.ontop.owlrefplatform.core.translator;

/*
 * #%L
 * ontop-reformulation-core
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


import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.LoggerFactory;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.convertToMutableFunction;

/***
 * Translate a intermediate queries expression into a Datalog program that has the
 * same semantics. We use the built-int predicates Join and Left join. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
 * 
 * 
 * @author mrezk
 */
public class IntermediateQueryToDatalogTranslator {


	private static class RuleHead {
		public final ConstructionNode constructionNode;
		public final DataAtom atom;

		private RuleHead(ConstructionNode constructionNode, DataAtom headAtom) {
			this.constructionNode = constructionNode;
			this.atom = headAtom;
		}
	}

	
	private final static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	//private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(IntermediateQueryToDatalogTranslator.class);

	// Incremented
	private int subQueryCounter;

	private IntermediateQueryToDatalogTranslator() {
		subQueryCounter = 0;
	}

	/**
	 * Translate an intermediate query tree into a Datalog program 
	 * 
	 */
	public static DatalogProgram translate(IntermediateQuery query) {
		IntermediateQueryToDatalogTranslator translator = new IntermediateQueryToDatalogTranslator();
		return translator.translateQuery(query);
	}

	private DatalogProgram translateQuery(IntermediateQuery query) {
		ConstructionNode root = query.getRootConstructionNode();
		
		Optional<ImmutableQueryModifiers> optionalModifiers =  root.getOptionalModifiers();

        DatalogProgram dProgram;
		if (optionalModifiers.isPresent()){
			QueryModifiers immutableQueryModifiers = optionalModifiers.get();

			// Mutable modifiers (used by the Datalog)
			OBDAQueryModifiers mutableModifiers = new MutableQueryModifiersImpl(immutableQueryModifiers);
			// TODO: support GROUP BY (distinct QueryNode)

            dProgram = ofac.getDatalogProgram(mutableModifiers);
		}
        else {
            dProgram = ofac.getDatalogProgram();
        }

		translate(query,  dProgram, root);
		
	
		
		
		return dProgram;
	}
	
	/**
	 * Translate a given IntermediateQuery query object to datalog program.
	 * 
	 *           
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
	private void translate(IntermediateQuery query, DatalogProgram pr, ConstructionNode root) {

		Queue<RuleHead> heads = new LinkedList<>();
		heads.add(new RuleHead(root, query.getProjectionAtom()));

		// Mutable (append-only)
		Map<ConstructionNode, DataAtom> subQueryProjectionAtoms = new HashMap<>();
		subQueryProjectionAtoms.put(root, query.getProjectionAtom());

		//In heads we keep the heads of the sub-rules in the program, e.g. ans5() :- LeftJoin(....)
		while(!heads.isEmpty()) {

			RuleHead head = heads.poll();

			//Applying substitutions in the head.
			ImmutableFunctionalTerm substitutedHeadAtom = head.constructionNode.getSubstitution().applyToFunctionalTerm(
					head.atom);

			List<Function> atoms = new LinkedList<>();

			//Constructing the rule
			CQIE newrule = ofac.getCQIE(convertToMutableFunction(substitutedHeadAtom), atoms);

			pr.appendRule(newrule);

			//Iterating over the nodes and constructing the rule
			for (QueryNode node : query.getChildren(head.constructionNode)) {

				List<Function> uAtoms = getAtomFrom(query, node, heads, subQueryProjectionAtoms);
				newrule.getBody().addAll(uAtoms);

			} //end-for
		}
	}

	

	/**
	 * This is the MAIN recursive method in this class!!
	 * Takes a node and return the list of functions (atoms) that it represents.
	 * Usually it will be a single atom, but it is different for the filter case.
	 */
	private List<Function> getAtomFrom(IntermediateQuery te, QueryNode node, Queue<RuleHead> heads,
											  Map<ConstructionNode, DataAtom> subQueryProjectionAtoms) {
		
		List<Function> body = new ArrayList<>();
		
		/**
		 * Basic Atoms
		 */
		
		if (node instanceof ConstructionNode) {
			ConstructionNode constructionNode = (ConstructionNode) node;
			DataAtom projectionAtom = Optional.ofNullable(
					subQueryProjectionAtoms.get(constructionNode))
					.map(atom -> adaptProjectionAtom(atom, constructionNode))
					.orElseGet(() -> generateProjectionAtom(constructionNode.getVariables()));

			heads.add(new RuleHead(constructionNode, projectionAtom));
			subQueryProjectionAtoms.put(constructionNode, projectionAtom);
			Function mutAt = convertToMutableFunction(projectionAtom);
			body.add(mutAt);
			return body;
			
		} else if (node instanceof FilterNode) {
			ImmutableExpression filter = ((FilterNode) node).getFilterCondition();
			Expression mutFilter =  ImmutabilityTools.convertToMutableBooleanExpression(filter);
			List<QueryNode> listnode =  te.getChildren(node);
			body.addAll(getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms));
			body.add(mutFilter);
			return body;
			
					
		} else if (node instanceof DataNode) {
			DataAtom atom = ((DataNode)node).getProjectionAtom();
			Function mutAt = convertToMutableFunction(atom);
			body.add(mutAt);
			return body;
				
			
			
		/**
		 * Nested Atoms	
		 */
		} else  if (node instanceof InnerJoinNode) {
			Optional<ImmutableExpression> filter = ((InnerJoinNode)node).getOptionalFilterCondition();
			List<Function> atoms = new ArrayList<>();
			List<QueryNode> listnode =  te.getChildren(node);
			for (QueryNode childnode: listnode) {
				List<Function> atomsList = getAtomFrom(te, childnode, heads, subQueryProjectionAtoms);
				atoms.addAll(atomsList);
			}

			if (atoms.size() <= 1) {
				throw new IllegalArgumentException("Inconsistent IQ: an InnerJoinNode must have at least two children");
			}

			if (filter.isPresent()){
				ImmutableExpression filter2 = filter.get();
				Function mutFilter = ImmutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newJ = getSPARQLJoin(atoms, Optional.of(mutFilter));
				body.add(newJ);
				return body;
			}else{
				Function newJ = getSPARQLJoin(atoms, Optional.empty());
				body.add(newJ);
				return body;
			}
			
		} else if (node instanceof LeftJoinNode) {
			Optional<ImmutableExpression> filter = ((LeftJoinNode)node).getOptionalFilterCondition();
			List<QueryNode> listnode =  te.getChildren(node);

			List<Function> atomsListLeft = getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms);
			List<Function> atomsListRight = getAtomFrom(te, listnode.get(1), heads, subQueryProjectionAtoms);
				
			if (filter.isPresent()){
				ImmutableExpression filter2 = filter.get();
				Expression mutFilter =  ImmutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newLJAtom = ofac.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.of(mutFilter));
				body.add(newLJAtom);
				return body;
			}else{
				Function newLJAtom = ofac.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.empty());
				body.add(newLJAtom);
				return body;
			}

		} else if (node instanceof UnionNode) {

			ConstructionNode parentNode = te.getParent(node)
					.filter(p -> p instanceof ConstructionNode)
					.map(p -> (ConstructionNode) p)
					.orElseThrow(() -> new IllegalStateException("Invalid IQ: " +
							"a UNION node must have a ConstructionNode as parent"));

			DistinctVariableOnlyDataAtom childIdealProjectionAtom = generateProjectionAtom(
					parentNode.getVariables());
			
			for (QueryNode child : te.getChildren(node)) {
				ConstructionNode childConstructionNode =(ConstructionNode) child;
				DataAtom childAtom = adaptProjectionAtom(childIdealProjectionAtom, childConstructionNode);
				subQueryProjectionAtoms.put(childConstructionNode, childAtom);
				heads.add(new RuleHead(childConstructionNode, childAtom));
		
			} //end for

			Function bodyAtom = convertToMutableFunction(childIdealProjectionAtom);
			body.add(bodyAtom);
			return body;
			

						
		} else {
			 throw new UnsupportedOperationException("Type od node in the intermediate tree is unknown!!");
		}
	
	}

	private DataAtom adaptProjectionAtom(DataAtom idealProjectionAtom, ConstructionNode constructionNode) {
		ImmutableList<? extends VariableOrGroundTerm> arguments = idealProjectionAtom.getArguments();
		ImmutableSet<Variable> projectedVariables = constructionNode.getVariables();

		if (ImmutableSet.copyOf(arguments).equals(projectedVariables)) {
			return idealProjectionAtom;
		}
		else {
			ImmutableList<VariableOrGroundTerm> newArguments = arguments.stream()
					.map(arg -> projectedVariables.contains(arg)
							? arg
							: OBDAVocabulary.NULL)
					.collect(ImmutableCollectors.toList());

			return ofac.getDataAtom(idealProjectionAtom.getPredicate(), newArguments);
		}
	}

	private DistinctVariableOnlyDataAtom generateProjectionAtom(ImmutableSet<Variable> projectedVariables) {
		AtomPredicate newPredicate = new AtomPredicateImpl("ansSQ" + ++subQueryCounter, projectedVariables.size());
		return ofac.getDistinctVariableOnlyDataAtom(newPredicate, ImmutableList.copyOf(projectedVariables));
	}

	private static Function getSPARQLJoin(List<Function> atoms, Optional<Function> optionalCondition) {
		int atomCount = atoms.size();
		Function rightTerm;

		switch (atomCount) {
			case 0:
			case 1:
				throw new IllegalArgumentException("A join requires at least two atoms");
			case 2:
				rightTerm = atoms.get(1);
				break;
			default:
				rightTerm = getSPARQLJoin(atoms.subList(1, atomCount), Optional.empty());
				break;
		}

		return optionalCondition.isPresent()
				? ofac.getSPARQLJoin(atoms.get(0), rightTerm, optionalCondition.get())
				: ofac.getSPARQLJoin(atoms.get(0), rightTerm);
	}

}
