/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.engine.variable.IActionVariableProvider;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.IVariableProvider;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.core.wicket.consent.ElectronicConsentUploadPage;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MarbleModule implements Module, IVariableProvider, ApplicationContextAware {

  private static final String MODE_ATTRIBUTE = "mode";

  private static final String ACCEPTED_ATTRIBUTE = "accepted";

  private static final String LOCALE_ATTRIBUTE = "locale";

  private ApplicationContext applicationContext;

  private ConsentService consentService;

  private IActionVariableProvider actionVariableProvider;

  private List<Stage> stages;

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("marbleReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("marbleInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("marbleCompletedState");

    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);

    exec.setInitialState(ready);

    return exec;
  }

  public String getName() {
    return "marble";
  }

  public void initialize(WebApplication application) {
    // Mount page to specific URL so it can be called from <embed> tag (submit form button).
    application.mountBookmarkablePage("/uploadConsent", ElectronicConsentUploadPage.class);
  }

  public void shutdown(WebApplication application) {
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setActionVariableProvider(IActionVariableProvider actionVariableProvider) {
    this.actionVariableProvider = actionVariableProvider;
  }

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {

    VariableData varData = new VariableData(variablePathNamingStrategy.getPath(variable));

    if(actionVariableProvider.isActionVariable(variable)) {
      varData = actionVariableProvider.getActionVariableData(participant, variable, variablePathNamingStrategy, varData, variable.getParent().getParent().getName());
    } else {
      // get participant's consent
      Consent consent = consentService.getConsent(participant.getInterview());

      if(consent != null) {
        String varName = variable.getName();
        if(varName.equals(ACCEPTED_ATTRIBUTE)) {
          varData.addData(DataBuilder.buildBoolean(consent.isAccepted()));
        } else if(varName.equals(LOCALE_ATTRIBUTE) && consent.getLocale() != null) {
          varData.addData(DataBuilder.buildText(consent.getLocale().toString()));
        } else if(varName.equals(MODE_ATTRIBUTE) && consent.getMode() != null) {
          varData.addData(DataBuilder.buildText(consent.getMode().toString()));
        }
      }
    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    for(Stage stage : stages) {
      Variable stageVariable = new Variable(stage.getName());
      variables.add(stageVariable);

      stageVariable.addVariable(new Variable(MODE_ATTRIBUTE).setDataType(DataType.TEXT));
      stageVariable.addVariable(new Variable(LOCALE_ATTRIBUTE).setDataType(DataType.TEXT));
      stageVariable.addVariable(new Variable(ACCEPTED_ATTRIBUTE).setDataType(DataType.BOOLEAN));

      stageVariable.addVariable(actionVariableProvider.createActionVariable());
    }

    return variables;
  }

}
