package com.rehabai.prescription_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupValidator.class);

    private final Environment env;
    private final boolean useBedrock;
    private final String bedrockAccessKey;
    private final String bedrockSecretKey;

    public StartupValidator(Environment env,
                            @Value("${llm.useBedrock:false}") boolean useBedrock,
                            @Value("${BEDROCK_AWS_ACCESS_KEY_ID:}") String bedrockAccessKey,
                            @Value("${BEDROCK_AWS_SECRET_ACCESS_KEY:}") String bedrockSecretKey) {
        this.env = env;
        this.useBedrock = useBedrock;
        this.bedrockAccessKey = bedrockAccessKey;
        this.bedrockSecretKey = bedrockSecretKey;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (useBedrock) {
            boolean missingBedrockEnv = (bedrockAccessKey == null || bedrockAccessKey.isBlank()) || (bedrockSecretKey == null || bedrockSecretKey.isBlank());
            boolean hasAwsDefaultEnv = has(env, "AWS_ACCESS_KEY_ID") && has(env, "AWS_SECRET_ACCESS_KEY");
            if (missingBedrockEnv && !hasAwsDefaultEnv) {
                log.warn("USE_BEDROCK is enabled but no dedicated credentials (BEDROCK_AWS_*) nor default AWS_* env vars were found. " +
                        "Proceeding with DefaultCredentialsProvider chain (e.g., instance profile). If Bedrock calls fail, set BEDROCK_AWS_ACCESS_KEY_ID/SECRET in .env or disable USE_BEDROCK.");
            }
        }
    }

    private boolean has(Environment env, String key) {
        String v = env.getProperty(key);
        return v != null && !v.isBlank();
    }
}
