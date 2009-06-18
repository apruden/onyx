/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print.impl;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;

/**
 * Base implementation of a IPrintableReport.
 */
abstract public class AbstractPrintableReport implements IPrintableReport {

  private static final Logger log = LoggerFactory.getLogger(AbstractPrintableReport.class);

  private String name;

  private MessageSourceResolvable label;

  private IDataSource readyCondition;

  protected ActiveInterviewService activeInterviewService;

  public MessageSourceResolvable getLabel() {
    return label;
  }

  public void setLabel(MessageSourceResolvable label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isReady() {
    if(readyCondition != null) {
      Participant participant = activeInterviewService.getParticipant();
      Data readyData = readyCondition.getData(participant);
      if(readyData == null) {
        log.info("Cannot get data for readyCondition of report {}, so readyCondition is false");
        return false;
      } else if(readyData.getType() == DataType.BOOLEAN) {
        log.info("Report {} readyCondition is {}", getName(), readyData.getValue());
        return (Boolean) readyData.getValue();
      } else {
        throw new RuntimeException("readyData not a BOOLEAN.  Please review the readyCondition for report : " + getName());
      }
    }

    // If no readyCondition exist than we assume that the report is "always ready".
    log.info("No readyCondition for report {}, so assume that the report is READY", getName());
    return true;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    activeInterviewService = (ActiveInterviewService) applicationContext.getBean("activeInterviewService");
  }

  public void setReadyCondition(IDataSource readyCondition) {
    this.readyCondition = readyCondition;
  }

}