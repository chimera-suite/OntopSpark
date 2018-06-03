package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.TermType;

public class RDFTermFunctionSymbolImpl extends FunctionSymbolImpl implements RDFTermFunctionSymbol {

    protected RDFTermFunctionSymbolImpl(TermType lexicalType, MetaRDFTermType typeTermType) {
        super("RDF", 2, ImmutableList.of(lexicalType, typeTermType));
    }

    /**
     * TODO: implement seriously
     */
    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability) {
        return super.evaluateNullability(arguments, childNullability);
    }
}