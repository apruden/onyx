/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.etl.participant.IParticipantPostProcessor;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterProcess;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Listener responsible for tracking the different events that happen during the appointment list update process
 */
public class AppointmentListUpdateListener {

  @Autowired
  private Collection<IParticipantPostProcessor> participantPostProcessors;

  private AppointmentManagementService appointmentManagementService;

  private int addedParticipants;

  private int updatedParticipants;

  private int ignoredParticipants;

  private int unreadableParticipants;

  private List<Participant> processedParticipants;

  public AppointmentListUpdateListener() {
    super();
    initListener();
  }

  /**
   * Increments the unreadableParticipants counter whenever the ParticipantReader throws an exception.
   *
   * @throws Exception
   */
  @OnReadError
  public void onParticipantReadError() throws Exception {
    addUnreadableParticipant();
  }

  /**
   * Called before the step of the AppointmentUpdateList job. Clears the previous participantProcessor data (needed when
   * several lists are submitted)
   *
   * @param stepExecution
   * @return
   */
  @BeforeStep
  public void beforeStep(StepExecution stepExecution) {
    ParticipantProcessor.initProcessor();
    initListener();
  }

  /**
   * Called on the return of the ParticipantProcessor. Increments: 1) the ignoredParticipants counter when the result is
   * null, 2) the addedParticipants counter when result is not null and item.getId() is null, 3) the updatedParticipants
   * counter when result and item.getId() are not null
   *
   * @param item
   * @param result
   */
  @AfterProcess
  public void afterParticipantProcessed(Participant item, Participant result) {
    if(item != null) {
      if(result == null) {
        addIgnoredParticipant();
      } else if(item.getId() != null) {
        addUpdatedParticipant();
      } else {
        addAddedParticipant();
      }
      processedParticipants.add(item);
    }
  }

  /**
   * Called after the step of the AppointmentUpdateList job. Persists the job resuming data.
   *
   * @param stepExecution
   * @return
   */
  @AfterStep
  public ExitStatus afterUpdateCompleted(StepExecution stepExecution) {
    ExitStatus status = stepExecution.getExitStatus();

    if(status.getExitCode().equals("COMPLETED")) {
      AppointmentUpdateStats appointmentUpdateStats = new AppointmentUpdateStats(
          stepExecution.getJobParameters().getDate("date"), getAddedParticipants(), getUpdatedParticipants(),
          getIgnoredParticipants(), getUnreadableParticipants());
      appointmentUpdateStats.setFileName(stepExecution.getExecutionContext().getString("fileName", "<N/A>"));
      appointmentManagementService.saveAppointmentUpdateStats(appointmentUpdateStats);
    }

    AppointmentUpdateLog.addLog(stepExecution.getExecutionContext(),
        new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "End updating appointments"));

    try {
      AppointmentUpdateLog.addLog(stepExecution.getExecutionContext(),
          new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO,
              "Start participants post processing"));
      postProcess(stepExecution.getExecutionContext(), processedParticipants);
      AppointmentUpdateLog.addLog(stepExecution.getExecutionContext(),
          new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO,
              "End participants post processing"));
    } catch(Exception e) {
      AppointmentUpdateLog.addLog(stepExecution.getExecutionContext(),
          new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR,
              "Error while post processing participants: " + e.getMessage()));
      e.printStackTrace();
    } finally {
      processedParticipants = null;
    }

    return status;
  }

  public void postProcess(ExecutionContext context, List<Participant> items) {
    if(participantPostProcessors != null) {
      for(IParticipantPostProcessor postWriter : participantPostProcessors) {
        postWriter.process(context, items);
      }
    }
  }

  private void initListener() {
    setAddedParticipants(0);
    setUpdatedParticipants(0);
    setIgnoredParticipants(0);
    setUnreadableParticipants(0);
    processedParticipants = Lists.newArrayList();
  }

  public int getAddedParticipants() {
    return addedParticipants;
  }

  public int getUpdatedParticipants() {
    return updatedParticipants;
  }

  public int getIgnoredParticipants() {
    return ignoredParticipants;
  }

  public int getUnreadableParticipants() {
    return unreadableParticipants;
  }

  public void setAddedParticipants(int addedParticipants) {
    this.addedParticipants = addedParticipants;
  }

  public void setUpdatedParticipants(int updatedParticipants) {
    this.updatedParticipants = updatedParticipants;
  }

  public void setIgnoredParticipants(int ignoredParticipants) {
    this.ignoredParticipants = ignoredParticipants;
  }

  public void setUnreadableParticipants(int unreadableParticipants) {
    this.unreadableParticipants = unreadableParticipants;
  }

  public void addAddedParticipant() {
    this.addedParticipants++;
  }

  public void addIgnoredParticipant() {
    this.ignoredParticipants++;
  }

  public void addUpdatedParticipant() {
    this.updatedParticipants++;
  }

  public void addUnreadableParticipant() {
    this.unreadableParticipants++;
  }

  public void setAppointmentManagementService(AppointmentManagementService appointmentManagementService) {
    this.appointmentManagementService = appointmentManagementService;
  }
}
