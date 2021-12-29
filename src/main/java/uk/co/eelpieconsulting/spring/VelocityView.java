package uk.co.eelpieconsulting.spring;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VelocityView extends AbstractTemplateView {

    private VelocityEngine velocityEngine;
    private VelocityEngineUtils velocityEngineUtils;

    public VelocityView() {
    }

    @Override
    protected void initServletContext(ServletContext servletContext) {
        this.velocityEngine = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                obtainApplicationContext(), VelocityEngine.class, true, false);
        this.velocityEngineUtils = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                obtainApplicationContext(), VelocityEngineUtils.class, true, false);
    }

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        velocityEngineUtils.mergeTemplate(velocityEngine, this.getUrl(), "UTF-8", map, httpServletResponse.getWriter());
    }

}
