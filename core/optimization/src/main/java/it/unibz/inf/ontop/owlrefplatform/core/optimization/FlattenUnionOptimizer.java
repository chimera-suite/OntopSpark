package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.FlattenUnionProposal;
import it.unibz.inf.ontop.iq.proposal.impl.FlattenUnionProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Flattens UnionNodes.
 * <p>
 * The algorithm searches the query q depth-first.
 * If a UnionNode u1 is encountered,
 * it proceeds as follows:
 * .Retrieve the set U composed of u1,
 * all children UnionNodes of u1,
 * all children UnionNodes of these,
 * etc. (recursively).
 * .Retrieve the set C of all child subtrees of some node in U.
 * .Within C, retain the set C' of subtrees whose root is not a UnionNode.
 * .Build the subtree s, whose root is a union node projecting the same variables as u1,
 * and such that the children subtrees of s are the subtrees in C'.
 * .In q, replace the subtree rooted in u1 by s.
 * .Continue the depth-first search
 * <p>
 * Assumption: the input query is well-formed wrt projections.
 */
public class FlattenUnionOptimizer extends NodeCentricDepthFirstOptimizer<FlattenUnionProposal> {


    public FlattenUnionOptimizer() {
        super(false);
    }

    @Override
    protected Optional<FlattenUnionProposal> evaluateNode(QueryNode node, IntermediateQuery query) {
        if (node instanceof UnionNode) {
            return evaluateUnionNode((UnionNode) node, query);
        }
        return Optional.empty();
    }

    private Optional<FlattenUnionProposal> evaluateUnionNode(UnionNode node, IntermediateQuery query) {
        ImmutableList<UnionNode> unionNodesToMerge = getUnionCluster(node, query)
                .collect(ImmutableCollectors.toList());
        return unionNodesToMerge.size() > 1 ?
                Optional.of(makeFlattenProposal(node, unionNodesToMerge, query)) :
                Optional.empty();
    }

    private Stream<UnionNode> getUnionCluster(UnionNode focusNode, IntermediateQuery query) {
        return Stream.concat(
                Stream.of(focusNode),
                query.getChildren(focusNode).stream()
                        .filter(n -> n instanceof UnionNode)
                        .flatMap(n -> getUnionCluster((UnionNode) n, query)
                        ));
    }

    private FlattenUnionProposal makeFlattenProposal(UnionNode focusNode, ImmutableList<UnionNode> unionNodesToMerge, IntermediateQuery query) {
        return new FlattenUnionProposalImpl(
                focusNode,
                unionNodesToMerge.stream()
                        .flatMap(n -> query.getChildren(n).stream())
                        .filter(n -> !(n instanceof UnionNode))
                        .collect(ImmutableCollectors.toSet())
        );
    }
}
