package com.example.rule.core;

import com.example.rule.model.Rule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieContainerSessionsPool;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.ConsequenceException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author minnxu
 */
@Component
@Slf4j
public class DroolsManager {

    private static final String SESSION_POSTFIX = "-session";
    private static final int SESSION_POOL_SIZE = 10;
    private static StatelessKieSession statelessKieSession;
    private static String kieBaseName = "kieBase-Min";
    private static final List<Rule> rules = new ArrayList<>();

    private static final KieServices kieServices = KieServices.Factory.get();

    // Kie file system, which needs to be cached.
    private static final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

    private static final KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

    // Globally there should be only one container.
    private static KieContainer kieContainer;
    private static KieContainerSessionsPool kieContainerSessionsPool;

    @PreDestroy
    public void destroyKieContainer() {
        if (kieContainer != null) {
            // All the session pools will be destroyed.
            kieContainer.dispose();
        }
    }

    /**
     * Check if the kbase exists
     */
    public boolean kieBaseExists() {
        if (kieContainer == null) {
            return false;
        }

        Collection<String> kieBaseNames = kieContainer.getKieBaseNames();
        if (kieBaseNames.contains(kieBaseName)) {
            return true;
        }
        log.info("Need to create KieBase: {}", kieBaseName);
        return false;
    }

    public void createOrUpdateRules(List<Rule> rules) {
        KieBaseModel kieBaseModel;
        final String packageName = "test";
        long startTime = System.currentTimeMillis();
        if (!kieBaseExists()) {
            log.info("To create a new Kie base model");
            kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName);
            kieBaseModel.addPackage(packageName);
            kieBaseModel.newKieSessionModel(kieBaseName + SESSION_POSTFIX);
            log.info("Finished to create a new Kie base model, cost {} ms", System.currentTimeMillis() - startTime);
        } else {
            log.info("To update an existing Kie base model");
            kieBaseModel = kieModuleModel.getKieBaseModels().get(kieBaseName);
            List<String> packages = kieBaseModel.getPackages();
            log.info("Existing packages: {}", packages);
            if (!packages.contains(packageName)) {
                kieBaseModel.addPackage(packageName);
                log.info("kieBase [{}] added a new package [{}]", kieBaseName, packageName);
            } else {
                kieBaseModel = null;
            }
            log.info("Finished to update the Kie model, cost {} ms", System.currentTimeMillis() - startTime);
        }

        startTime = System.currentTimeMillis();
        boolean needToBuild = false;
        for (Rule rule : rules) {
            String drlFile = String.format("src/main/resources/%s/%s.drl", packageName, rule.getId());
            byte[] content = kieFileSystem.read(drlFile);
            if (content == null || !Objects.equals(new String(content), rule.getDslContent())) {
                // Only load if it is new or any difference.
                kieFileSystem.write(drlFile, rule.getDslContent());
                needToBuild = true;
            }
        }

        if (kieBaseModel != null) {
            String kmoduleXml = kieModuleModel.toXML();
            log.info("Loading kmodule.xml: [\n{}]", kmoduleXml);
            kieFileSystem.writeKModuleXML(kmoduleXml);
        }
        log.info("Finished to write rules, cost {} ms", System.currentTimeMillis() - startTime);

        if (!needToBuild) {
            log.info("No any rule change!");
            return;
        }

        log.info("Start to build rules");
        startTime = System.currentTimeMillis();
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        log.info("Finished to build rules, cost {} ms", System.currentTimeMillis() - startTime);
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Level.ERROR)) {
            log.info("Failed to load the rule, please double check the content.");
            List<Message> messages = results.getMessages(Level.ERROR);
            for (Message message : messages) {
                log.error(message.getText());
            }
            log.error("Invalid rule!");
        }

        startTime = System.currentTimeMillis();
        if (kieContainer == null) {
            // KieContainer is created only once and always use it then.
            log.info("Start to create container");
            kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
            kieContainerSessionsPool = kieContainer.newKieSessionsPool(SESSION_POOL_SIZE);
            log.info("Finished to create container, cost {} ms", System.currentTimeMillis() - startTime);
        } else {
            // Dynamically update
            log.info("Start to update container");
            ((KieContainerImpl) kieContainer).updateToKieModule((InternalKieModule) kieBuilder.getKieModule());
            log.info("Finished to update container, cost {} ms", System.currentTimeMillis() - startTime);
        }
    }

    public void deleteRules(List<Rule> rules) {
        final String packageName = "test";
        if (kieBaseExists()) {
            KieBase kieBase = kieContainer.getKieBase(kieBaseName);
            rules.forEach(rule -> {
                try {
                    kieBase.removeRule(packageName, rule.getName());
                    // Remove drl from kieFileSystem
                    String drlFile = String.format("src/main/resources/%s/%s.drl", packageName, rule.getId());
                    kieFileSystem.delete(drlFile);
                    log.info("Deleted kieBase: [{}], package: [{}], rule: [{}]", kieBaseName, packageName,
                            rule.getName());
                } catch (IllegalArgumentException e) {
                    log.warn("Failed to delete rule [{}]: ", rule.getName(), e);
                }
            });
        }
    }

    private boolean verifyKieSession() {
        if (kieContainer == null || kieContainerSessionsPool == null || kieBaseName == null) {
            log.info("[verifyKieSession] It seems KIE is not ready... no rule loaded?");
            return false;
        }

        if (statelessKieSession == null) {
            statelessKieSession = kieContainerSessionsPool.newStatelessKieSession(kieBaseName + SESSION_POSTFIX);
        }

        if (statelessKieSession != null) {
            statelessKieSession.getKieBase().getKiePackages().forEach(
                    kiePackage -> {
                        log.info("[verifyKieSession] Rules in Kie Package: {}", kiePackage.getName());
                        kiePackage.getRules().forEach(rule -> log.info("{}", rule.getName()));
                    }
            );
        } else {
            return false;
        }

        return true;
    }

    public void setGlobals(Map<String, Object> globalMap) {
        if (!verifyKieSession()) {
            log.info("Failed to verify kie session");
            return;
        }

        if (!CollectionUtils.isEmpty(globalMap)) {
            globalMap.forEach((k, v) -> {
                if (statelessKieSession.getGlobals().getGlobalKeys().contains(k)) {
                    log.debug("The global [{}] already exists!", k);
                } else {
                    statelessKieSession.setGlobal(k, v);
                }
            });
        }
    }

    public void execute(Object object) {
        execute(Arrays.asList(object));
    }

    public void execute(List<Object> objects) {
        if (!verifyKieSession()) {
            log.info("Failed to verify kie session");
            return;
        }

        try {
            statelessKieSession.execute(objects);
        } catch (ConsequenceException e) {
            log.error("Error occurred: ", e);
        }
    }

}
