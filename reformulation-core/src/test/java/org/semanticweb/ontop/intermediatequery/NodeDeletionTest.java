package org.semanticweb.ontop.intermediatequery;

import com.google.common.base.Optional;
import org.junit.Test;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.TableNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.LEFT;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.RIGHT;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    @Test(expected = EmptyQueryException.class)
    public void testSimpleJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 1), x));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder();
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(rootNode, joinNode);

        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table1", 1), x));
        queryBuilder.addChild(joinNode, table1);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 1), x));
        queryBuilder.addChild(joinNode, table2);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }

    @Test
    public void testInvalidRightPartOfLeftJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(
                new AtomPredicateImpl("ans1", 2), x, y));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder();
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.<ImmutableBooleanExpression>absent());
        queryBuilder.addChild(rootNode, ljNode);

        String table1Name = "table1";
        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl(table1Name, 1), x));
        queryBuilder.addChild(ljNode, table1, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 2), x, y));
        queryBuilder.addChild(joinNode, table2);

        TableNode table3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table3", 2), x, y));
        queryBuilder.addChild(joinNode, table3);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.err.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof TableNode);
        assertEquals(((TableNode) viceRootNode).getAtom().getPredicate().getName(), table1Name);
        assertTrue(optimizedQuery.getCurrentSubNodesOf(viceRootNode).isEmpty());
    }
}