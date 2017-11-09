package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.term.impl.DatatypePredicateImpl;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static it.unibz.inf.ontop.model.type.impl.AbstractNumericRDFDatatype.createAbstractNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.ConcreteNumericRDFDatatypeImpl.createTopConcreteNumericTermType;
import static it.unibz.inf.ontop.model.type.impl.SimpleRDFDatatype.createSimpleRDFDatatype;
import static it.unibz.inf.ontop.model.vocabulary.RDF.LANGSTRING;

@Singleton
public class TypeFactoryImpl implements TypeFactory {

	private final DatatypePredicate XSD_STRING;
	private final DatatypePredicate XSD_INTEGER, XSD_NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER;
	private final DatatypePredicate XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER;
	private final DatatypePredicate XSD_INT, XSD_UNSIGNED_INT, XSD_LONG;
	private final DatatypePredicate XSD_DECIMAL;
	private final DatatypePredicate XSD_DOUBLE, XSD_FLOAT;
	private final DatatypePredicate XSD_DATETIME, XSD_DATETIME_STAMP;
	private final DatatypePredicate XSD_BOOLEAN, XSD_BASE64;
	private final DatatypePredicate XSD_DATE, XSD_TIME, XSD_YEAR;

	private final Map<TermType, DatatypePredicate> mapTypetoPredicate = new HashMap<>();
	private final List<Predicate> predicateList = new LinkedList<>();

	// Only builds these TermTypes once.
	private final Map<IRI, RDFDatatype> datatypeCache = new ConcurrentHashMap<>();
	private final Map<String, RDFDatatype> langTypeCache = new ConcurrentHashMap<>();

	private final TermType rootTermType;
	private final RDFTermType rootRDFTermType;
	private final UnboundRDFTermType unboundRDFTermType;
	private final ObjectRDFType objectRDFType, iriTermType, blankNodeTermType;
	private final RDFDatatype rdfsLiteralDatatype;
	private final NumericRDFDatatype numericDatatype, owlRealDatatype;
	private final ConcreteNumericRDFDatatype owlRationalDatatype, xsdDecimalDatatype;
	private final ConcreteNumericRDFDatatype xsdDoubleDatatype, xsdFloatDatatype;
	private final ConcreteNumericRDFDatatype xsdIntegerDatatype, xsdLongDatatype, xsdIntDatatype, xsdShortDatatype, xsdByteDatatype;
	private final ConcreteNumericRDFDatatype xsdNonPositiveIntegerDatatype, xsdNegativeIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdNonNegativeIntegerDatatype, xsdPositiveIntegerDatatype;
	private final ConcreteNumericRDFDatatype xsdUnsignedLongDatatype, xsdUnsignedIntDatatype, xsdUnsignedShortDatatype, xsdUnsignedByteDatatype;
	private final RDFDatatype defaultUnsupportedDatatype, xsdStringDatatype, xsdBooleanDatatype, xsdBase64Datatype;
	private final RDFDatatype xsdTimeDatatype, xsdDateDatatype, xsdDatetimeDatatype, xsdDatetimeStampDatatype, xsdGYearDatatype;
	private final RDF rdfFactory;

