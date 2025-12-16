/*    */ package com.tcs.bancs.microservices.config;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.IOException;
/*    */ import java.util.Properties;
/*    */ import org.springframework.context.annotation.Bean;
/*    */ import org.springframework.context.annotation.Configuration;
/*    */ import org.springframework.context.annotation.PropertySource;
/*    */ import org.slf4j.Logger;
         import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @Configuration
/*    */ @PropertySource({"file:${ChannelsPropConfigPath}/Config/Config.properties"})
/*    */ public class CacheConfig
/*    */ {
	       static Logger logger = LoggerFactory.getLogger(CacheConfig.class);
/* 27 */   public static Properties frameworkConfigProperties = loadFrameworkConfigurations();
/*    */   
/*    */   public static String configPath;
/*    */   
/*    */   public static String channelsPropConfigPath;
/*    */   
/*    */   @Bean
/*    */   public static Properties loadFrameworkConfigurations() {
/* 35 */     FileInputStream input = null;
/* 36 */     Properties frameworkConfigProperties = new Properties();
/* 37 */     channelsPropConfigPath = System.getenv("ChannelsPropConfigPath");
/* 38 */     if (channelsPropConfigPath == null || channelsPropConfigPath.equalsIgnoreCase("")) {
/* 39 */       channelsPropConfigPath = System.getProperty("ChannelsPropConfigPath");
/*    */     }
/* 41 */     if (null == channelsPropConfigPath)
/*    */     {
/* 43 */       throw new NullPointerException();
/*    */     }
/*    */     
/*    */     try {
/* 47 */       configPath = channelsPropConfigPath + File.separator + "Config";
/* 48 */       input = new FileInputStream(configPath + File.separator + "Config.properties");
/* 49 */       frameworkConfigProperties.load(input);
/* 50 */       //System.out.println("Config.properties configuration successful ");
				logger.info("Config.properties configuration successful");
/*    */     }
/* 52 */     catch (IOException e) {
/*    */ 
/*    */       
/* 55 */       logger.error("Technical Exception occurred in CacheConfig");
/*    */     } finally {
/*    */       
/* 58 */       if (input != null) {
/* 59 */         safeClose(input);
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 64 */     return frameworkConfigProperties;
/*    */   }
/*    */ 
/*    */   
/*    */   public static void safeClose(FileInputStream fis) {
/* 69 */     if (fis != null)
/*    */       try {
/* 71 */         fis.close();
/* 72 */       } catch (IOException iOException) {} 
/*    */   }
/*    */ }

