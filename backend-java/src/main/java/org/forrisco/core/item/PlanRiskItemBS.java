package org.forrisco.core.item;

import javax.enterprise.context.RequestScoped;

import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import br.com.caelum.vraptor.boilerplate.HibernateBusiness;
import br.com.caelum.vraptor.boilerplate.bean.PaginatedList;

/**
 * @author Juliano Afonso
 */

@RequestScoped
public class PlanRiskItemBS extends HibernateBusiness {
		
		//listItensByPlanRisk
	
	/**
	 * Lista os itens de um plano de risco.
	 * @param PlanRisk
	 * 			plano de risco no qual se deseja obter os itens.
	 * 
	 * @return PaginatedList<PlanRiskItem>
	 * 			Lista de itens.
	 */
	public PaginatedList<PlanRiskItem> listItensByPlanRisk(PlanRisk planRisk) {
	
		PaginatedList<PlanRiskItem> results = new PaginatedList<PlanRiskItem>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItem.class).add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("planRisk", planRisk)).addOrder(Order.asc("name"));
		
		Criteria count = this.dao.newCriteria(PlanRiskItem.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk", planRisk)).setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskItem.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Lista os campos de um item.
	 * @param PlanRiskItemField
	 * 			item no qual se deseja obter os campos.
	 * 
	 * @return PaginatedList<PlanRiskItemField>
	 * 			Lista de campos.
	 */
	public PaginatedList<PlanRiskItemField> listItensByPlanRiskField(PlanRiskItem planRiskItem) {
		
		PaginatedList<PlanRiskItemField> results = new PaginatedList<PlanRiskItemField>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItemField.class).add(Restrictions.eq("deleted", false));
		
		Criteria count = this.dao.newCriteria(PlanRiskItem.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("item", planRiskItem)).setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskItemField.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
		
	}
	
	/**
	 * Salva no banco de dados um novo item
	 * 
	 * @param plaRiskItem instância do item a ser salvo
	 */
	public void save(PlanRiskItem plaRiskItem) {
		plaRiskItem.setId(null);
		plaRiskItem.setDeleted(false);
		
		this.persist(plaRiskItem);
	}
	
	/**
	 * Salva no banco de dados um novo campo
	 * 
	 * @param planRiskItemField, instância do PlanRiskItemField a ser salvo
	 */
	public void save(PlanRiskItemField planRiskItemField) {
		planRiskItemField.setId(null);
		planRiskItemField.setDeleted(false);
		this.persist(planRiskItemField);
	}

}