	@Inject
	private TypeFactoryImpl() {
		rdfFactory = new SimpleRDF();

		rootTermType = TermTypeImpl.createOriginTermType();
		rootRDFTermType = RDFTermTypeImpl.createRDFTermRoot(rootTermType.getAncestry());

		unboundRDFTermType = UnboundRDFTermTypeImpl.createUnboundRDFTermType(rootRDFTermType.getAncestry());

		objectRDFType = AbstractObjectRDFType.createAbstractObjectRDFType(rootRDFTermType.getAncestry());
		iriTermType = new IRITermType(objectRDFType.getAncestry());
		blankNodeTermType = new BlankNodeTermType(objectRDFType.getAncestry());

		rdfsLiteralDatatype = createSimpleRDFDatatype(RDFS.LITERAL, rootRDFTermType.getAncestry(), true);
		registerDatatype(rdfsLiteralDatatype);

		numericDatatype = createAbstractNumericTermType(OntopInternal.NUMERIC, rdfsLiteralDatatype.getAncestry());
		registerDatatype(numericDatatype);

		xsdDoubleDatatype = createTopConcreteNumericTermType(XSD.DOUBLE, numericDatatype);
		registerDatatype(xsdDoubleDatatype);

		// Type promotion: an xsd:float can be promoted into a xsd:double
		xsdFloatDatatype = createConcreteNumericTermType(XSD.FLOAT, numericDatatype.getAncestry(),
				xsdDoubleDatatype.getPromotionSubstitutionHierarchy(),true);
		registerDatatype(xsdFloatDatatype);

		owlRealDatatype = createAbstractNumericTermType(OWL.REAL, numericDatatype.getAncestry());
		registerDatatype(owlRealDatatype);
		// Type promotion: an owl:rational can be promoted into a xsd:float
		owlRationalDatatype = createConcreteNumericTermType(OWL.RATIONAL, owlRealDatatype.getAncestry(),
				xsdFloatDatatype.getPromotionSubstitutionHierarchy(), true);
		registerDatatype(owlRationalDatatype);
		xsdDecimalDatatype = createConcreteNumericTermType(XSD.DECIMAL, owlRationalDatatype, true);
		registerDatatype(xsdDecimalDatatype);
		xsdIntegerDatatype = createConcreteNumericTermType(XSD.INTEGER, xsdDecimalDatatype, true);
		registerDatatype(xsdIntegerDatatype);

		xsdNonPositiveIntegerDatatype = createConcreteNumericTermType(XSD.NON_POSITIVE_INTEGER,
				xsdIntegerDatatype, false);
		registerDatatype(xsdNonPositiveIntegerDatatype);
		xsdNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NEGATIVE_INTEGER,
				xsdNonPositiveIntegerDatatype, false);
		registerDatatype(xsdNegativeIntegerDatatype);

		xsdLongDatatype = createConcreteNumericTermType(XSD.LONG, xsdIntegerDatatype,false);
		registerDatatype(xsdLongDatatype);
		xsdIntDatatype = createConcreteNumericTermType(XSD.INT, xsdLongDatatype,false);
		registerDatatype(xsdIntDatatype);
		xsdShortDatatype = createConcreteNumericTermType(XSD.SHORT, xsdIntDatatype, false);
		registerDatatype(xsdShortDatatype);
		xsdByteDatatype = createConcreteNumericTermType(XSD.BYTE, xsdShortDatatype, false);
		registerDatatype(xsdByteDatatype);

		xsdNonNegativeIntegerDatatype = createConcreteNumericTermType(XSD.NON_NEGATIVE_INTEGER,
				xsdIntegerDatatype,false);
		registerDatatype(xsdNonNegativeIntegerDatatype);

		xsdUnsignedLongDatatype = createConcreteNumericTermType(XSD.UNSIGNED_LONG, xsdIntegerDatatype, false);
		registerDatatype(xsdUnsignedLongDatatype);
		xsdUnsignedIntDatatype = createConcreteNumericTermType(XSD.UNSIGNED_INT, xsdUnsignedLongDatatype,false);
		registerDatatype(xsdUnsignedIntDatatype);

		xsdUnsignedShortDatatype = createConcreteNumericTermType(XSD.UNSIGNED_SHORT, xsdUnsignedIntDatatype, false);
		registerDatatype(xsdUnsignedShortDatatype);
		xsdUnsignedByteDatatype = createConcreteNumericTermType(XSD.UNSIGNED_BYTE, xsdUnsignedShortDatatype, false);
		registerDatatype(xsdUnsignedByteDatatype);

