package org.formentor.magnolia.ai.ui.datasource;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.provider.AbstractDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.ItemCaptionGenerator;
import info.magnolia.ui.field.SelectFieldSupport;
import info.magnolia.ui.filter.DataFilter;
import org.formentor.magnolia.ai.AIContentsModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AiModelsSelectFieldSupport implements SelectFieldSupport<String> {
    private final List<AIContentsModule.AiModel> aiModelList;
    public AiModelsSelectFieldSupport(AIContentsModule aiContentsModule) {
        aiModelList = (aiContentsModule.getModels() == null)? new ArrayList<>(): aiContentsModule.getModels();
    }

    @Override
    public DataProvider<String, ?> getDataProvider() {
        return new AiModelsDataProvider(aiModelList);
    }

    @Override
    public ItemCaptionGenerator<String> getItemCaptionGenerator() {
        return value -> aiModelList.stream()
                .filter(aiModel -> aiModel.getName().equals(value))
                .map(AIContentsModule.AiModel::getLabel)
                .findFirst().orElse(null);
    }

    @Override
    public Converter<String, String> defaultConverter() {
        return new Converter<>() {
            @Override
            public Result<String> convertToModel(String caption, ValueContext context) {
                return aiModelList.stream()
                        .filter(aiModel -> aiModel.getLabel().equals(caption))
                        .map(AIContentsModule.AiModel::getName)
                        .findFirst()
                        .map(name -> Result.ok(name)).orElse(Result.ok(null));
            }

            @Override
            public String convertToPresentation(String value, ValueContext context) {
                return aiModelList.stream()
                        .filter(aiModel -> aiModel.getName().equals(value))
                        .map(AIContentsModule.AiModel::getLabel)
                        .findFirst().orElse(null);
            }
        };
    }

    private static class AiModelsDataProvider extends AbstractDataProvider<String, DataFilter> {

        private final List<AIContentsModule.AiModel> aiModelList;

        private AiModelsDataProvider(List<AIContentsModule.AiModel> aiModelList) {
            this.aiModelList = aiModelList;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<String, DataFilter> query) {
            return aiModelList.size();
        }

        @Override
        public Stream<String> fetch(Query<String, DataFilter> query) {
            return aiModelList.stream().map(AIContentsModule.AiModel::getName);
        }
    }
}
