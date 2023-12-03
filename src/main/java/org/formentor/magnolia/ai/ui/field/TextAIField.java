package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.application.TextAIComplete;
import org.formentor.magnolia.ai.domain.PropertyPromptValue;
import org.formentor.magnolia.ai.domain.Strategy;
import org.formentor.magnolia.ai.ui.dialog.DialogCallback;

import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_MAP;

@Slf4j
public class TextAIField extends CustomField<String> {

    private final String appName;
    private final LocaleContext localeContext;
    private final Locale fallbackLocale;

    private final TextAIFieldDefinition definition;
    private final Strategy strategy;

    private final AbstractTextField textField;
    private final DialogCallback dialogCallback;
    private final TextAIComplete textAIComplete;
    private final SimpleTranslator i18n;
    private final ValueContext<JcrNodeWrapper> valueContext;
    private final AIContentsModule aiContentsModule;
    private final UIComponent parentView;

    private static final String DIALOG_COMPLETE_ID = "magnolia-ai-contents:CompleteTextDialog";
    private static final String DIALOG_EDIT_ID = "magnolia-ai-contents:EditTextDialog";
    private static final int COMPLETION_MAX_WORDS_DEFAULT = 2048;

    @Inject
    public TextAIField(AppContext appContext, TranslationService translationService, LocaleContext localeContext, I18nContentSupport i18nContentSupport, AbstractTextField textField, TextAIFieldDefinition definition, DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, DialogBuilder dialogBuilder, TextAIComplete textAIComplete, SimpleTranslator i18n, ValueContext<JcrNodeWrapper> valueContext, AIContentsModule aiContentsModule, UIComponent parentView) {
        this.appName = appContext.getName();
        this.localeContext = localeContext;
        this.fallbackLocale = i18nContentSupport.getFallbackLocale();
        this.definition = definition;
        this.strategy = determineStrategyFromString(definition.getStrategy());

        this.textField = textField;
        this.aiContentsModule = aiContentsModule;
        this.i18n = new SimpleTranslator(translationService, new LocaleProvider() {
            @Override
            public Locale getLocale() {
                return localeContext.getCurrent();
            }
        });
        this.valueContext = valueContext;
        this.parentView = parentView;
        this.dialogCallback = new DialogCallback(dialogDefinitionRegistry, i18nizer, parentView, dialogBuilder); // TODO try to inject DialogCallback
        this.textAIComplete = textAIComplete;
    }

    @Override
    protected Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(textField, buildActions(strategy));

