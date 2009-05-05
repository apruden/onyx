/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.panel.OnyxEntityList;

public class TubeRegistrationPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  //
  // Constructors
  //

  public TubeRegistrationPanel(String id) {
    super(id);

    setOutputMarkupId(true);

    add(new TubeBarcodePanel("tubeBarcodePanel"));
    add(new OnyxEntityList<RegisteredParticipantTube>("list", new RegisteredParticipantTubeProvider(), new RegisteredParticipantTubeColumnProvider(tubeRegistrationConfiguration), new SpringStringResourceModel("Ruby.RegisteredParticipantTubeList")));
  }
}