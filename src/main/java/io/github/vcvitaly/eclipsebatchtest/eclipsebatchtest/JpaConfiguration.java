package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

@Slf4j
@Configuration
@EnableConfigurationProperties({JpaProperties.class, HibernateProperties.class})
public class JpaConfiguration extends JpaBaseConfiguration {

    // This is temporal solution, that allows to determine Hibernate related configuration
    // and notify user that it should be replaced
    private static final String HIBERNATE_PROPERTIES_PREFIX = "hibernate.";
    private final HibernateProperties hibernateProperties;

    // Configuration properties
    private static final String ECLIPSELINK_PROPERTIES_PREFIX = "eclipselink.";
    private static final String ECLIPSELINK_PROPERTIES_SHOW_SQL = "eclipselink.show_sql";
    private static final String ECLIPSELINK_PROPERTIES_SHOW_SQL_PARAMETERS = "eclipselink.show_sql_parameters";
    private static final String JPA_SPEC_PROPERTIES_PREFIX = "jakarta.";


    protected JpaConfiguration(DataSource dataSource,
                               JpaProperties properties,
                               HibernateProperties hibernateProperties,
                               ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
        super(dataSource, properties, jtaTransactionManager);
        this.hibernateProperties = hibernateProperties;
    }

    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        checkHibernateProperties();
        validatePropertiesConfiguration();


        return new EclipseLinkJpaVendorAdapter();
    }

    @Override
    protected Map<String, Object> getVendorProperties() {
        Map<String, Object> map = new HashMap<>();

        TreeMap<String, String> configuredJpaProperties = new TreeMap<>(getProperties().getProperties());

        // Apply default values before user configuration.
        // These properties might be rewritten by application.yml properties
        map.put(PersistenceUnitProperties.WEAVING, detectWeavingMode());
        map.put(PersistenceUnitProperties.NATIVE_SQL, "true");
        map.put(PersistenceUnitProperties.SHARED_CACHE_MODE, "NONE");
        if (!getProperties().isGenerateDdl()) {
            map.put(PersistenceUnitProperties.DDL_GENERATION, "none");
        }
        map.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, CamelCaseSessionCustomizer.class.getName());
        map.put(PersistenceUnitProperties.TARGET_DATABASE,
                "org.eclipse.persistence.platform.database.PostgreSQLPlatform");


        if (Boolean.parseBoolean(configuredJpaProperties.get(ECLIPSELINK_PROPERTIES_SHOW_SQL))) {
            map.put("eclipselink.logging.level.sql", "FINE");
        }
        configuredJpaProperties.remove(ECLIPSELINK_PROPERTIES_SHOW_SQL);

        if (Boolean.parseBoolean(configuredJpaProperties.get(ECLIPSELINK_PROPERTIES_SHOW_SQL_PARAMETERS))) {
            map.put("eclipselink.logging.parameters", "true");
        }
        configuredJpaProperties.remove(ECLIPSELINK_PROPERTIES_SHOW_SQL_PARAMETERS);

        // apply user defined properties
        map.putAll(configuredJpaProperties.tailMap(ECLIPSELINK_PROPERTIES_PREFIX));
        map.putAll(configuredJpaProperties.tailMap(JPA_SPEC_PROPERTIES_PREFIX));

        return map;
    }

    private String detectWeavingMode() {
        return InstrumentationLoadTimeWeaver.isInstrumentationAvailable() ? "true" : "static";
    }

    private void validatePropertiesConfiguration() {
        Map<String, String> configuredJpaProperties = getProperties().getProperties();

        if (getProperties().isGenerateDdl()
                && configuredJpaProperties.containsKey(PersistenceUnitProperties.DDL_GENERATION)) {
            String ddlGenerationParamName = "spring.jpa.properties." + PersistenceUnitProperties.DDL_GENERATION;
            log.warn("Both 'spring.jpa.generateDdl' and {} has been used together.\n " +
                    "'spring.jpa.generateDdl' WILL BE FORCEFULLY CHANGED TO 'false'!", ddlGenerationParamName);
            getProperties().setGenerateDdl(false);
        }

    }

    private void checkHibernateProperties() {
        boolean isConfigurationError = false;

        if (hibernateProperties != null && hibernateProperties.getDdlAuto() != null) {
            isConfigurationError = true;

            String eclipselinkConfig = "spring.jpa.properties." + PersistenceUnitProperties.DDL_GENERATION;
            String hibernateConfig = "spring.hibernate.ddl-auto";
            String errMessage =
                    "Hibernate related configuration '{}'. It should be replaced with {}. \n For more details check {}";

            log.error(errMessage, hibernateConfig, eclipselinkConfig, PersistenceUnitProperties.class.getName());
        }

        Map<String, String> hibernateConfiguration =
                new TreeMap<>(getProperties().getProperties()).tailMap(HIBERNATE_PROPERTIES_PREFIX);

        if (!hibernateConfiguration.isEmpty()) {
            isConfigurationError = true;

            String errMessage =
                    "Hibernate related configurations: \n\t{}" +
                            "\nIt should be replaced with alternative JPA/Eclipselink configuration.\n" +
                            "For more details check {}";
            String unexpectedProperties = String.join("\n\t", hibernateConfiguration.keySet());

            log.error(errMessage, unexpectedProperties, PersistenceUnitProperties.class.getName());
        }

        if (isConfigurationError) {
            throw new IllegalArgumentException(
                    "Failed to setup JPA configuration. Illegal configuration properties provided.");
        }

    }


    // transforms snakeCase Table/Field names to camel_case
    public static class CamelCaseSessionCustomizer implements SessionCustomizer {
        @Override
        public void customize(Session session) throws SQLException {
            for (ClassDescriptor descriptor : session.getDescriptors().values()) {
//                // Only change the table name for non-embedable entities with no
                // @Table already
                if (!descriptor.getTables().isEmpty()
                        && descriptor.getAlias().equalsIgnoreCase(descriptor.getTableName())) {
                    String tableName = addUnderscores(descriptor.getTableName());
                    descriptor.setTableName(tableName);
//                    for (IndexDefinition index : descriptor.getTables().get(0).getIndexes()) {
//                        index.setTargetTable(tableName);
//                    }
                }
                for (DatabaseMapping mapping : descriptor.getMappings()) {
                    // Only change the column name for non-embedable entities with
                    // no @Column already
                    if (mapping.getField() != null && !mapping.getAttributeName().isEmpty()
                            && mapping.getField().getName().equalsIgnoreCase(mapping.getAttributeName())) {
                        mapping.getField().setName(addUnderscores(mapping.getAttributeName()));
                    }
                }
            }
        }

        private static String addUnderscores(String name) {
            StringBuilder buf = new StringBuilder(name.replace('.', '_'));
            for (int i = 1; i < buf.length() - 1; i++) {
                if (Character.isLowerCase(buf.charAt(i - 1)) && Character.isUpperCase(buf.charAt(i))
                        && Character.isLowerCase(buf.charAt(i + 1))) {
                    buf.insert(i++, '_');
                }
            }
            return buf.toString().toUpperCase();
        }

    }

}
