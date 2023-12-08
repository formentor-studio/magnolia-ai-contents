package org.formentor.magnolia.ai.ui.datasource;

import info.magnolia.ui.datasource.DatasourceDefinition;
import info.magnolia.ui.datasource.DatasourceType;

@DatasourceType("aiModelsDatasource")
public class AiModelsDatasourceDefinition implements DatasourceDefinition {
    @Override
    public Class<?> getEntityType() {
        return String.class;
    }

    @Override
    public String getName() {
        return "aimodels";
    }
}