		xsdPositiveIntegerDatatype = createConcreteNumericTermType(XSD.POSITIVE_INTEGER,
				xsdNonNegativeIntegerDatatype,false);
		registerDatatype(xsdPositiveIntegerDatatype);

		xsdBooleanDatatype = createSimpleRDFDatatype(XSD.BOOLEAN, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdBooleanDatatype);

		xsdStringDatatype = createSimpleRDFDatatype(XSD.STRING, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdStringDatatype);

		defaultUnsupportedDatatype = UnsupportedRDFDatatype.createUnsupportedDatatype(rdfsLiteralDatatype.getAncestry());

		xsdTimeDatatype = createSimpleRDFDatatype(XSD.TIME, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdTimeDatatype);
		xsdDateDatatype = createSimpleRDFDatatype(XSD.DATE, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdDateDatatype);
		xsdDatetimeDatatype = createSimpleRDFDatatype(XSD.DATETIME, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdDatetimeDatatype);
		xsdDatetimeStampDatatype = createSimpleRDFDatatype(XSD.DATETIMESTAMP, xsdDatetimeDatatype.getAncestry());
		registerDatatype(xsdDatetimeStampDatatype);
		xsdGYearDatatype = createSimpleRDFDatatype(XSD.GYEAR, rdfsLiteralDatatype.getAncestry());
		registerDatatype(xsdGYearDatatype);

		xsdBase64Datatype = createSimpleRDFDatatype(XSD.BASE64BINARY, rdfsLiteralDatatype.getAncestry(), false);
		registerDatatype(xsdBase64Datatype);

		XSD_INTEGER = registerType(xsdIntegerDatatype);  //  4 "http://www.w3.org/2001/XMLSchema#integer";
		XSD_DECIMAL = registerType( xsdDecimalDatatype);  // 5 "http://www.w3.org/2001/XMLSchema#decimal"
		XSD_DOUBLE = registerType(xsdDoubleDatatype);  // 6 "http://www.w3.org/2001/XMLSchema#double"
		XSD_STRING = registerType(xsdStringDatatype);  // 7 "http://www.w3.org/2001/XMLSchema#string"
		XSD_DATETIME = registerType(xsdDatetimeDatatype); // 8 "http://www.w3.org/2001/XMLSchema#dateTime"
		XSD_DATETIME_STAMP = registerType(xsdDatetimeStampDatatype);
		XSD_BOOLEAN = registerType(xsdBooleanDatatype);  // 9 "http://www.w3.org/2001/XMLSchema#boolean"
		XSD_DATE = registerType(xsdDateDatatype);  // 10 "http://www.w3.org/2001/XMLSchema#date";
		XSD_TIME = registerType(xsdTimeDatatype);  // 11 "http://www.w3.org/2001/XMLSchema#time";
		XSD_YEAR = registerType(xsdGYearDatatype);  // 12 "http://www.w3.org/2001/XMLSchema#gYear";
		XSD_LONG = registerType(xsdLongDatatype);  // 13 "http://www.w3.org/2001/XMLSchema#long"
		XSD_FLOAT = registerType(xsdFloatDatatype); // 14 "http://www.w3.org/2001/XMLSchema#float"
		XSD_NEGATIVE_INTEGER = registerType(xsdNegativeIntegerDatatype); // 15 "http://www.w3.org/2001/XMLSchema#negativeInteger";
		XSD_NON_NEGATIVE_INTEGER = registerType(xsdNonNegativeIntegerDatatype); // 16 "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
		XSD_POSITIVE_INTEGER = registerType(xsdPositiveIntegerDatatype); // 17 "http://www.w3.org/2001/XMLSchema#positiveInteger"
		XSD_NON_POSITIVE_INTEGER = registerType(xsdNonPositiveIntegerDatatype); // 18 "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
		XSD_INT = registerType(xsdIntDatatype);  // 19 "http://www.w3.org/2001/XMLSchema#int"
		XSD_UNSIGNED_INT = registerType(xsdUnsignedIntDatatype);   // 20 "http://www.w3.org/2001/XMLSchema#unsignedInt"

