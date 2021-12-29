package uk.co.eelpieconsulting.spring;

import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class VelocityViewResolver extends AbstractTemplateViewResolver {

    public VelocityViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    protected Class<?> requiredViewClass() {
        return VelocityView.class;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        VelocityView view = (VelocityView) super.buildView(viewName);
        view.setAttributesMap(getAttributesMap());
        return view;
    }

}
