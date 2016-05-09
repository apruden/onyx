package org.obiba.onyx.marble.core;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class StagePropertyResolver {

  private static final String BASENAME = "basename";

  private static final String VARIABLE_TO_FIELD_MAP = "variableToField";

  private Properties marbleProperties;

  private Resource props;

  private ActiveInterviewService activeInterviewService;

  public String getBasename() {
    String stageName = activeInterviewService.getInteractiveStage().getName();
    return safeGetStageProperty(stageName, BASENAME);
  }

  public String getVariableToFieldMap(String stageName) {
    return safeGetStageProperty(stageName, VARIABLE_TO_FIELD_MAP);
  }

  private String safeGetStageProperty(String stageName, String suffix) {
    Properties res = null;
    try {
      res = PropertiesLoaderUtils.loadProperties(props);
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String variableToField = marbleProperties.getProperty(getPropertyId(stageName, suffix));

    if(variableToField == null) variableToField = marbleProperties.getProperty(getPropertyId(null, suffix));

    return variableToField;
  }

  private String getPropertyId(String stageName, String suffix) {
    List<String> parts = Lists.newArrayList("org.obiba.onyx.marble.consent");

    if(!Strings.isNullOrEmpty(stageName)) {
      parts.add(stageName);
    }

    parts.add(suffix);

    return Joiner.on(".").join(parts);
  }

  @Required
  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public Properties getMarbleProperties() {
    return marbleProperties;
  }

  public void setMarbleProperties(Properties marbleProperties) {
    this.marbleProperties = marbleProperties;
  }

  public Resource getProps() {
    return props;
  }

  public void setProps(Resource props) {
    this.props = props;
  }
}
