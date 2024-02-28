package org.formentor.magnolia.ai.ui.field;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.util.TempFilesManager;
import info.magnolia.ui.theme.ResurfaceTheme;
import info.magnolia.ui.vaadin.server.DownloadStreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.formentor.magnolia.ai.domain.ImageAiGenerator;
import org.formentor.magnolia.ai.domain.ImageFormat;
import org.formentor.magnolia.ai.domain.ImageSize;
import org.formentor.magnolia.ai.ui.dialog.DialogCallback;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.EMPTY_MAP;

/**
 * Image field that generates contents from a given prompt.
 */
@Slf4j
public class ImageAIField extends CustomField<File> {
    private static final Resource DEFAULT_REVIEW_IMG = MagnoliaIcons.FILE;
    private static final Tika TIKA = new Tika();
    private static final String DIALOG_ID = "magnolia-ai-contents:GenerateImageDialog";
    private static final String DEFAULT_BUTTON_CREATE_LABEL = "Ask AI to create";

    private final TempFilesManager tempFilesManager;
    private final SimpleTranslator translator;
    private final MessagesManager messagesManager;
    private final ImageAiGenerator imageAiGenerator;

    private File currentTempFile;
    private CssLayout imageContainer;
    private Button removeUploadBtn;
    private Button downloadBtn;
    private Image thumbnail;
    private final DialogCallback dialogCallback;

    @Inject
    public ImageAIField(TempFilesManager tempFilesManager, SimpleTranslator translator, MessagesManager messagesManager, ImageAiGenerator imageAiGenerator, DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, UIComponent parentView, DialogBuilder dialogBuilder) {
        this.tempFilesManager = tempFilesManager;
        this.translator = translator;
        this.messagesManager = messagesManager;
        this.imageAiGenerator = imageAiGenerator;
        this.dialogCallback = new DialogCallback(dialogDefinitionRegistry, i18nizer, parentView, dialogBuilder); // TODO try to inject DialogCallback
    }

    @Override
    public File getValue() {
        return currentTempFile;
    }

    @Override
    protected void doSetValue(File value) {
        currentTempFile = value;
        tempFilesManager.register(value);
    }

    @Override
    protected Component initContent() {
        // Create body layout
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setMargin(false);
        rootLayout.setSpacing(true);

        // Container for the image
        thumbnail = new Image(StringUtils.EMPTY, DEFAULT_REVIEW_IMG);
        thumbnail.addStyleName("file-preview-thumbnail");
        imageContainer = buildImageContainer(thumbnail);

        HorizontalLayout rootUploadPanel = buildRootUploadPanel();
        rootUploadPanel.addComponents(imageContainer);
        rootLayout.addComponents(rootUploadPanel);

        updateControlVisibilities();

        return rootLayout;
    }

    private CssLayout buildImageContainer(Image thumbnail) {
        CssLayout imageContainer = commonUploadPanel();
        imageContainer.addComponents(buildControlButtonPanel(), thumbnail, buildCreateButton());

        return imageContainer;
    }

    private Button buildCreateButton() {
        Button button = new Button(DEFAULT_BUTTON_CREATE_LABEL);
        button.addStyleName("upload-button");
        button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                DIALOG_ID,
                properties -> {
                    final String promptForImage = properties.get("prompt").orElse("");
                    final String sizeOfImage = properties.get("size").orElse(ImageSize.Size1024.toString());
                    try {
                        currentTempFile = createImageAI(promptForImage, ImageSize.valueOf(sizeOfImage)).get().orElse(null); // TODO set currentTempFile Optional
                        updateControlVisibilities();
                        fireEvent(createValueChange(null, false));
                        Notification.show("Image created successfully");
                    } catch (Exception e) {
                        Notification.show("Errors creating AI image: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                    return CompletableFuture.completedFuture("");
                },
                EMPTY_MAP
        ));

        return button;
    }

