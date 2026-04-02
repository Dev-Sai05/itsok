 .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.6.RELEASE)

2026-04-02 16:19:30.305  INFO 32648 --- [           main] n.j.s.SpringbootKafkaTutorialApplication : Starting SpringbootKafkaTutorialApplication on GCHTCS0671 with PID 32648 (E:\My Work Folder\AI Inventions\springboot-kafka-tutorial\springbootkafkatutorial\bin started by V1010513 in E:\My Work Folder\AI Inventions\springboot-kafka-tutorial\springbootkafkatutorial)
2026-04-02 16:19:30.307  INFO 32648 --- [           main] n.j.s.SpringbootKafkaTutorialApplication : No active profile set, falling back to default profiles: default
2026-04-02 16:19:30.838  WARN 32648 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanDefinitionStoreException: Failed to process import candidates for configuration class [org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration]; nested exception is org.springframework.core.NestedIOException: ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet: class path resource [org/springframework/kafka/annotation/KafkaListenerConfigurationSelector.class]; nested exception is java.lang.IllegalArgumentException: Unsupported class file major version 61
2026-04-02 16:19:30.859  INFO 32648 --- [           main] ConditionEvaluationReportLoggingListener : 

Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
2026-04-02 16:19:30.864 ERROR 32648 --- [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanDefinitionStoreException: Failed to process import candidates for configuration class [org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration]; nested exception is org.springframework.core.NestedIOException: ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet: class path resource [org/springframework/kafka/annotation/KafkaListenerConfigurationSelector.class]; nested exception is java.lang.IllegalArgumentException: Unsupported class file major version 61
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:610) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:311) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:250) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:600) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.access$800(ConfigurationClassParser.java:111) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser$DeferredImportSelectorGroupingHandler.lambda$processGroupImports$1(ConfigurationClassParser.java:812) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511) ~[na:na]
	at org.springframework.context.annotation.ConfigurationClassParser$DeferredImportSelectorGroupingHandler.processGroupImports(ConfigurationClassParser.java:809) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser$DeferredImportSelectorHandler.process(ConfigurationClassParser.java:780) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:193) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:319) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:236) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:280) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:96) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:707) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:533) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:143) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:758) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:750) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:405) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:315) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1237) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1226) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at net.javaguides.springboot.SpringbootKafkaTutorialApplication.main(SpringbootKafkaTutorialApplication.java:10) ~[bin/:na]
Caused by: org.springframework.core.NestedIOException: ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet: class path resource [org/springframework/kafka/annotation/KafkaListenerConfigurationSelector.class]; nested exception is java.lang.IllegalArgumentException: Unsupported class file major version 61
	at org.springframework.core.type.classreading.SimpleMetadataReader.getClassReader(SimpleMetadataReader.java:60) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReader.<init>(SimpleMetadataReader.java:49) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:103) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory.createMetadataReader(ConcurrentReferenceCachingMetadataReaderFactory.java:86) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory.getMetadataReader(ConcurrentReferenceCachingMetadataReaderFactory.java:73) ~[spring-boot-2.3.6.RELEASE.jar:2.3.6.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:81) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.asSourceClass(ConfigurationClassParser.java:696) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser$SourceClass.getRelated(ConfigurationClassParser.java:1090) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser$SourceClass.getAnnotationAttributes(ConfigurationClassParser.java:1071) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.collectImports(ConfigurationClassParser.java:549) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.getImports(ConfigurationClassParser.java:522) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:311) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:250) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processMemberClasses(ConfigurationClassParser.java:372) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:272) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:250) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:600) ~[spring-context-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	... 23 common frames omitted
Caused by: java.lang.IllegalArgumentException: Unsupported class file major version 61
	at org.springframework.asm.ClassReader.<init>(ClassReader.java:196) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.asm.ClassReader.<init>(ClassReader.java:177) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.asm.ClassReader.<init>(ClassReader.java:163) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.asm.ClassReader.<init>(ClassReader.java:284) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReader.getClassReader(SimpleMetadataReader.java:57) ~[spring-core-5.2.11.RELEASE.jar:5.2.11.RELEASE]
	... 39 common frames omitted
