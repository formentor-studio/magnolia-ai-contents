package org.formentor.magnolia.ai.ui.dialog;

import com.vaadin.ui.Window;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.action.CloseAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.dialog.ActionExecution;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.ui.dialog.DialogDefinition;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.EditorActionBar;
import info.magnolia.ui.dialog.FormDialogDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.field.LocaleSelector;
import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Dialog with callback function. Opens a modal for a given dialogId and calls to Callback function when.
 */
public class DialogCallback {
    private final DialogDefinitionRegistry dialogDefinitionRegistry;
    private final I18nizer i18nizer;
    private final UIComponent parentView;
    private final DialogBuilder dialogBuilder;

    @Inject
    public DialogCallback(DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, UIComponent parentView, DialogBuilder dialogBuilder) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.i18nizer = i18nizer;
        this.parentView = parentView;
        this.dialogBuilder = dialogBuilder;
    }

    public void open(String dialogId, Callback callback, Map<String, String> initialFormValues) {
        FormDialogDefinition formDialogDefinition = (FormDialogDefinition)getDialogDefinition(dialogId);
        EditorView form = (EditorView) parentView.create(formDialogDefinition.getForm());
        applyInitialValuesToForm(formDialogDefinition.getForm(), initialFormValues);
        form.applyDefaults();

        List<ActionExecution> actionExecutions = buildActions(formDialogDefinition, callback, form.getComponentProvider());

        Window dialog = dialogBuilder
                .withTitle(formDialogDefinition.getLabel())
                .withContent(form.asVaadinComponent())
                .light(formDialogDefinition.isLight())
                .width(formDialogDefinition.getWidth())
                .withFooter(configureFooter(formDialogDefinition, actionExecutions, form).layout())
                .buildAndOpen();

        CloseHandler closeHandler = getCloseHandler(dialog);
        dialog.addCloseListener((Window.CloseListener) e -> closeHandler.close());
        form.bindInstance(CloseHandler.class, closeHandler);
    }

    private void applyInitialValuesToForm(FormDefinition form, Map<String, String> initialValues) {
        initialValues.forEach((key, value) -> {
            form.getFieldDefinition(key).ifPresent(field -> {
                if (field instanceof TextFieldDefinition) {
                    ((TextFieldDefinition) field).setDefaultValue(value);
                }
            });
        });
    }

    private DialogDefinition getDialogDefinition(String dialogId) {
        return i18nizer.decorate(dialogDefinitionRegistry.getProvider(dialogId).get());
    }

    private List<ActionExecution> buildActions(FormDialogDefinition formDialogDefinition, Callback callback, ComponentProvider componentProvider) {
        List<ActionExecution> actionExecutions =
                ActionExecution.fromDefinitions(formDialogDefinition.getActions().values(), componentProvider)
                        .collect(toList());
        DialogCallbackActionDefinition dialogCallbackActionDefinition = new DialogCallbackActionDefinition();
        dialogCallbackActionDefinition.setCallback(callback);
        dialogCallbackActionDefinition.setLabel("Execute"); // TODO define a label
        actionExecutions.add(new ActionExecution(dialogCallbackActionDefinition, componentProvider));

        return actionExecutions;
    }

    private EditorActionBar configureFooter(FormDialogDefinition dialogDefinition, List<ActionExecution> actionExecutions, EditorView form) {
        EditorActionBar editorActionBar = form.create(EditorActionBar.class);
        editorActionBar
                .withActions(actionExecutions)
                .withLayoutDefinition(dialogDefinition.getFooterLayout());

        if (dialogDefinition.getForm().hasI18NProperties()) {
            LocaleSelector localeSelector = form.create(LocaleSelector.class);
            editorActionBar = editorActionBar.withLabeledControl("localeSelector", localeSelector);
        }
        return editorActionBar;
    }
    private CloseHandler getCloseHandler(Window dialog) {
        return dialog::close;
    }

    @FunctionalInterface
    public interface Callback extends Function<Map, CompletableFuture> {
    }

    @Getter
    @Setter
    public static class DialogCallbackActionDefinition extends CommitActionDefinition {  // TODO Do not extend CommitActionDefinition
        private Callback callback;

        public DialogCallbackActionDefinition() {
            setImplementationClass(DialogCallbackAction.class);
        }
    }

    public static class DialogCallbackAction extends CloseAction<DialogCallbackActionDefinition> {
        private final FormView form;

        @Inject
        public DialogCallbackAction(DialogCallbackActionDefinition definition, CloseHandler closeHandler, FormView form) {
            super(definition, closeHandler);
            this.form = form;
        }

        @Override
        public void execute() throws ActionExecutionException {
            Map<String, Object> properties = new HashMap<>();
            form.getPropertyNames().forEach(propertyName -> properties.put(propertyName.toString(), form.getPropertyValue(propertyName.toString()).orElse("")));
            getDefinition().getCallback()
                    .apply(properties)
                    .join();
            super.execute();
        }
    }
}
