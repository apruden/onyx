package org.obiba.onyx.jade.engine;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JadeModule implements Module, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);

  private ApplicationContext applicationContext;

  private ActiveInterviewService activeInterviewService;

  private List<Stage> stages;

  public String getName() {
    return "jade";
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage, IStageExecution... dependsOn) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("jadeReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("jadeInProgressState");
    AbstractStageState skipped = (AbstractStageState) applicationContext.getBean("jadeSkippedState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("jadeCompletedState");
    AbstractStageState notApplicable = (AbstractStageState) applicationContext.getBean("jadeNotApplicableState");
    exec.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(ready, TransitionEvent.SKIP, skipped);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(inProgress, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(skipped, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);

    AbstractStageState waiting = (AbstractStageState) applicationContext.getBean("jadeWaitingState");
    exec.addEdge(notApplicable, TransitionEvent.VALID, ready);
    exec.addEdge(notApplicable, TransitionEvent.INVALID, waiting);

    // if (dependsOn != null && dependsOn.length>0) {
    exec.addEdge(waiting, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(waiting, TransitionEvent.VALID, ready);
    exec.addEdge(waiting, TransitionEvent.SKIP, skipped);
    exec.addEdge(ready, TransitionEvent.INVALID, waiting);
    exec.addEdge(skipped, TransitionEvent.INVALID, waiting);
    exec.addEdge(completed, TransitionEvent.INVALID, waiting);

    if(stage.getStageDependencyCondition() == null) {
      exec.setInitialState(ready);
    } else {
      if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == null) {
        exec.setInitialState(waiting);
      } else if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == true) {
        exec.setInitialState(ready);
      } else {
        exec.setInitialState(notApplicable);
      }
    }
    return exec;
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

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }
}
