package org.formentor.magnolia.ai.infrastructure.openai;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.keystore.PasswordManagerModule;
import info.magnolia.keystore.PasswordNodeTypes;
import info.magnolia.keystore.registry.PasswordRegistry;
import info.magnolia.observation.WorkspaceEventListenerRegistration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Optional;

/**
 * Provider for the OpenAI token.
 * This implementation fetches the token from Passwords manager.
 */
@Slf4j
public class TokenProviderPasswords implements TokenProvider {
    private static final String PASSWORDS_API_KEY_PATH="/openai/api-key";
    private static final long LISTENER_DELAY = 2000L;
    private static final long LISTENER_MAX_DELAY = 2000L;

    private final PasswordRegistry passwordRegistry;
    private final SystemContext systemContext;
    private WorkspaceEventListenerRegistration.Handle keystoreListenerRegistration;

    @Getter
    private String apiKey;

    @Inject
    public TokenProviderPasswords(PasswordRegistry passwordRegistry, SystemContext systemContext) {
        this.passwordRegistry = passwordRegistry;
        this.systemContext = systemContext;
    }

    @PostConstruct
    private void initApiKeyAndStartListenerOnKeyStoreWorkspace() {
        updateApiKeyFromPasswordManager(PASSWORDS_API_KEY_PATH);
        try {
            keystoreListenerRegistration = WorkspaceEventListenerRegistration
                    .observe(PasswordManagerModule.KEYSTORE_WORKSPACE, PASSWORDS_API_KEY_PATH,
                            events -> MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                                @Override
                                public void doExec() {
                                    updateApiKeyFromPasswordManager(PASSWORDS_API_KEY_PATH);
                                }
                            }, true))
                    .withNodeTypes(PasswordNodeTypes.Password.NAME)
                    .withDelay(LISTENER_DELAY, LISTENER_MAX_DELAY)
                    .register();
        } catch (RepositoryException e) {
            log.warn("Errors registering OpenAI api-key listener on \"{}\" in keystore workspace.", PASSWORDS_API_KEY_PATH, e);
        }
    }

    @PreDestroy
    public void unregisterKeyStoreListener() {
        if (keystoreListenerRegistration != null) {
            try {
                keystoreListenerRegistration.unregister();
            } catch (RepositoryException e) {
                log.warn("Errors unregistering OpenAI api-key listener in keystore workspace.");
            }
        }
    }

    @Override
    public String get() {
        return apiKey;
    }

    private void updateApiKeyFromPasswordManager(String apiKeyPasswordsPath) {
        apiKey = getApiKeyFromPasswordManager(apiKeyPasswordsPath).orElse(null);
    }

    private Optional<String> getApiKeyFromPasswordManager(String apiKeyPasswordsPath) {
        try {
            Session keyStoreSession = this.systemContext.getJCRSession(PasswordManagerModule.KEYSTORE_WORKSPACE);
            if (!keyStoreSession.nodeExists(apiKeyPasswordsPath)) {
                log.error("OpenAI api-key not found in \"{}\" of Password manager.", apiKeyPasswordsPath);
                return Optional.empty();
            }
            return Optional.of(passwordRegistry.getPassword(keyStoreSession.getNode(apiKeyPasswordsPath).getIdentifier()).getDecryptedValue());
        } catch (RepositoryException ex) {
            log.error("Errors when trying to get OpenAI api-key from {} in Password manager.", apiKeyPasswordsPath);
            throw new RuntimeRepositoryException("Errors when trying to get OpenAI api-key from Password manager.", ex);
        }
    }
}