		XSD_BASE64 = registerType(xsdBase64Datatype);
	}

	private void registerDatatype(RDFDatatype datatype) {
		datatypeCache.put(datatype.getIRI(), datatype);
	}

	private DatatypePredicate registerType(RDFDatatype type) {
		DatatypePredicate predicate = new DatatypePredicateImpl(type, type);
		mapTypetoPredicate.put(type, predicate);
		predicateList.add(predicate);
		return predicate;
	}

	@Override
	public Optional<RDFDatatype> getOptionalDatatype(String uri) {
		return getOptionalDatatype(rdfFactory.createIRI(uri));
	}

	@Override
	public Optional<RDFDatatype> getOptionalDatatype(IRI iri) {
		return Optional.ofNullable(datatypeCache.get(iri));
	}

	@Override
	public ImmutableList<Predicate> getDatatypePredicates() {
		return ImmutableList.copyOf(predicateList);
	}

	@Override
	public DatatypePredicate getRequiredTypePredicate(TermType type) {
		return getOptionalTypePredicate(type)
				.orElseThrow(() -> new NoConstructorFunctionException(type));
	}

	@Override
	public DatatypePredicate getRequiredTypePredicate(IRI datatypeIri) {
		if (datatypeIri.equals(LANGSTRING))
			throw new IllegalArgumentException("Lang string predicates are not unique (they depend on the language tag)");
		return getRequiredTypePredicate(getDatatype(datatypeIri));
	}

	@Override
	public Optional<DatatypePredicate> getOptionalTypePredicate(TermType type) {
		return Optional.ofNullable(mapTypetoPredicate.get(type));
	}

	@Override
	public URITemplatePredicate getURITemplatePredicate(int arity) {
		return new URITemplatePredicateImpl(arity, this);
	}

	@Override
	public RDFDatatype getLangTermType(String languageTagString) {
		return langTypeCache
				.computeIfAbsent(languageTagString.toLowerCase(), this::createLangStringDatatype);
	}

	@Override
	public RDFDatatype getDatatype(IRI iri) {
		RDFDatatype datatype = datatypeCache.get	(iri);
		if (datatype == null)
			throw new RuntimeException("TODO: support arbitrary datatypes such as " + iri);
		return datatype;
	}

	@Override
	public ObjectRDFType getIRITermType() {
		return iriTermType;
	}

	@Override
	public ObjectRDFType getBlankNodeType() {
		return blankNodeTermType;
	}

	@Override
	public UnboundRDFTermType getUnboundTermType() {
		return unboundRDFTermType;
	}

	@Override
	public RDFDatatype getUnsupportedDatatype() {
		return defaultUnsupportedDatatype;
	}

	@Override
	public RDFDatatype getAbstractOntopNumericDatatype() {
		return numericDatatype;
	}

	@Override
	public RDFDatatype getAbstractRDFSLiteral() {
		return rdfsLiteralDatatype;
	}

	@Override
	public TermType getAbstractAtomicTermType() {
		return rootTermType;
	}

	@Override
	public RDFTermType getAbstractRDFTermType() {
		return rootRDFTermType;
	}

    @Override
    public ObjectRDFType getAbstractObjectRDFType() {
		return objectRDFType;
    }

    private RDFDatatype createLangStringDatatype(String languageTagString) {
		RDFDatatype datatype = LangDatatype.createLangDatatype(
				new LanguageTagImpl(languageTagString), xsdStringDatatype.getAncestry(), this);

		// Creates a predicate
		mapTypetoPredicate.put(datatype, new DatatypePredicateImpl(datatype, datatype));

		return datatype;
	}


    private static class NoConstructorFunctionException extends OntopInternalBugException {

		private NoConstructorFunctionException(TermType type) {
			super("No construction function found for " + type);
		}
	}
}
