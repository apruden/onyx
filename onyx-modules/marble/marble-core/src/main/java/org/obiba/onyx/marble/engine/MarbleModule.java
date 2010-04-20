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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.magma.spring.BeanValueTableFactoryBean;
import org.obiba.magma.spring.ValueTableFactoryBean;
import org.obiba.magma.spring.ValueTableFactoryBeanProvider;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.core.wicket.consent.ElectronicConsentUploadPage;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.magma.ConsentBeanResolver;
import org.obiba.onyx.marble.magma.ConsentVariableValueSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Sets;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;

public class MarbleModule implements Module, ValueTableFactoryBeanProvider, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(MarbleModule.class);

  private ApplicationContext applicationContext;

  private ConsentService consentService;

  private List<Stage> stages;

  private Map<String, String> variableToFieldMap = new HashMap<String, String>();

  private ConsentBeanResolver beanResolver;

  private VariableEntityProvider variableEntityProvider;

  //
  // Module Methods
  //

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

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public Component getWidget(String id) {
    return null;
  }

  public boolean isInteractive() {
    return false;
  }

  public void delete(Participant participant) {
    consentService.purgeConsent(participant.getInterview());
  }

  //
  // ValueTableFactoryBeanProvider Methods
  //

  public Set<? extends ValueTableFactoryBean> getValueTableFactoryBeans() {
    Set<BeanValueTableFactoryBean> tableFactoryBeans = Sets.newHashSet();

    for(Stage stage : stages) {
      BeanValueTableFactoryBean b = new BeanValueTableFactoryBean();
      b.setValueTableName(stage.getName());
      b.setValueSetBeanResolver(beanResolver);
      b.setVariableEntityProvider(variableEntityProvider);

      ConsentVariableValueSourceFactory factory = new ConsentVariableValueSourceFactory(stage.getName());
      factory.setVariableToFieldMap(variableToFieldMap);
      b.setVariableValueSourceFactory(factory);

      tableFactoryBeans.add(b);
    }

    return tableFactoryBeans;
  }

  //
  // Methods
  //

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  public void setBeanResolver(ConsentBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  public void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    this.variableEntityProvider = variableEntityProvider;
  }

  public String getConsentField(Consent consent, String fieldName) {

    byte[] pdfForm = consent.getPdfForm();
    // Access PDF content with IText library.
    PdfReader reader;
    try {
      reader = new PdfReader(pdfForm);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }

    // Get the PDF form fields.
    AcroFields form = reader.getAcroFields();
    return form.getField(fieldName);
  }

  public void setVariableToFieldMap(String keyValuePairs) {
    variableToFieldMap.clear();
    // Get list of strings separated by the delimiter
    StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
    while(tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken();
      String[] entry = token.split("=");
      if(entry.length == 2) {
        variableToFieldMap.put(entry[0].trim(), entry[1].trim());
      } else {
        log.error("Could not identify PDF field name to variable path mapping: " + token);
      }
    }
  }
}
