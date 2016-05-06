package org.obiba.onyx.marble.core;

import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageLocalizedResourceLoader extends LocalizedResourceLoader {
    private static final Logger log = LoggerFactory.getLogger(StageLocalizedResourceLoader.class);

    private ActiveInterviewService activeInterviewService;

    @Override
    public String getResourceBasename() {
        String stageName = activeInterviewService.getInteractiveStage().getName();

        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>. " + stageName);

        return stageName;
    }

    public ActiveInterviewService getActiveInterviewService() {
        return activeInterviewService;
    }

    public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
        this.activeInterviewService = activeInterviewService;
    }
}
