/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.test.TestVariableProvider;

/**
 * 
 */
public class OnyxDataExportTest {

  VariableDirectory mockDirectory = new VariableDirectory();

  OnyxDataExport ode;

  UserSessionService mockUserSessionService;

  EntityQueryService mockEntityQueryService;

  IOnyxDataExportStrategy mockExportStrategy;

  TestVariableProvider variableProvider;

  @Before
  public void setup() {
    mockDirectory.setVariablePathNamingStrategy(new DefaultVariablePathNamingStrategy());

    variableProvider = new TestVariableProvider();
    variableProvider.addVariables(getClass().getResourceAsStream("testVariables.xml"));
    mockDirectory.registerVariables(variableProvider);

    mockUserSessionService = EasyMock.createMock(UserSessionService.class);
    mockEntityQueryService = EasyMock.createMock(EntityQueryService.class);
    mockExportStrategy = EasyMock.createMock(IOnyxDataExportStrategy.class);

    ode = new OnyxDataExport();
    ode.setUserSessionService(mockUserSessionService);
    ode.setQueryService(mockEntityQueryService);
    ode.setExportStrategy(mockExportStrategy);
    ode.setVariableDirectory(mockDirectory);
  }

  @Test
  public void testNoDataToExport() throws Exception {
    User exportUser = new User();
    List<Interview> testInterviews = new LinkedList<Interview>();
    EasyMock.expect(mockUserSessionService.getUser()).andReturn(exportUser);
    EasyMock.expect(mockEntityQueryService.match((Interview) EasyMock.anyObject())).andReturn(testInterviews);
    EasyMock.replay(mockUserSessionService, mockEntityQueryService, mockExportStrategy);
    ode.exportCompletedInterviews();
    EasyMock.verify(mockUserSessionService, mockEntityQueryService, mockExportStrategy);
  }

  @Test
  public void testExport() throws Exception {
    User exportUser = new User();

    Interview interview = new Interview();
    Participant participant = new Participant();
    participant.setBarcode("testbarcode");
    interview.setParticipant(participant);
    variableProvider.createRandomData(participant);
    List<Interview> testInterviews = new LinkedList<Interview>();
    testInterviews.add(interview);

    EasyMock.expect(mockUserSessionService.getUser()).andReturn(exportUser);
    EasyMock.expect(mockEntityQueryService.match((Interview) EasyMock.anyObject())).andReturn(testInterviews);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mockExportStrategy.prepare((OnyxDataExportContext) EasyMock.anyObject());
    EasyMock.expect(mockExportStrategy.newEntry(participant.getBarcode() + ".xml")).andReturn(baos);
    mockExportStrategy.terminate((OnyxDataExportContext) EasyMock.anyObject());

    EasyMock.replay(mockUserSessionService, mockEntityQueryService, mockExportStrategy);
    ode.exportCompletedInterviews();
    EasyMock.verify(mockUserSessionService, mockEntityQueryService, mockExportStrategy);

    byte data[] = baos.toByteArray();
    Assert.assertNotNull(data);
    String xml = new String(data);
    String expectedData = getExpectedOutput();
    Assert.assertEquals(expectedData, xml);
  }

  /**
   * @return
   */
  private String getExpectedOutput() {
    // The expected output contains random data. The data is always the same across test execution, because the
    // TestVariableProvider uses the same seed for every invocation. The data will be different if the number or
    // ordering of the variables differs.
    try {
      InputStream is = getClass().getResourceAsStream("dataOutput.xml");

      StringBuilder sb = new StringBuilder();
      char[] buffer = new char[1024 * 1024];
      int r = 0;
      Reader reader = new InputStreamReader(is);
      while((r = reader.read(buffer)) > 0) {
        sb.append(buffer, 0, r);
      }
      return sb.toString();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
