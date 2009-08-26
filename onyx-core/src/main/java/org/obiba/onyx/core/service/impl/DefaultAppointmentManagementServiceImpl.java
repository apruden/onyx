/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultAppointmentManagementServiceImpl extends PersistenceManagerAwareService implements AppointmentManagementService, ResourceLoaderAware {

  private JobLauncher jobLauncher;

  private Job job;

  private String inputDirectory;

  private String outputDirectory;

  private File inputDir;

  private File outputDir;

  private ResourceLoader resourceLoader;

  public void initialize() {
    if(inputDirectory == null || inputDirectory.isEmpty()) throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null");

    try {
      setInputDir(resourceLoader.getResource(inputDirectory).getFile());
      if(!getInputDir().exists()) {
        getInputDir().mkdirs();
      }
      if(!getInputDir().isDirectory()) {
        throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: InputDirectory " + getInputDir().getAbsolutePath() + " is not a directory");
      }

      if(outputDirectory != null && !outputDirectory.isEmpty()) {
        setOutputDir(resourceLoader.getResource(outputDirectory).getFile());
        if(!getOutputDir().exists()) {
          getOutputDir().mkdirs();
        }
        if(!getOutputDir().isDirectory()) {
          throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: OutputDirectory " + getOutputDir().getAbsolutePath() + " is not a directory");
        }
      }
    } catch(IOException ex) {
      throw new RuntimeException("DefaultAppointmentManagementServiceImpl: Failed to access directory - " + ex);
    }
  }

  synchronized public void updateAppointments() {

    Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
    jobParameterMap.put("date", new JobParameter(new Date()));
    JobParameters parameters = new JobParameters(jobParameterMap);

    try {
      jobLauncher.run(job, parameters);
    } catch(JobExecutionAlreadyRunningException e) {
      // logger.error("This job is already running", e);
      throw new RuntimeException("This job is already running" + e);
    } catch(JobInstanceAlreadyCompleteException e) {
      // logger.info("This job is already complete. " + "Maybe you need to change the input parameters?", e);
      throw new RuntimeException("This job is already complete. " + "Maybe you need to change the input parameters?" + e);
    } catch(JobRestartException e) {
      // logger.error("Unspecified restart exception", e);
      throw new RuntimeException("Unspecified restart exception" + e);
    }

  }

  public void saveAppointmentUpdateStats(AppointmentUpdateStats appointmentUpdateStats) {
    getPersistenceManager().save(appointmentUpdateStats);
  }

  public void setInputDirectory(String inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public File getInputDir() {
    return inputDir;
  }

  public void setInputDir(File inputDir) {
    this.inputDir = inputDir;
  }

  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
  }

  public File getOutputDir() {
    return outputDir;
  }

  public void setJobLauncher(JobLauncher jobLauncher) {
    this.jobLauncher = jobLauncher;
  }

  public void setJob(Job job) {
    this.job = job;
  }

}
