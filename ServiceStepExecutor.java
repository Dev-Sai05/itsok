/*     */ package com.tcs.bancs.microservices.aggregator;
/*     */ 
/*     */ import com.tcs.bancs.microservices.comparator.SubServiceStepComparator;
/*     */ import com.tcs.bancs.microservices.config.InputSourceType;
/*     */ import com.tcs.bancs.microservices.db.model.BaseModel;
/*     */ import com.tcs.bancs.microservices.domain.DefaultFieldValue;
/*     */ import com.tcs.bancs.microservices.domain.DefaultValues;
/*     */ import com.tcs.bancs.microservices.domain.NextOnEvent;
/*     */ import com.tcs.bancs.microservices.domain.NextSteps;
/*     */ import com.tcs.bancs.microservices.domain.ServiceField;
/*     */ import com.tcs.bancs.microservices.domain.ServiceStep;
/*     */ import com.tcs.bancs.microservices.domain.StepErrorHandling;
/*     */ import com.tcs.bancs.microservices.domain.StepInputField;
/*     */ import com.tcs.bancs.microservices.domain.StepOutput;
/*     */ import com.tcs.bancs.microservices.domain.StepProcess;
/*     */ import com.tcs.bancs.microservices.domain.SubServiceStep;
/*     */ import com.tcs.bancs.microservices.domain.SubServiceSteps;
/*     */ import com.tcs.bancs.microservices.exception.RepoImplExecutionException;
/*     */ import com.tcs.bancs.microservices.util.AggregationUtils;
/*     */ import com.tcs.bancs.microservices.util.ReflectionUtility;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Pattern;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.springframework.beans.factory.annotation.Autowired;
/*     */ import org.springframework.context.ApplicationContext;
/*     */ import org.springframework.context.annotation.ComponentScan;
/*     */ import org.springframework.stereotype.Component;
/*     */ import org.springframework.util.ReflectionUtils;
/*     */ 
/*     */ 
/*     */ 
/*     */ @ComponentScan(basePackages = {"com"})
/*     */ @Component
/*     */ public class ServiceStepExecutor
/*     */ {
/*  47 */   private static final Logger logger = LoggerFactory.getLogger(ServiceStepExecutor.class);
/*     */ 
/*     */ 
/*     */   
/*     */   @Autowired
/*     */   ApplicationContext context;
/*     */ 
/*     */   
/*  55 */   public String outStatus = "";
/*  56 */   public String outErrorMessage = "";
/*     */ 
/*     */   
/*     */   public int outErrorCode;
/*     */ 
/*     */ 
/*     */   
/*     */   public void executeServiceStep(ServiceStep serviceStep, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, boolean isRepeatStep, HashMap<String, ServiceStep> serviceStepMap, String collectionRef, HashMap<String, Object> stepCollectedRow, List<Object> modifiedList) throws IOException {
/*  64 */     long startTime = System.currentTimeMillis();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  78 */     logger.debug("Starting execution for step : " + serviceStep.getId());
/*     */     
/*  80 */     StepProcess stepProcess = serviceStep.getStepProcess();
/*  81 */     StepOutput stepOutput = serviceStep.getStepOutput();
/*  82 */     String repoName = stepProcess.getRepo_name();
/*  83 */     String repoFunction = stepProcess.getFunc_id();
/*  84 */     String stepOutputType = stepOutput.getType();
/*     */ 
/*     */     
/*  87 */     String repoBeanName = "com.tcs.bancs.microservices.db.model." + repoName.substring(0, repoName.indexOf("DetailsRepositoryImpl"));
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/*  92 */       logger.debug("Instantiating Repository Bean for Repo " + repoName + "with repoBean class name as " + repoBeanName);
/*     */ 
/*     */       
/*  95 */       Class<?> repoBeanClass = Class.forName(repoBeanName);
/*  96 */       Object dbModelBean = repoBeanClass.newInstance();
/*     */       
/*  98 */       Map<String, Object> idClassObjects = new HashMap<>();
/*  99 */       boolean isContinueToRepoImplExecution = false;
/*     */       
/*     */       try {
/* 102 */         logger.debug("Now initializing and preparing the input bean " + repoBeanName + " for the " + repoName + "repository impl call");
/* 103 */         formDbModelBeanFromStepInput(serviceStep, dbModelBean, serviceFieldMap, collectedRow, idClassObjects, stepCollectedRow);
/*     */         
/* 105 */         isContinueToRepoImplExecution = true;
/* 106 */       } catch (Exception e) {
/* 107 */         logger.error("Error encountered while forming input bean for service step " + serviceStep.getId());
/* 108 */         this.outStatus = "EXCEPTION";
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 113 */       Object returnBeanObj = null;
/* 114 */       boolean isContinueToOutputFormation = false;
/* 115 */       if (isContinueToRepoImplExecution) {
/*     */         
/*     */         try {
/*     */ 
/*     */ 
/*     */           
/* 121 */           returnBeanObj = callRepoImplForStep(serviceStep, dbModelBean, repoName, repoFunction);
/* 122 */           isContinueToOutputFormation = true;
/* 123 */         } catch (InvocationTargetException e) {
/*     */           
/* 125 */           if (e.getCause() instanceof RepoImplExecutionException) {
/*     */             
/* 127 */             RepoImplExecutionException rie = (RepoImplExecutionException)e.getCause();
/*     */             
/* 129 */             String errorType = rie.getErrorType();
/*     */             
/* 131 */             if (!"ERROR".equals(errorType) && !errorType.contains("ERR")) {
/* 132 */               isContinueToOutputFormation = true;
/*     */             } else {
/* 134 */               this.outStatus = errorType;
/* 135 */               this.outErrorMessage = rie.getErrorMessage();
/* 136 */               this.outErrorCode = Integer.parseInt(rie.getErrorCode());
/*     */             }
/*     */           
/*     */           } else {
/*     */             
/* 141 */             logger.error("Error encounterd while executing " + repoName + " for service step " + serviceStep
/* 142 */                 .getId());
/* 143 */             this.outStatus = "EXCEPTION";
/*     */           }
/*     */         
/*     */         }
/* 147 */         catch (RepoImplExecutionException rie) {
/*     */           
/* 149 */           String errorType = rie.getErrorType();
/*     */           
/* 151 */           if (!"ERROR".equals(errorType)) {
/* 152 */             isContinueToOutputFormation = true;
/*     */           } else {
/* 154 */             this.outStatus = errorType;
/* 155 */             this.outErrorMessage = rie.getErrorMessage();
/* 156 */             this.outErrorCode = Integer.parseInt(rie.getErrorCode());
/*     */           } 
/* 158 */         } catch (Exception e) {
/* 159 */           logger.error("Error encounterd while executing " + repoName + " for service step " + serviceStep
/* 160 */               .getId());
/* 162 */           this.outStatus = "EXCEPTION";
/*     */         } 
/*     */       }
/*     */       
/* 166 */       boolean isErrorInSettingStepOutputToService = false;
/*     */       
/* 168 */       if (isContinueToOutputFormation) {
/*     */         try {
/* 170 */           boolean setValue = false;
/*     */ 
/*     */           
/* 173 */           if (returnBeanObj instanceof BaseModel) {
/* 174 */             setValue = true;
/* 175 */           } else if (returnBeanObj instanceof List && !((List)returnBeanObj).isEmpty() && (
/* 176 */             (List)returnBeanObj).get(0) instanceof BaseModel) {
/* 177 */             setValue = true;
/*     */           } 
/*     */ 
/*     */           
/* 181 */           if (setValue) {
/* 182 */             setStepOutputToService(returnBeanObj, stepOutputType, serviceStep, isRepeatStep, collectionRef, serviceFieldMap, collectedRow, stepCollectedRow, serviceStepMap, modifiedList);
/*     */           } else {
/*     */             
/* 185 */             throw new Exception("Unsupported repository db model bean class" + returnBeanObj.getClass());
/*     */           }
/*     */         
/*     */         }
/* 189 */         catch (Exception e) {
/*     */           
/* 191 */           logger.error("Error encountered while setting step output to the service context due to exception ");
/* 192 */           this.outStatus = "EXCEPTION";
/* 193 */           isErrorInSettingStepOutputToService = true;
/*     */         } 
/*     */       }
/*     */       
/* 197 */       executeSubStepsAndNextSteps(serviceFieldMap, collectedRow, collectionRef, stepOutputType, isRepeatStep, serviceStep, this.outStatus, isErrorInSettingStepOutputToService, isContinueToOutputFormation, serviceStepMap, modifiedList, stepCollectedRow);
/*     */ 
/*     */     
/*     */     }
/* 201 */     catch (ClassNotFoundException cnfe) {
/* 202 */       logger.error("Repository Bean class not found " + repoBeanName);
/* 203 */       logger.error("Class Not Found Exception at LineNumber ");
/* 204 */     } catch (InstantiationException ie) {
/*     */ 
/*     */       
/* 207 */       logger.error("Error occurred in executeServiceStep due to InstantiationException Exception");
/* 208 */     } catch (IllegalAccessException iae) {
/*     */       
/* 210 */       logger.error("Error occurred in executeServiceStep due to IllegalAccessException Exception");
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void executeSubStepsAndNextSteps(HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, String collectionRef, String stepOutputType, boolean isRepeatStep, ServiceStep serviceStep, String outStatus, boolean isErrorInSettingStepOutputToService, boolean isContinueToOutputFormation, HashMap<String, ServiceStep> serviceStepMap, List<Object> modifiedList, HashMap<String, Object> stepCollectedRow) {
/*     */     try {
/* 225 */       NextSteps nextServiceSteps = serviceStep.getNextSteps();
/* 226 */       List<NextOnEvent> nextOnEvents = nextServiceSteps.getNextOnEvents();
/*     */       
/* 228 */       NextOnEvent nextOnEvent = null;
/* 229 */       NextOnEvent nextOnEventAnyMatch = null;
/*     */       
/* 231 */       for (NextOnEvent onEvent : nextOnEvents) {
/* 232 */         String onExitStatus = onEvent.getOnExitStatus();
/* 233 */         if (onExitStatus.equals("*")) {
/*     */           
/* 235 */           nextOnEventAnyMatch = onEvent;
/*     */           continue;
/*     */         } 
/* 238 */         if (onExitStatus != null && !"".equals(onExitStatus)) {
/* 239 */           if (onExitStatus.contains("*"))
/* 240 */             onExitStatus = onExitStatus.replaceAll("\\*", ".*"); 
/* 241 */           Pattern matchPattern = Pattern.compile(onExitStatus);
/* 242 */           if (matchPattern.matcher(outStatus).matches()) {
/* 243 */             nextOnEvent = onEvent;
/*     */             
/*     */             break;
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 250 */       nextOnEvent = (nextOnEvent == null) ? ((nextOnEventAnyMatch == null) ? new NextOnEvent() : nextOnEventAnyMatch) : nextOnEvent;
/*     */ 
/*     */       
/* 253 */       HashMap<String, Object> subStepFieldMap = new HashMap<>();
/*     */       
/* 255 */       StepErrorHandling stepErrorHandling = nextOnEvent.getStepErrorHandling();
/* 256 */       if (stepErrorHandling != null && (isErrorInSettingStepOutputToService || !isContinueToOutputFormation)) {
/* 257 */         DefaultValues defaultValues = stepErrorHandling.getDefaultValues();
/* 258 */         if (defaultValues != null) {
/* 259 */           LinkedList<DefaultFieldValue> defaultFieldValueList = defaultValues.getDefaultFieldValue();
/* 260 */           if (defaultFieldValueList != null && !defaultFieldValueList.isEmpty()) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 267 */             ArrayList<Object> stepCollectionOutFieldList = new ArrayList();
/*     */             
/* 269 */             if (!"list".equals(stepOutputType)) {
/* 270 */               for (DefaultFieldValue defaultFieldValue : defaultFieldValueList) {
/* 271 */                 if (isRepeatStep) {
/* 272 */                   subStepFieldMap.put(defaultFieldValue.getFieldName(), defaultFieldValue
/* 273 */                       .getDefaultValue()); continue;
/*     */                 } 
/* 275 */                 serviceFieldMap.put(defaultFieldValue.getFieldName(), defaultFieldValue
/* 276 */                     .getDefaultValue());
/*     */               } 
/* 278 */               if (isRepeatStep) {
/* 279 */                 collectedRow.put("FieldMap" + ((collectionRef == null || collectionRef
/* 280 */                     .equals("")) ? serviceStep.getId() : collectionRef), subStepFieldMap);
/*     */ 
/*     */                 
/* 283 */                 stepCollectedRow.put("FieldMap" + ((collectionRef == null || collectionRef.equals("")) ? serviceStep.getId() : collectionRef), subStepFieldMap);
/*     */               }
/*     */             
/*     */             }
/*     */             else {
/*     */               
/* 289 */               HashMap<String, Object> outFieldMapRow = new HashMap<>();
/*     */               
/* 291 */               for (DefaultFieldValue defaultFieldValue : defaultFieldValueList) {
/* 292 */                 outFieldMapRow.put(defaultFieldValue.getFieldName(), defaultFieldValue
/* 293 */                     .getDefaultValue());
/*     */               }
/* 295 */               stepCollectionOutFieldList.add(outFieldMapRow);
/*     */               
/* 297 */               if (isRepeatStep) {
/* 298 */                 collectedRow.put("FieldMap" + ((collectionRef == null || collectionRef
/* 299 */                     .equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */ 
/*     */                 
/* 302 */                 stepCollectedRow.put("FieldMap" + ((collectionRef == null || collectionRef.equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */               }
/*     */               else {
/*     */                 
/* 306 */                 modifiedList.add(stepCollectionOutFieldList);
/*     */                 
/* 308 */                 serviceFieldMap.put("Collection" + ((collectionRef == null || collectionRef
/* 309 */                     .equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */               } 
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 328 */       String toStep = nextOnEvent.getToStep();
/*     */ 
/*     */       
/* 331 */       SubServiceSteps subServiceSteps = nextOnEvent.getSubServiceSteps();
/*     */       
/* 333 */       List<SubServiceStep> subServiceStepsList = (subServiceSteps == null) ? null : subServiceSteps.getSubServiceStepList();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 339 */       if (null != subServiceStepsList && !subServiceStepsList.isEmpty() && !isRepeatStep) {
/*     */ 
/*     */ 
/*     */         
/* 343 */         processSubServiceSteps(subServiceStepsList, serviceFieldMap, serviceStep, serviceStepMap, collectedRow, modifiedList);
/*     */ 
/*     */ 
/*     */       
/*     */       }
/* 348 */       else if (null != subServiceStepsList && !subServiceStepsList.isEmpty() && isRepeatStep == true) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 355 */         for (int i = 0; i < subServiceStepsList.size(); i++) {
/*     */ 
/*     */ 
/*     */           
/* 359 */           for (SubServiceStep subservice : subServiceStepsList) {
/*     */ 
/*     */             
/* 362 */             if (!serviceStep.getId().equalsIgnoreCase(subservice.getCollectionref()))
/*     */             {
/*     */ 
/*     */               
/* 366 */               executeSubStepService(subServiceStepsList, serviceStep, serviceStepMap, serviceFieldMap, collectedRow, true, modifiedList);
/*     */             }
/*     */           } 
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 391 */       if (nextOnEvent.getOnExitStatus().equalsIgnoreCase("SUCCESS") && toStep != null && (toStep.equals("") || toStep.equalsIgnoreCase("OUTPUT"))) {
/*     */         
/* 393 */         logger.debug("No further steps are available to process. Exiting the step execution process and moving to output formation");
/*     */ 
/*     */       
/*     */       }
/* 397 */       else if (toStep != null && !toStep.equals("") && !toStep.equalsIgnoreCase("OUTPUT")) {
/* 398 */         ServiceStep nextStepObj = serviceStepMap.get(toStep);
/* 399 */         if (nextStepObj != null)
/*     */         {
/*     */           
/* 402 */           executeServiceStep(nextStepObj, serviceFieldMap, collectedRow, false, serviceStepMap, null, subStepFieldMap, new ArrayList());
/*     */         
/*     */         }
/*     */       }
/* 406 */       else if (!nextOnEvent.getOnExitStatus().equalsIgnoreCase("SUCCESS") && toStep != null && !toStep.equals("") && toStep.equalsIgnoreCase("OUTPUT")) {
/*     */         
/* 408 */         serviceFieldMap.put("ERROR_MESSAGE", this.outErrorMessage);
/* 409 */         serviceFieldMap.put("ERROR_CODE", Integer.valueOf(this.outErrorCode));
/* 410 */         logger.debug("No further steps are available to process. Exiting the step execution process and moving to output formation");
/*     */       
/*     */       }
/*     */     
/*     */     }
/* 415 */     catch (Exception e) {
/* 416 */       logger.error("Error occurred in executeSubStepsAndNextSteps due to  Exception ");
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void setStepOutputToService(Object returnBeanObj, String stepOutputType, ServiceStep serviceStep, boolean isRepeatStep, String collectionRef, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, HashMap<String, Object> stepCollectedRow, HashMap<String, ServiceStep> serviceStepMap, List<Object> modifiedList) throws Exception {
/*     */     try {
/* 436 */       List<ServiceField> serviceFieldList = serviceStep.getStepOutput().getServiceFieldsList();
/* 437 */       HashMap<String, Object> subStepFieldMap = new HashMap<>();
/*     */       
/* 439 */       if (!"list".equals(stepOutputType)) {
/* 440 */         BaseModel dbModelBean = (BaseModel)returnBeanObj;
/*     */         
/* 442 */         this.outStatus = dbModelBean.getExitStatus();
/* 443 */         this.outStatus = (this.outStatus == null) ? "SUCCESS" : this.outStatus;
/*     */         
/* 445 */         for (ServiceField serviceField : serviceFieldList) {
/* 446 */           String repoFieldName = serviceField.getRepo_field_name();
/* 447 */           String serviceFieldName = serviceField.getService_field_name();
/* 448 */           String outObjType = serviceField.getValue_type();
/* 449 */           String postProcess = serviceField.getPost_process();
/*     */           
/* 451 */           Object outFieldObj = null;
/* 452 */           if (repoFieldName.contains(".")) {
/* 453 */             String[] tokens = repoFieldName.split("\\.");
/* 454 */             String idClass = tokens[0];
/* 455 */             String idRepoFieldName = tokens[1];
/*     */             
/* 457 */             Object idClassOutInstance = null;
/*     */ 
/*     */             
/*     */             try {
/* 461 */               idClassOutInstance = ReflectionUtility.invokeGetter(dbModelBean.getClass(), dbModelBean, idClass);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             }
/* 469 */             catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */               
/* 471 */               logger.error("Error in executing getter to fetch value from the repository bean for step " + serviceStep
/* 472 */                   .getId() + " and method " + idClass);
/* 473 */               throw e;
/*     */             } 
/*     */ 
/*     */             
/*     */             try {
/* 478 */               outFieldObj = ReflectionUtility.invokeGetter(idClassOutInstance.getClass(), idClassOutInstance, idRepoFieldName);
/*     */             
/*     */             }
/* 481 */             catch (NoSuchMethodException|SecurityException e) {
/*     */               
/* 483 */               logger.error("The Getter method does not exist. For step " + serviceStep.getId() + " and method " + idRepoFieldName );
/* 485 */               throw e;
/* 486 */             } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */               
/* 488 */               logger.error("Error while executing the getter method for step " + serviceStep.getId() + " and method " + idRepoFieldName);
/* 490 */               throw e;
/*     */             } 
/*     */           } else {
/*     */ 
/*     */             
/*     */             try {
/*     */ 
/*     */               
/* 498 */               outFieldObj = ReflectionUtility.invokeGetter(dbModelBean.getClass(), dbModelBean, repoFieldName);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             }
/* 507 */             catch (NoSuchMethodException|SecurityException e) {
/*     */               
/* 509 */               logger.error("The Getter method does not exist. For step " + serviceStep.getId() + " and method " + repoFieldName);
/* 511 */               throw e;
/* 512 */             } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */               
/* 514 */               logger.error("Error while executing the getter method for step " + serviceStep.getId() + " and method " + repoFieldName);
/* 516 */               throw e;
/*     */             } 
/*     */           } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 525 */           if (isRepeatStep) {
/* 526 */             subStepFieldMap.put(serviceFieldName, outFieldObj);
/*     */           } else {
/* 528 */             serviceFieldMap.put(serviceFieldName, outFieldObj);
/*     */           } 
/* 530 */           if (postProcess != null && postProcess.contains("MicroservicesUtil")) {
/* 531 */             Object returnObj = AggregationUtils.callUtilityFromText(postProcess, serviceFieldMap, subStepFieldMap, collectedRow);
/*     */             
/* 533 */             if (isRepeatStep) {
/* 534 */               subStepFieldMap.put(serviceFieldName, returnObj); continue;
/*     */             } 
/* 536 */             serviceFieldMap.put(serviceFieldName, returnObj);
/*     */           } 
/*     */         } 
/*     */         
/* 540 */         if (isRepeatStep) {
/* 541 */           collectedRow.put("FieldMap" + ((collectionRef == null || collectionRef
/* 542 */               .equals("")) ? serviceStep.getId() : collectionRef), subStepFieldMap);
/*     */ 
/*     */           
/* 545 */           stepCollectedRow.put("FieldMap" + ((collectionRef == null || collectionRef
/* 546 */               .equals("")) ? serviceStep.getId() : collectionRef), subStepFieldMap);
/*     */         }
/*     */       
/*     */       }
/*     */       else {
/*     */         
/* 552 */         List<BaseModel> dbModelBeanList = (List<BaseModel>)returnBeanObj;
/* 553 */         HashMap<Integer, Object> stepCollectionOutFieldMap = new HashMap<>();
/* 554 */         ArrayList<Object> stepCollectionOutFieldList = new ArrayList();
/*     */         
/* 556 */         this.outStatus = ((BaseModel)dbModelBeanList.get(0)).getExitStatus();
/* 557 */         this.outStatus = (this.outStatus == null) ? "SUCCESS" : this.outStatus;
/*     */ 
/*     */ 
/*     */         
/* 561 */         for (BaseModel oo : dbModelBeanList) {
/*     */           
/* 563 */           HashMap<String, Object> outFieldMapRow = new HashMap<>();
/*     */           
/* 565 */           for (ServiceField serviceField : serviceFieldList) {
/* 566 */             String repoFieldName = serviceField.getRepo_field_name();
/* 567 */             String serviceFieldName = serviceField.getService_field_name();
/* 568 */             String outObjType = serviceField.getValue_type();
/* 569 */             String postProcess = serviceField.getPost_process();
/*     */             
/* 571 */             Object outFieldObj = null;
/*     */             
/* 573 */             if (repoFieldName.contains(".")) {
/* 574 */               String[] tokens = repoFieldName.split("\\.");
/* 575 */               String idClass = tokens[0];
/* 576 */               String idRepoFieldName = tokens[1];
/*     */               
/* 578 */               Object idClassOutInstance = null;
/*     */ 
/*     */               
/*     */               try {
/* 582 */                 idClassOutInstance = ReflectionUtility.invokeGetter(oo.getClass(), oo, idClass);
/*     */               }
/* 584 */               catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */                 
/* 586 */                 logger.error("Error in executing getter to fetch value from the repository bean for step " + serviceStep
/*     */                     
/* 588 */                     .getId() + " and method " + idClass);
/* 590 */                 throw e;
/*     */               } 
/*     */               
/*     */               try {
/* 594 */                 outFieldObj = ReflectionUtility.invokeGetter(idClassOutInstance.getClass(), idClassOutInstance, idRepoFieldName);
/*     */               
/*     */               }
/* 597 */               catch (NoSuchMethodException|SecurityException e) {
/*     */                 
/* 599 */                 logger.error("The Getter method does not exist. For step " + serviceStep.getId() + " and method " + idRepoFieldName );
/* 601 */                 throw e;
/* 602 */               } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */                 
/* 604 */                 logger.error("Error while executing the getter method for step " + serviceStep.getId() + " and method " + idRepoFieldName );
/* 606 */                 throw e;
/*     */               } 
/*     */             } else {
/*     */ 
/*     */               
/*     */               try {
/*     */ 
/*     */                 
/* 614 */                 outFieldObj = ReflectionUtility.invokeGetter(oo.getClass(), oo, repoFieldName);
/*     */               }
/* 616 */               catch (NoSuchMethodException|SecurityException e) {
/*     */                 
/* 618 */                 logger.error("The Getter method does not exist. For step " + serviceStep.getId() + " and method " + repoFieldName );
/* 620 */                 throw e;
/* 621 */               } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */                 
/* 623 */                 logger.error("Error while executing the getter method for step " + serviceStep.getId() + " and method " + repoFieldName );
/* 625 */                 throw e;
/*     */               } 
/*     */             } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 634 */             if (outObjType.equals(InputSourceType.CONSTANT.toString())) {
/* 635 */               outFieldObj = postProcess;
/*     */             }
/*     */             
/* 638 */             if (outFieldObj == null || !outFieldObj.getClass().getName().equals(outObjType));
/*     */ 
/*     */ 
/*     */             
/* 642 */             outFieldMapRow.put(serviceFieldName, outFieldObj);
/*     */             
/* 644 */             if (isRepeatStep)
/*     */             {
/* 646 */               stepCollectedRow.put(serviceFieldName, outFieldObj);
/*     */             }
/*     */ 
/*     */             
/* 650 */             if (postProcess != null && postProcess.contains("MicroservicesUtil")) {
/* 651 */               Object returnObj = AggregationUtils.callUtilityFromText(postProcess, serviceFieldMap, outFieldMapRow, null);
/*     */               
/* 653 */               outFieldMapRow.put(serviceFieldName, returnObj);
/*     */             } 
/*     */           } 
/*     */ 
/*     */           
/* 658 */           stepCollectionOutFieldList.add(outFieldMapRow);
/*     */         } 
/*     */ 
/*     */         
/* 662 */         if (isRepeatStep) {
/* 663 */           collectedRow.put("Collection" + ((collectionRef == null || collectionRef
/* 664 */               .equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */ 
/*     */ 
/*     */           
/* 668 */           stepCollectedRow.put("Collection" + ((collectionRef == null || collectionRef
/* 669 */               .equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */         
/*     */         }
/*     */         else {
/*     */           
/* 674 */          // System.out.println("First Iteration List" + stepCollectionOutFieldList);
/*     */           logger.info("First Iteration");
/* 676 */           modifiedList.add(stepCollectionOutFieldList);
/*     */           
/* 678 */           serviceFieldMap.put("Collection" + ((collectionRef == null || collectionRef
/* 679 */               .equals("")) ? serviceStep.getId() : collectionRef), stepCollectionOutFieldList);
/*     */         
/*     */         }
/*     */ 
/*     */       
/*     */       }
/*     */ 
/*     */     
/*     */     }
/* 688 */     catch (Exception e) {
/* 689 */       throw e;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object callRepoImplForStep(ServiceStep serviceStep, Object dbModelBean, String repoName, String repoFunction) throws Exception {
/*     */     try {
/* 704 */       String repoContextName = repoName.substring(0, 1).toLowerCase() + repoName.substring(1);
/*     */       
/* 706 */       Object repoClassIns = this.context.containsBean(repoContextName) ? this.context.getBean(repoContextName) : null;
/*     */       
/* 708 */       if (repoClassIns == null) {
/* 709 */         logger.error("Repository impl class is not loaded into the context. Can not instantiate");
/* 710 */         throw new Exception("Repository impl class is not loaded into the context. Can not instantiate");
/*     */       } 
/*     */       
/* 713 */       Class<?> repoClass = repoClassIns.getClass();
/*     */       
/* 715 */       Method repoImplMethod = repoClass.getDeclaredMethod(repoFunction, new Class[] { dbModelBean.getClass() });
/* 716 */       Object returnBean = repoImplMethod.invoke(repoClassIns, new Object[] { dbModelBean });
/*     */ 
/*     */ 
/*     */       
/* 720 */       return returnBean;
/*     */     }
/* 722 */     catch (RepoImplExecutionException rie) {
/*     */       
/* 724 */       logger.error(repoName + "Repository Impl Execution failed " + serviceStep.getId());
/* 725 */       throw rie;
/*     */     }
/* 727 */     catch (NoSuchMethodException|SecurityException e) {
/* 728 */       logger.error(repoFunction + "Repository Impl method is not found or is not accessible for service step " + serviceStep
/* 729 */           .getId());
/* 731 */       throw e;
/* 732 */     } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/*     */       
/* 734 */       logger.error(repoName + "  " + repoFunction + " method execution error for service step " + serviceStep.getId());
/* 735 */       throw e;
/* 736 */     } catch (ClassNotFoundException cnfe) {
/*     */       
/* 738 */       logger.error(repoName + " Class Not found for service Step " + serviceStep.getId());
/* 739 */       throw cnfe;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void formDbModelBeanFromStepInput(ServiceStep serviceStep, Object dbModelBean, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, Map<String, Object> idClassObjects, HashMap<String, Object> stepCollectedRow) throws Exception {
/*     */     try {
/* 751 */       Class<?> repoBeanClass = dbModelBean.getClass();
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 756 */       for (StepInputField stepInputField : serviceStep.getStepInput().getStepInputFieldList()) {
/*     */         try {
/* 758 */           String valueType = stepInputField.getValue_type();
/* 759 */           String repoFieldName = stepInputField.getRepo_field_name();
/*     */           
/* 761 */           Object fieldValue = null;
/*     */           
/* 763 */           String value = stepInputField.getValue();
/*     */           
/* 765 */           if (valueType.equals(InputSourceType.CONSTANT.toString())) {
/* 766 */             fieldValue = value;
/* 767 */           } else if (valueType.equals(InputSourceType.SERVICE_CONTEXT.toString())) {
/* 768 */             if (value.contains("Collection"))
/* 769 */             { String[] tokens = value.split("\\.");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 776 */               fieldValue = (collectedRow != null) ? (collectedRow.containsKey(tokens[1]) ? collectedRow.get(tokens[1]) : ((stepCollectedRow != null) ? (stepCollectedRow.containsKey(tokens[1]) ? stepCollectedRow.get(tokens[1]) : (serviceFieldMap.containsKey(tokens[1]) ? serviceFieldMap.get(tokens[1]) : "")) : "")) : "";
/*     */                }
/*     */             
/*     */             else
/*     */             
/*     */             { 
/* 782 */               fieldValue = serviceFieldMap.containsKey(value) ? serviceFieldMap.get(value) : ""; } 
/* 783 */           } else if (valueType.equals(InputSourceType.UTILITY.toString())) {
/* 784 */             Object val = AggregationUtils.callUtilityFromText(value, serviceFieldMap, stepCollectedRow, collectedRow);
/* 785 */             fieldValue = val;
/*     */           } 
/*     */           
/* 788 */           if (repoFieldName.contains(".")) {
/*     */             
/* 790 */             String[] tokens = repoFieldName.split("\\.");
/* 791 */             String idClass = tokens[0];
/* 792 */             String idRepoFieldName = tokens[1];
/*     */             
/* 794 */             Object idClassInstance = null;
/*     */             
/* 796 */             if (idClassObjects.containsKey(idClass)) {
/* 797 */               idClassInstance = idClassObjects.get(idClass);
/*     */             } else {
/* 799 */               Field idClassField = repoBeanClass.getDeclaredField(idClass);
/*     */               
/* 801 */               ReflectionUtils.makeAccessible(idClassField);
/* 802 */               idClassInstance = idClassField.getType().newInstance();
/*     */             } 
/*     */ 
/*     */             
/* 806 */             ReflectionUtility.invokeSetter(idClassInstance.getClass(), idClassInstance, idRepoFieldName, fieldValue);
/*     */ 
/*     */             
/* 809 */             idClassObjects.put(idClass, idClassInstance);
/*     */             
/* 811 */             ReflectionUtility.invokeSetter(repoBeanClass, dbModelBean, idClass, idClassInstance);
/*     */ 
/*     */ 
/*     */             
/*     */             continue;
/*     */           } 
/*     */ 
/*     */           
/* 819 */           ReflectionUtility.invokeSetter(repoBeanClass, dbModelBean, repoFieldName, fieldValue);
/*     */         
/*     */         }
/* 822 */         catch (InstantiationException e) {
/*     */           
/* 824 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to InstantiationException Exception");
/* 825 */           throw e;
/* 826 */         } catch (IllegalAccessException e) {
/*     */           
/* 828 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to IllegalAccessException Exception");
/* 829 */           throw e;
/* 830 */         } catch (NoSuchFieldException e) {
/*     */           
/* 832 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to NoSuchMethodException Exception");
/* 833 */           throw e;
/* 834 */         } catch (SecurityException e) {
/*     */           
/* 836 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to SecurityException Exception");
/* 837 */           throw e;
/* 838 */         } catch (NoSuchMethodException e) {
/*     */           
/* 840 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to NoSuchMethodException Exception");
/* 841 */           throw e;
/* 842 */         } catch (IllegalArgumentException e) {
/*     */           
/* 844 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to IllegalArgumentException Exception");
/* 845 */           throw e;
/* 846 */         } catch (InvocationTargetException e) {
/*     */           
/* 848 */           logger.error("Error occurred in formDbModelBeanFromStepInput due to InvocationTargetException Exception");
/* 849 */           throw e;
/*     */         
/*     */         }
/*     */ 
/*     */       
/*     */       }
/*     */     
/*     */     }
/* 857 */     catch (Exception e) {
/* 858 */       logger.error("Error while forming input bean for service step " + serviceStep.getId() );
/*     */ 
/*     */       
/* 861 */       String exitStatus = "EXCEPTION";
/* 862 */       Method methodR = BaseModel.class.getDeclaredMethod("setExitStatus", new Class[] { exitStatus.getClass() });
/* 863 */       methodR.invoke(dbModelBean, new Object[] { exitStatus });
/*     */       
/* 865 */       throw e;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void processSubServiceSteps(List<SubServiceStep> subServiceStepsList, HashMap<String, Object> serviceFieldMap, ServiceStep serviceStep, HashMap<String, ServiceStep> serviceStepMap, HashMap<String, Object> collectedRow, List<Object> modifiedList) throws Exception {
/*     */     try {
/* 880 */       logger.debug("Processing steps in their order of execution");
/*     */ 
/*     */       
/* 883 */       HashMap<String, SubServiceStep> subServiceStepMap = new HashMap<>();
/* 884 */       Collections.sort(subServiceStepsList, (Comparator<? super SubServiceStep>)new SubServiceStepComparator());
/*     */       
/* 886 */       for (SubServiceStep subServiceStep : subServiceStepsList) {
/* 887 */         subServiceStepMap.put(subServiceStep.getSubStepRef(), subServiceStep);
/*     */       }
/*     */       
/* 890 */       logger.debug("Service Step Map is --> " + subServiceStepMap);
/*     */       
/* 892 */       for (SubServiceStep subServiceStep : subServiceStepsList)
/*     */       {
/* 894 */         if (subServiceStep.getSubStepOrder() == null || subServiceStep.getSubStepOrder().equals("")) {
/*     */           continue;
/*     */         }
/* 897 */         executeSubStepService(subServiceStepsList, serviceStep, serviceStepMap, serviceFieldMap, collectedRow, false, modifiedList);
/*     */       }
/*     */     
/*     */     }
/* 901 */     catch (Exception e) {
/* 902 */       logger.error("Error while processing subservice step " + subServiceStepsList);
/*     */       
/* 904 */       throw e;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private List<Object> executeSubStepService(List<SubServiceStep> subServiceStepsList, ServiceStep serviceStep, HashMap<String, ServiceStep> serviceStepMap, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, boolean isRepeatColl, List<Object> modifiedStepCollectionList) throws Exception {
/*     */     try {
/* 920 */       List<SubServiceStep> subServiceStepList = subServiceStepsList;
/*     */ 
/*     */       
/* 923 */       for (SubServiceStep subServiceStep : subServiceStepList) {
/* 924 */         String stepName = subServiceStep.getSubStepRef();
/* 925 */         ServiceStep subServiceStepObj = serviceStepMap.get(stepName);
/* 926 */         int i = 0;
/*     */         
/* 928 */         if (subServiceStepObj != null) {
/* 929 */           Object stepCollectionObj = new Object();
/* 930 */           HashMap<String, Object> tempserviceFieldMap = new HashMap<>();
/* 931 */           List<Object> tempList = new ArrayList();
/* 932 */           List<Object> stepCollectionObjList = new ArrayList();
/*     */           
/* 934 */           if (isRepeatColl || !collectedRow.isEmpty()) {
/* 935 */             String parentCollection = "Collection" + subServiceStepObj.getDepends();
/*     */             
/* 937 */             if (serviceFieldMap.containsKey(parentCollection)) {
/* 938 */               stepCollectionObj = serviceFieldMap.get(parentCollection);
/*     */             } else {
/* 940 */               stepCollectionObj = null;
/*     */             } 
/* 942 */             stepCollectionObjList.add(stepCollectionObj);
/*     */           
/*     */           }
/*     */           else {
/*     */ 
/*     */             
/* 948 */             stepCollectionObj = serviceFieldMap.containsKey("Collection" + serviceStep.getId()) ? serviceFieldMap.get("Collection" + serviceStep.getId()) : null;
/*     */ 
/*     */             
/* 951 */             stepCollectionObjList.add(stepCollectionObj);
/*     */           } 
/*     */           
/* 954 */           for (int s = 0; s < stepCollectionObjList.size(); s++) {
/*     */             
/* 956 */             List<Object> stepObject = (List<Object>)stepCollectionObjList.get(s);
/* 957 */             int j = 0;
/* 958 */             for (Object itr : stepObject) {
/* 959 */               HashMap<String, Object> stepCollectedRow = (HashMap<String, Object>)itr;
/*     */ 
/*     */               
/* 962 */               executeServiceStep(subServiceStepObj, serviceFieldMap, collectedRow, true, serviceStepMap, subServiceStep
/* 963 */                   .getCollectionref(), stepCollectedRow, modifiedStepCollectionList);
/*     */ 
/*     */ 
/*     */               
/* 967 */               j++;
/*     */             } 
/*     */           } 
/*     */ 
/*     */           
/* 972 */           i++;
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 979 */       return modifiedStepCollectionList;
/* 980 */     } catch (IllegalArgumentException e) {
/*     */       
/* 982 */       throw e;
/* 983 */     } catch (Exception e) {
/*     */       
/* 985 */       logger.error("Error occurred in executeSubStepService due to Exception");
/* 986 */       throw e;
/*     */     } 
/*     */   }
/*     */ }