    private CompletableFuture<Optional<File>> createImageAI(String rawPrompt, ImageSize size) {
        String prompt = Jsoup.clean(rawPrompt, Safelist.none());
        return imageAiGenerator.generateImage(prompt, 1, size, ImageFormat.url)
                .thenApply(imageUrl -> {
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        log.warn("Image not created");
                        return Optional.empty();
                    }
                    try {
                        File localFile = tempFilesManager.createTempFile(prompt.hashCode() + ".png");
                        final URL url = new URL(imageUrl);

                        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                             FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                             FileChannel fileChannel = fileOutputStream.getChannel()) {
                             fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        }
                        return Optional.of(localFile);
                    } catch (IOException e) {
                        log.error("Errors creating AI image", e);
                        return Optional.empty();
                    }
                });
    }

    private void updatePreviewThumbnail() {
        if (currentTempFile != null) {
            thumbnail.setIcon(null);
            thumbnail.setSource(new FileResource(currentTempFile));
            imageContainer.removeStyleName("upload-file-panel-large");
        } else {
            thumbnail.setIcon(null);
            thumbnail.setSource(null);
            imageContainer.setStyleName("upload-file-panel");
        }
    }

    private void updateControlVisibilities() {
        boolean hasValue = getValue() != null;

        removeUploadBtn.setVisible(hasValue);
        downloadBtn.setVisible(hasValue);

        updatePreviewThumbnail();
        thumbnail.setVisible(hasValue);
    }

    private Button createControlPanelButton(Resource icon) {
        Button button = new Button(icon);
        button.addStyleNames(ResurfaceTheme.BUTTON_ICON, "control-button");
        return button;
    }

    private void clearUploadPanel() {
        FileUtils.deleteQuietly(currentTempFile);
        currentTempFile = null;
        updateControlVisibilities();
    }

    private void openDownload() {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(currentTempFile))) {
            String mimeType = TIKA.detect(currentTempFile);
            StreamResource.StreamSource streamSource = () -> inputStream;
            StreamResource streamResource = new StreamResource(streamSource, "");
            streamResource.setMIMEType(mimeType);
            String fileName = currentTempFile.getName();
            DownloadStreamResource resource = new DownloadStreamResource(streamSource, fileName);
            // Accessing the DownloadStream via getStream() will set its cacheTime to whatever is set in the parent
            // StreamResource. By default it is set to 1000 * 60 * 60 * 24, thus we have to override it beforehand.
            // A negative value or zero will disable caching of this stream.
            resource.setCacheTime(-1);
            resource.getStream().setParameter("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
            resource.setMIMEType(mimeType);
            Page.getCurrent().open(resource, null, false);
        } catch (IOException e) {
            Message message = new Message(MessageType.ERROR,
                    translator.translate("fields.uploadField.download.error.subject"),
                    translator.translate("fields.uploadField.download.error.message"));

            messagesManager.sendLocalMessage(message);
            log.warn(e.getMessage());
        }
    }

    private CssLayout buildControlButtonPanel() {
        CssLayout controlButtonPanel = new CssLayout();
        controlButtonPanel.addStyleName("control-button-panel");
        controlButtonPanel.setWidth(30, Unit.PIXELS);

        removeUploadBtn = createControlPanelButton(MagnoliaIcons.TRASH);
        removeUploadBtn.addClickListener(event -> clearUploadPanel());
        removeUploadBtn.setDescription(translator.translate("fields.uploadField.upload.removeFile"));

        controlButtonPanel.addComponent(removeUploadBtn);

        downloadBtn = createControlPanelButton(MagnoliaIcons.DOWNLOAD);
        downloadBtn.addClickListener(event -> openDownload());
        downloadBtn.setDescription(translator.translate("fields.uploadField.upload.download"));
        controlButtonPanel.addComponent(downloadBtn);
        return controlButtonPanel;
    }

    private HorizontalLayout buildRootUploadPanel() {
        HorizontalLayout rootUploadPanel = new HorizontalLayout();
        rootUploadPanel.setSizeFull();
        rootUploadPanel.setHeightUndefined();
        rootUploadPanel.setVisible(true);

        return rootUploadPanel;
    }

    private CssLayout commonUploadPanel() {
        CssLayout uploadRelatedLayout = new CssLayout();
        uploadRelatedLayout.setSizeFull();
        uploadRelatedLayout.setStyleName("upload-file-panel");
        uploadRelatedLayout.setHeightUndefined();
        return uploadRelatedLayout;
    }
}
