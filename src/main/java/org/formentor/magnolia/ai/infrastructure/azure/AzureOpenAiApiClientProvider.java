package org.formentor.magnolia.ai.infrastructure.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.AIContentsModule;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import java.util.function.Supplier;

@Slf4j
public class AzureOpenAiApiClientProvider implements Supplier<AzureOpenAiApi> {
    private final AIContentsModule definition;
    private final AzureApiKeyProvider apiKeyProvider;

    @Inject
    public AzureOpenAiApiClientProvider(AIContentsModule definition, AzureApiKeyProviderPasswords apiKeyProvider) {
        this.definition = definition;
        this.apiKeyProvider = apiKeyProvider;
    }

    @Override
    public AzureOpenAiApi get() {
        if (definition.getAzure() == null) {
            log.error("Missing configuration of Azure in ai-contents");
            return null;
        }

        UriBuilder FULL_PATH = UriBuilder.fromPath(buildTargeUri(definition.getAzure().getHost(), definition.getAzure().getResource(), definition.getAzure().getDeployment()));
        return ((ResteasyClient) ClientBuilder.newClient())
                    .target(FULL_PATH)
                    .register(new AuthenticatorFilter(apiKeyProvider.get()))
                    .register(buildJacksonProvider())
                    .proxy(AzureOpenAiApi.class);
    }

    private static class AuthenticatorFilter implements ClientRequestFilter {
        private final String token;
        public AuthenticatorFilter(String token) {
            this.token = token;
        }
        @Override
        public void filter(ClientRequestContext requestContext) {
            requestContext.getHeaders().add("api-key", token);
        }
    }

    private String buildTargeUri(String host, String resource, String deployment) {
        return String.format("https://%s.%s/openai/deployments/%s", resource, host, deployment);
    }

    protected ResteasyJackson2Provider buildJacksonProvider() {
        OpenAiResteasyJackson2Provider jackson2Provider = new OpenAiResteasyJackson2Provider();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2Provider.setMapper(mapper);

        return jackson2Provider;
    }

    private static class OpenAiResteasyJackson2Provider extends ResteasyJackson2Provider {
    }

}
