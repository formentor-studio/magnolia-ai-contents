package org.formentor.magnolia.ai.infrastructure.openai;

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
public class OpenAiApiClientProvider implements Supplier<OpenAiApi> {
    private final AIContentsModule definition;
    private final TokenProvider tokenProvider;

    @Inject
    public OpenAiApiClientProvider(AIContentsModule definition, TokenProvider tokenProvider) {
        this.definition = definition;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public OpenAiApi get() {
        if (definition.getOpenAI() == null) {
            log.error("Missing configuration of OpenAI");
            return null;
        }

        UriBuilder FULL_PATH = UriBuilder.fromPath(definition.getOpenAI().getHost());
        return ((ResteasyClient) ClientBuilder.newClient())
                    .target(FULL_PATH)
                    .register(new AuthenticatorFilter(tokenProvider.get()))
                    .register(buildJacksonProvider())
                    .proxy(OpenAiApi.class);
    }

    private static class AuthenticatorFilter implements ClientRequestFilter {
        private final String token;
        public AuthenticatorFilter(String token) {
            this.token = token;
        }
        @Override
        public void filter(ClientRequestContext requestContext) {
            requestContext.getHeaders().add("Authorization", "Bearer " + token);
        }
    }

    private ResteasyJackson2Provider buildJacksonProvider() {
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
