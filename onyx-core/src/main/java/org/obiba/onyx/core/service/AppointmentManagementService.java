/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;

/**
 * Interface that manages the process of updating the appointment list
 */
public interface AppointmentManagementService {

  /**
   * executes the process of updating the appointments
   */
  public void updateAppointments();

  /**
   * Save the AppointmentUpdateStats
   * @param appointmentUpdateStats
   */
  public void saveAppointmentUpdateStats(AppointmentUpdateStats appointmentUpdateStats);

}
