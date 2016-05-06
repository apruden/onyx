package org.obiba.onyx.marble.core;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.FdfProducer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class FdfProducerFactory implements ApplicationContextAware{

    public ActiveInterviewService getActiveInterviewService() {
        return activeInterviewService;
    }

    public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
        this.activeInterviewService = activeInterviewService;
    }

    private ActiveInterviewService activeInterviewService;

    private ApplicationContext applicationContext;

    public FdfProducer getFdfProducer() {
        return (FdfProducer) applicationContext.getBean("fdfProducer");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getStageName() {
        return activeInterviewService.getInteractiveStage().getName();
    }
}