        return layout;
    }

    @Override
    protected void doSetValue(String value) {
        textField.setValue(Optional.ofNullable(value).orElse(""));
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }


    private Strategy determineStrategyFromString(String strategy) {
        try {
            return strategy == null ? null : Strategy.valueOf(strategy);
        } catch (IllegalArgumentException e) {
            log.warn("Text strategy {} not allowed", strategy);
            return null;
        }
    }

    protected Component buildActions(Strategy forStrategy) {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        switch (forStrategy) {
            case completion: {
                actionsLayout.addComponents(
                        buildCompletionButton(i18n.translate("magnolia-ai-contents.button.completion.label")),
                        buildEditButton(i18n.translate("magnolia-ai-contents.button.edit.label")));
                break;
            }
            case edit: {
                actionsLayout.addComponents(
                        buildEditButton(i18n.translate("magnolia-ai-contents.button.edit.label")));
                break;
            }
        }

        return actionsLayout;
    }

    private Button buildCompletionButton(String label) {
        Button button = new Button(label);
        button.addClickListener((Button.ClickListener) event -> {
            Map<String, String> initialFormValues = new HashMap<>();
            // By the moment it is discarded to build the prompt from FormView as it does not support Complex fields like MultiValue - See how works FormView.write(), the property "subEditors" is private :(
            // initialFormValues.put("prompt", derivePromptFromDefinitionAndFormView(definition, getFormFromSubApp(parentView)));
            initialFormValues.put("prompt", derivePromptFromDefinitionAndValueContext(definition, valueContext));
            initialFormValues.put("model", definition.getModel());

            dialogCallback.open(
                    DIALOG_COMPLETE_ID,
                    properties -> textAIComplete.complete(
                            properties.get("prompt").orElse(""),
                            properties.get("model").orElse(getDefaultModel()),
                            null
                    ).thenAccept(textField::setValue),
                    initialFormValues
            );}
        );

        return button;
    }

    private Button buildEditButton(String label) {
        Button button = new Button(label);
        button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                DIALOG_EDIT_ID,
                properties -> textAIComplete.edit(textField.getValue(), properties.get("prompt").toString()).thenAccept(textField::setValue),
                EMPTY_MAP
        ));

        return button;
    }

    /**
     * Derives a prompt from the definition and ValueContext - the Node in context -
     * IMPORTANT: Properties in ValueContext are not updated on changing the fields in UI.
     * @param definition
     * @param context
     * @return
     */
    private String derivePromptFromDefinitionAndValueContext(TextAIFieldDefinition definition, ValueContext<JcrNodeWrapper> context) {
        final PromptGeneratorDefinition promptGenerator = definition.getPromptGenerator();
        if (promptGenerator == null) {
            return "";
        }

        String prompt = context.getSingle()
                .map(I18nNodeWrapper::new)
                .map(item -> promptGenerator.getProperties()
                        .stream()
                        .reduce("", (acc, propertyDefinition) -> {
                            Optional<String> propertyPromptValue = getPropertyPromptValue(item, propertyDefinition);
                            return propertyPromptValue.map(propertyValue -> acc.concat(String.format("%s: %s.\n", getPropertyPromptLabel(propertyDefinition.getName()), propertyValue))).orElse(acc);
                        }, String::concat))
                .orElse("");

        if (promptGenerator.getTemplate() != null) {
            return i18n.translate(promptGenerator.getTemplate(), prompt, localeContext.getCurrent().getDisplayName(), definition.getWords());
        }

        return prompt;
    }

    private String getPropertyPromptLabel(String propertyName) {
        String labelKeyWithoutLabel = String.format("%s.%s", appName, propertyName);
        String label = i18n.translate(labelKeyWithoutLabel);
        if (label.equals(labelKeyWithoutLabel)) {
            return i18n.translate(labelKeyWithoutLabel + ".label");
        }
        return label;
    }

    private Optional<String> getPropertyPromptValue(Node node, PropertyPromptValue promptDefinition) {
        Optional<Object> propertyValueObject = getPropertyValueObject(node, promptDefinition.getName());
        if (!propertyValueObject.isPresent()) {
            return Optional.empty();
        }

        if (!isMultiple(propertyValueObject.get())) {
            String propertyValueString = propertyValueObject.get().toString();
            return (promptDefinition.isReference())
                    ? getReferencedNodePropertyString(promptDefinition.getTargetWorkspace(), propertyValueString, promptDefinition.getTargetPropertyName())
                    : Optional.of(propertyValueString);
        } else {
            List<String> propertyValueList = (List<String>)propertyValueObject.get();
            int limit = Optional.ofNullable(promptDefinition.getLimit()).orElse(propertyValueList.size());
            if (!promptDefinition.isReference()) {
                return Optional.ofNullable(String.join(", ", propertyValueList.subList(0, Math.min(limit, propertyValueList.size()))));
            } else {
                String referenceWorkspace = promptDefinition.getTargetWorkspace();
                String referencePropertyName = promptDefinition.getTargetPropertyName();
                List<String> propertyValuesPopulatedWithReference = propertyValueList.stream()
                        .map(referenceNodeId -> getReferencedNodePropertyString(referenceWorkspace, referenceNodeId, referencePropertyName))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                return Optional.ofNullable(String.join(", ", propertyValuesPopulatedWithReference.subList(0, Math.min(limit, propertyValuesPopulatedWithReference.size()))));
            }
        }
    }

    private Optional<String> getReferencedNodePropertyString(String referenceWorkspace, String referenceNodeId, String referencePropertyName) {
        try {
            Session session = MgnlContext.getJCRSession(referenceWorkspace);
            Node referenceNode = session.getNodeByIdentifier(referenceNodeId);
            return getPropertyString(referenceNode, referencePropertyName);
        } catch (ItemNotFoundException e) {
            log.error("Can't find referenced node '{}' in workspace '{}' when building training example of {}", referenceNodeId, referenceWorkspace, e);
            return Optional.empty();
        } catch (RepositoryException e) {
            log.error("Errors reading property '{}' of the referenced node {} in workspace '{}' when building training example of {}", referencePropertyName, referenceNodeId, referenceWorkspace, e);
            return Optional.empty();
        }
    }

    private Optional<Object> getPropertyValueObject(Node node, String propertyName) {
        Object valueI18n = PropertyUtil.getPropertyValueObject(node, buildPropertyNameByLocale(propertyName, localeContext.getCurrent()));
        return (valueI18n != null)? Optional.of(valueI18n): Optional.ofNullable(PropertyUtil.getPropertyValueObject(node, propertyName));
    }

    private Optional<String> getPropertyString(Node node, String propertyName) {
        String valueI18n = PropertyUtil.getString(node, buildPropertyNameByLocale(propertyName, localeContext.getCurrent()));
        return (valueI18n != null)? Optional.of(valueI18n): Optional.ofNullable(PropertyUtil.getString(node, propertyName));
    }

    private String buildPropertyNameByLocale(String propertyName, Locale locale) {
        return (locale.equals(fallbackLocale))? propertyName: String.format("%s_%s", propertyName, locale);
    }

    private boolean isMultiple(Object propertyValue) {
        return propertyValue instanceof List;
    }

    private String getDefaultModel() {
        List<AIContentsModule.AiModel> models = aiContentsModule.getModels();
        if (models != null && !models.isEmpty()) {
            return models.get(0).getName();
        }

        return "";
    }
    /**
     * NOTE: By the moment it is discarded to build the prompt from FormView as it does not support Complex fields like MultiValue - See how works FormView.write(), the property "subEditors" is private :(
     *
     * Derives a prompt from the definition and the FormView - the current value in UI -
     * IMPORTANT: It does not work for ComplexProperties like MultiValue fields!
     *
     * @param definition
     * @param form
     * @return
     */
    private String derivePromptFromDefinitionAndFormView(TextAIFieldDefinition definition, FormView form) {
        // i18n.translate("ai.prompt.template.rooms", ((FormView)parentView.accessViewBeanStore().getInstance(EditorView.class).get()).getPropertyValue("name"));
        final PromptGeneratorDefinition promptGenerator = definition.getPromptGenerator();
        if (promptGenerator == null) {
            return "";
        }
        String prompt = promptGenerator.getProperties().stream()
                .reduce("", (acc, propertyDefinition) -> {
                            Optional<String> propertyPromptValue = form.getPropertyValue(propertyDefinition.getName());
                            return propertyPromptValue.map(value -> acc.concat(String.format("%s is %s. ", propertyDefinition.getName(), value))).orElse(acc);
                        },
                        String::concat)
                .trim();
        if (promptGenerator.getTemplate() != null) {
            return  i18n.translate(promptGenerator.getTemplate(), prompt);
        }

        return prompt;
    }

    /**
     * Returns FormView for a given SubApp.
     * IMPORTANT: Do not remove until building prompt from FormView is discarded definitively - see method derivePromptFromDefinitionAndFormView() -
     * @param view
     * @return
     */
    private FormView getFormFromSubApp(UIComponent view) {
        return (FormView)view.accessViewBeanStore().getInstance(EditorView.class).get();
    }
}
