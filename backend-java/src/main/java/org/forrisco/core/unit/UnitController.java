package org.forrisco.core.unit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.abstractions.AbstractController;
import org.forpdi.core.user.User;
import org.forpdi.core.user.authz.Permissioned;
import org.forpdi.system.PDFgenerate;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;

import com.itextpdf.text.DocumentException;

import br.com.caelum.vraptor.Consumes;
import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Delete;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Put;
import br.com.caelum.vraptor.boilerplate.NoCache;
import br.com.caelum.vraptor.boilerplate.bean.PaginatedList;
import br.com.caelum.vraptor.boilerplate.util.GeneralUtils;

/**
 * @author Matheus Nascimento
 */
@Controller
public class UnitController extends AbstractController {

	@Inject
	private UnitBS unitBS;
	@Inject
	private RiskBS riskBS;
	@Inject 
	private ProcessBS processBS;
	@Inject
	private PDFgenerate pdf;

	protected static final String PATH = BASEPATH + "/unit";

	/**
	 * Salvar unidade
	 * 
	 * @param Unit
	 *            unidade a ser salva
	 */
	@Post(PATH + "/new")
	@Consumes
	@NoCache
	// @Permissioned(value = AccessLevels.COMPANY_ADMIN, permissions = {
	// ManagePolicyPermission.class })
	public void save(@NotNull @Valid Unit unit) {
		try {
			PlanRisk planRisk = this.unitBS.exists(unit.getPlan().getId(), PlanRisk.class);
			if (planRisk == null) {
				this.fail("Unidade não possui Plano de Risco");
			}
			unit.setId(null);
			unit.setPlan(planRisk);
			this.unitBS.save(unit);
			this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Salvar subunidade
	 * 
	 * @param Unit
	 *            subunidade a ser salva
	 */
	@Post(PATH + "/subnew")
	@Consumes
	@NoCache
	// @Permissioned(value = AccessLevels.COMPANY_ADMIN, permissions = {
	// ManagePolicyPermission.class })
	public void saveSub(@NotNull @Valid Unit unit) {
		try {

			if (unit.getPlan() == null) {
				this.fail("Unidade não possui Plano de Risco");
				return;
			}

			if (unit.getParent() == null) {
				this.fail("Unidade não possui unidade pai");
				return;
			}

			unit.setId(null);
			this.unitBS.save(unit);
			this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna unidades.
	 * 
	 * @param PLanRisk
	 *            Id do plano de risco.
	 * @return <PaginatedList> Unit
	 */
	@Get(PATH + "")
	@NoCache
	@Consumes
	public void listUnits(@NotNull Long planId) {
		try {
			PlanRisk plan = this.unitBS.exists(planId, PlanRisk.class);
			
			if (plan== null) {
				this.fail("O Plano de Risco não foi encontrado");
				return;
			}
			
			PaginatedList<Unit> units = this.unitBS.listOnlyUnitsbyPlanRisk(plan);
			this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna unidade.
	 * 
	 * @param id
	 *            Id da unidade a ser retornado.
	 * @return Unit Retorna a unidade de acordo com o id passado.
	 */
	@Get(PATH + "/{id}")
	@NoCache
	@Permissioned
	public void getUnit(Long id) {
		try {
			Unit unit = this.unitBS.retrieveUnitById(id);
			if (unit == null) {
				this.fail("A unidade solicitada não foi encontrado.");
			} else {
				this.success(unit);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Retorna subunidades.
	 * 
	 * @param unitId
	 *            Id da unidade parent.
	 * @return <PaginatedList> Subunidades filhas da unidade passada
	 */
	@Get(PATH + "/listsub/{unitId}")
	@NoCache
	@Consumes
	public void listSubunits(@NotNull Long unitId) {
		try {
			Unit unit = this.unitBS.exists(unitId, Unit.class);
			
			if (unit == null) {
				this.fail("A unidade não foi encontrada");
				return;
			}
			
			PaginatedList<Unit> subunits = this.unitBS.listSubunitbyUnit(unit);
			this.success(subunits);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Retorna processos das unidades.
	 * 
	 * @return PaginatedList<RiskProcess>
	 * 			Lista de Processos 
	 */
	@Get(PATH + "/process")
	@NoCache
	@Permissioned
	public void listProcess() {
		try {
			PaginatedList<Process> list= this.unitBS.listProcess();
			
			this.success(list);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Erro inesperado: " + ex.getMessage());
		}
	}
	

	/**
	 * Exclui unidade.
	 * 
	 * @param id
	 *            Id da unidade a ser excluído.
	 *
	 */
	@Delete(PATH + "/{id}")
	@NoCache
	// @Permissioned(value = AccessLevels.MANAGER, permissions = {
	// ManagePolicyPermission.class })
	public void deleteUnit(@NotNull Long id) {
		try {

			Unit unit = this.unitBS.exists(id, Unit.class);
			if (GeneralUtils.isInvalid(unit)) {
				this.result.notFound();
				return;
			}

			// verifica se possui subunidades vinculadas
			PaginatedList<Unit> subunits = this.unitBS.listSubunitbyUnit(unit);
			if (subunits.getTotal() > 0) {
				this.fail("Unidade possui subunidade(s) vinculada(s).");
				return;
			}

			// verifica se possui riscos vinculados
			PaginatedList<Risk> risks = this.riskBS.listRiskbyUnit(unit);
			if (risks.getTotal() > 0) {
				this.fail("Unidade possui risco(s) vinculado(s).");
				return;
			}
			
			//verifica se possui processos vinculados com algum risco de outra unidade?
			//um processo pode estar vinculado a um risco de outra unidade? parentemente sim
			PaginatedList<Process> processes = this.processBS.listProcessbyUnit(unit);
			for(Process process :processes.getList()) {
				
				if (this.riskBS.hasLinkedRiskProcess(process)) {
					this.fail("Processo vinculado a um Risco. É necessário deletar a vinculação no Risco para depois excluir a unidade.");
					return;
				}
				if (this.riskBS.hasLinkedRiskActivity(process)) {
					this.fail("Processo vinculado a um Risco. É necessário deletar a vinculação no Risco para depois excluir a unidade.");
					return;
				}
			}
			
			//deletar processos desta unidade
			for(Process process :processes.getList()) {
				this.processBS.deleteProcess(process);
			}

			this.unitBS.delete(unit);
			this.success();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Atualiza unidade.
	 * 
	 * @param unit
	 *            Unidade a ser atualizada.
	 *
	 */
	@Put(PATH)
	@NoCache
	// @Permissioned(value = AccessLevels.MANAGER, permissions = {
	// ManagePolicyPermission.class })
	@Consumes
	public void updateUnit(@NotNull Unit unit) {
		try {
			Unit existent = this.unitBS.exists(unit.getId(), Unit.class);
			if (existent == null) {
				this.fail("A unidade não foi encontrada.");
				return;
			}
			
			User user = this.riskBS.exists(unit.getUser().getId(), User.class);
			if (user == null) {
				this.fail("O usuário responsável não foi encontrado.");
				return;
			}

			existent.setAbbreviation(unit.getAbbreviation());
			existent.setUser(user);
			existent.setDescription(unit.getDescription());
			
			this.unitBS.persist(existent);
			
			this.success();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	
	/**
	 * Cria arquivo pdf  para exportar relatório  
	 * 
	 * 
	 * @param title
	 * @param author
	 * @param pre
	 * @param item
	 * @param subitem
	 * @throws DocumentException 
	 * @throws IOException 
	 * 
	 */
	@Get(PATH + "/exportUnitReport")
	@NoCache
	//@Permissioned
	public void exportreport(String title, String author, boolean pre, String units,String subunits){
		try {
		
			File pdf = this.pdf.exportUnitReport(title, author, units, subunits);

			OutputStream out;
			FileInputStream fis= new FileInputStream(pdf);
			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"" + title + ".pdf\"");
			out = this.response.getOutputStream();
			
			IOUtils.copy(fis, out);
			out.close();
			fis.close();
			pdf.delete();
			this.result.nothing();
			
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			this.fail(ex.getMessage());
		}
	}
}
