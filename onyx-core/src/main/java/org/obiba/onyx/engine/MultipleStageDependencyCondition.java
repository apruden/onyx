package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

public class MultipleStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private Operator operator;

  private StageDependencyCondition leftStageDependencyCondition;

  private StageDependencyCondition rightStageDependencyCondition;

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    Boolean leftResult = leftStageDependencyCondition.isDependencySatisfied(activeInterviewService);
    Boolean rightResult = rightStageDependencyCondition.isDependencySatisfied(activeInterviewService);

    if(leftResult != null && rightResult != null) {
      if(operator.equals(Operator.AND)) return (leftResult && rightResult);
      else
        return (leftResult || rightResult);
    } else {
      if(operator.equals(Operator.AND)) {
        if(leftResult != null && leftResult == false) return false;
        if(rightResult != null && rightResult == false) return false;
        return null;
      } else {
        if(leftResult != null) return (leftResult == true) ? leftResult : null;
        if(rightResult != null) return (rightResult == true) ? rightResult : null;
        return null;
      }
    }
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return (leftStageDependencyCondition.isDependentOn(stageName) || rightStageDependencyCondition.isDependentOn(stageName));
  }

  public void setLeftStageDependencyCondition(StageDependencyCondition leftStageDependencyCondition) {
    this.leftStageDependencyCondition = leftStageDependencyCondition;
  }

  public void setRightStageDependencyCondition(StageDependencyCondition rightStageDependencyCondition) {
    this.rightStageDependencyCondition = rightStageDependencyCondition;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }
}
