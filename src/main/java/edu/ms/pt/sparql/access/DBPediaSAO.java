package edu.ms.pt.sparql.access;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class DBPediaSAO {

	/**
	 * @param searchKeyword
	 * @param uriInfo 
	 */
	public CompanyList searchCompanyInfo(String searchKeyword, UriInfo uriInfo) {	
		Triple triple = Triple.create(Var.alloc("organization"), NodeFactory.createURI("http://xmlns.com/foaf/0.1/name") , Var.alloc("name"));
		Expr e = new E_Regex(new ExprVar("name"), new NodeValueString("^" + searchKeyword + "*"), new NodeValueString("i"));
		
		ElementTriplesBlock tripleBlock = new ElementTriplesBlock();
		tripleBlock.addTriple(triple);
		ElementFilter filter = new ElementFilter(e);
		
		ElementGroup queryBody = new ElementGroup();
		queryBody.addElement(tripleBlock);
		queryBody.addElementFilter(filter);
		
		
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar("organization");
		query.addResultVar("name");
		query.setQueryPattern(queryBody);
		query.setLimit(5);
		
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService("" +
				"http://dbpedia.org/sparql", query);
		ResultSet resultSet = queryExecution.execSelect();
		
		
		CompanyList companies = new CompanyList();
		List<Company> companyList = new ArrayList<Company>();
		companies.setCompanies(companyList);
		
		while(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			RDFNode organizationObject = result.get("organization");
			RDFNode name = result.get("name");
			
			Company company = new Company();
			company.setCompanyName(name.toString());
			company.setUrl(organizationObject.toString().replace("http://dbpedia.org/resource/", "http://" + uriInfo.getRequestUri().getHost()+":"+ uriInfo.getRequestUri().getPort() + "/SmartWikiSearch/company/"));
			companyList.add(company);
		}	
		return companies;
	}	
	
	public Company getCompanyInfo(String companyName, UriInfo uriInfo) {
		Triple triple = Triple.create(NodeFactory.createURI("http://dbpedia.org/resource/" + companyName), Var.alloc("property"), Var.alloc("value"));
		
		ElementTriplesBlock tripleBlock = new ElementTriplesBlock();
		tripleBlock.addTriple(triple);
		
		ElementGroup queryBody = new ElementGroup();
		queryBody.addElement(tripleBlock);
		
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar("property");
		query.addResultVar("value");
		query.setQueryPattern(queryBody);
		//query.setLimit(5);
		
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		ResultSet resultSet = queryExecution.execSelect();
		Company company = new Company();
		
		while(resultSet.hasNext()) {
			QuerySolution result = resultSet.next();
			RDFNode property = result.get("property");
			RDFNode value = result.get("value");
			
			if (property.toString().equals("http://xmlns.com/foaf/0.1/name"))
				company.setCompanyName(value.toString());
			if (property.toString().equals("http://dbpedia.org/ontology/abstract"))
				company.setDescription(value.toString());
			if (property.toString().equals("http://xmlns.com/foaf/0.1/isPrimaryTopicOf"))
				company.setDataSourceUrl(value.toString());
			company.setUrl(uriInfo.getBaseUri().toString() + companyName);
		}	
		return company;
	}
	
}