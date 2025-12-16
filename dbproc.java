/*     */ package com.tcs.bancs.microservices.util;
/*     */ 
/*     */ import com.tcs.bancs.microservices.config.CBSDayDbConfig;
/*     */ import com.tcs.bancs.microservices.config.CacheConfig;
/*     */ import com.tcs.bancs.microservices.db.model.Sysc;
/*     */ import com.tcs.bancs.microservices.domain.DBData;
/*     */ import com.tcs.bancs.microservices.exception.RuleAccessTechnicalException;
/*     */ import com.tcs.bancs.microservices.interceptor.AggregatorRequestHandlerInterceptor;
/*     */ import com.tcs.bancs.microservices.repository.night.SyscRepository;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import javax.servlet.http.HttpServletRequest;
/*     */ import org.apache.commons.lang3.StringUtils;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.springframework.beans.factory.annotation.Autowired;
/*     */ import org.springframework.context.ApplicationContext;
/*     */ import org.springframework.context.annotation.Import;
/*     */ import org.springframework.data.domain.Page;
/*     */ import org.springframework.data.domain.PageImpl;
/*     */ import org.springframework.data.domain.PageRequest;
/*     */ import org.springframework.data.domain.Pageable;
/*     */ import org.springframework.stereotype.Service;
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
/*     */ @Service
/*     */ @Import({CacheConfig.class})
/*     */ public class DBProcess
/*     */ {
/*     */   @Autowired(required = false)
/*     */   SyscRepository SyscRepository;
/*     */   public static long dbStartTimeMillis;
/*     */   @Autowired
/*     */   ApplicationContext context;
/*     */   @Autowired
/*     */   HttpServletRequest HttpServletRequest;
/*     */   @Autowired
/*     */   DaoUtilities daoUtil;
/*  53 */   public Integer[] pageDetails = new Integer[2];
/*  54 */   public static Integer[] recordNumParam = new Integer[2];
/*     */   
/*  56 */   Logger logger = LoggerFactory.getLogger(DBProcess.class);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int defaultRowNumber = 10;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public DBData readSyscTable() {
/*  72 */     DBData dbData = new DBData();
/*  73 */     Sysc oSysc = new Sysc();
/*     */     
/*  75 */     oSysc = this.SyscRepository.FetchSyscDetails(AggregatorConfigLoader.syscProperties.getProperty("TargetLevel"), AggregatorConfigLoader.syscProperties
/*  76 */         .getProperty("RegionNo"), AggregatorConfigLoader.syscProperties
/*  77 */         .getProperty("EntityNo"), AggregatorConfigLoader.syscProperties
/*  78 */         .getProperty("SystemNo"), AggregatorConfigLoader.syscProperties
/*  79 */         .getProperty("NodeNo"), AggregatorConfigLoader.syscProperties
/*  80 */         .getProperty("ReplicaTypeX"), AggregatorConfigLoader.syscProperties
/*  81 */         .getProperty("ReplicaNo"));
/*  82 */     dbData.setAPP(oSysc.getSyscVariable().substring(10, 11));
/*     */     
/*  84 */     dbData.setMASTER_1(oSysc.getSyscVariable().substring(37, 45).trim());
/*  85 */     dbData.setMASTER_2(oSysc.getSyscVariable().substring(52, 60).trim());
/*  86 */     dbData.setIN_USE(oSysc.getSyscVariable().substring(67, 75).trim());
/*  87 */     return dbData;
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
/*     */   public DBData fetchDbRegionDetails(String beanName, DBData dbData) throws Exception {
/* 100 */     String type = "0";
/*     */     
/* 102 */     if (dbData.getAPP().equals("D")) {
/*     */       
/* 104 */       dbData.setDBINDICATOR("DD");
/* 105 */       dbData.setACCESS_TYPE(type);
/*     */     } else {
/* 107 */       type = AggregatorConfigLoader.propertiesConfiguration.getProperty(beanName.toUpperCase());
/*     */ 
/*     */ 
/*     */       
/* 111 */       String DBIndicator = dbData.getIN_USE().equals(CBSDayDbConfig.connectionType.trim()) ? "DN" : "RN";
/*     */ 
/*     */       
/* 114 */       dbData.setDBINDICATOR(DBIndicator);
/* 115 */       dbData.setACCESS_TYPE(type);
/*     */     } 
/* 117 */     return dbData;
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
/*     */   public <T> T fetchRepositories(Object dbModelBean, String repoBeanName, String repositoryName, String repoMethod, boolean pagination, boolean isList, ArrayList<Object> queryParams) throws Exception {
/* 148 */     T nightData = null;
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 153 */       DBData dbData = new DBData();
/* 154 */       dbData.setDBINDICATOR("DD");
/* 155 */       dbData.setACCESS_TYPE("0");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 163 */       if (pagination)
/* 164 */         getPageDetails(); 
/* 165 */       if (isList) {
/* 166 */         getRowNumber();
/*     */       }
/* 168 */       if (AggregatorConfigLoader.applicationDBRregionType.equals("24")) {
/*     */         
/* 170 */         dbData = (DBData)this.HttpServletRequest.getAttribute("DBData");
/*     */         
/* 172 */         dbData = fetchDbRegionDetails(repoBeanName, dbData);
/*     */       } 
/*     */ 
/*     */       
/* 176 */       String repositoryDDName = StringUtils.uncapitalize(RepoImplMapping2RepositoryBean("DD", repositoryName));
/*     */ 
/*     */       
/* 179 */       if (!"DD".equals(dbData.getDBINDICATOR())) {
/*     */ 
/*     */ 
/*     */         
/* 183 */         String repositoryNight = StringUtils.uncapitalize(
/* 184 */             RepoImplMapping2RepositoryBean("DN", repositoryName));
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
/* 197 */         nightData = invokeRepository(dbModelBean, repositoryNight, repoMethod, queryParams, this.pageDetails[0], this.pageDetails[1], recordNumParam[0], pagination, isList);
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
/* 209 */         if (nightData != null && dbData
/* 210 */           .getACCESS_TYPE().equalsIgnoreCase("3")) {
/* 211 */           return nightData;
/*     */         }
/*     */ 
/*     */         
/* 215 */         if ("RN".equals(dbData.getDBINDICATOR())) {
/*     */           
/* 217 */           String repositoryRef = StringUtils.uncapitalize(
/* 218 */               RepoImplMapping2RepositoryBean("RN", repositoryName));
/* 219 */           if (isList || pagination) {
/* 220 */             return invokeListRepository(nightData, dbModelBean, repositoryRef, repoMethod, pagination, queryParams);
/*     */           }
/* 222 */           if (nightData == null) {
/* 223 */             return invokeRepository(dbModelBean, repositoryRef, repoMethod, queryParams, this.pageDetails[0], this.pageDetails[1], recordNumParam[0], pagination, isList);
/*     */           }
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 231 */         if (isList || pagination) {
/* 232 */           return invokeListRepository(nightData, dbModelBean, repositoryDDName, repoMethod, pagination, queryParams);
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
/* 252 */       if (nightData == null) {
/* 253 */         return invokeRepository(dbModelBean, repositoryDDName, repoMethod, queryParams, this.pageDetails[0], this.pageDetails[1], recordNumParam[0], pagination, isList);
/*     */       
/*     */       }
/*     */     }
/* 257 */     catch (Exception e) {
/* 258 */       throw e;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 265 */     return nightData;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> T invokeListRepository(T data, Object dbModelBean, String repositoryName, String repoMethod, boolean pagination, ArrayList<Object> queryParams) throws Exception {
/* 312 */     Integer count = null;
/* 313 */     List<T> list2 = new ArrayList<>();
/*     */     
/* 315 */     if (pagination) {
/* 316 */       Page<T> pagedResult = (Page<T>)data;
/*     */       
/* 318 */       List<T> list3 = pagedResult.getContent();
/*     */       
/* 320 */       if (this.pageDetails[1] != null && list3.size() != this.pageDetails[1].intValue()) {
/*     */         
/* 322 */         count = (Integer)this.daoUtil.<T>calculatePageSize(list3, this.pageDetails[1].intValue());
/* 323 */         if (count.intValue() != 0) {
/* 324 */           queryParams.remove(queryParams.size() - 1);
/*     */           
/* 326 */           Page<T> pagedResult2 = invokeRepository(dbModelBean, repositoryName, repoMethod, queryParams, this.pageDetails[0], count, null, pagination, false);
/*     */ 
/*     */           
/* 329 */           list2 = pagedResult2.getContent();
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
/* 340 */       List<T> list4 = this.daoUtil.union(list3, list2);
/* 341 */       return (T)new PageImpl(list4);
/*     */     } 
/*     */ 
/*     */     
/* 345 */     List<T> list1 = (List<T>)data;
/*     */     
/* 347 */     if (list1.size() == 0)
/*     */     {
/* 349 */       return invokeRepository(dbModelBean, repositoryName, repoMethod, queryParams, this.pageDetails[0], this.pageDetails[1], recordNumParam[0], pagination, true);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 355 */     int listSize = 0;
/* 356 */     boolean isRange = false;
/* 357 */     if (recordNumParam.length == 2 && recordNumParam[1] != null) {
/* 358 */       listSize = recordNumParam[1].intValue() - recordNumParam[0].intValue();
/* 359 */       isRange = true;
/*     */     } 
/*     */     
/* 362 */     if (list1.size() != listSize) {
/* 363 */       count = (Integer)this.daoUtil.<T>calculatePageSize(list1, listSize);
/* 364 */       if (count.intValue() != 0) {
/*     */         
/* 366 */         queryParams.remove(queryParams.size() - 1);
/*     */         
/* 368 */         T data2 = invokeRepository(dbModelBean, repositoryName, repoMethod, queryParams, null, null, count, pagination, true);
/*     */         
/* 370 */         list2 = (List<T>)data2;
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 378 */     List<T> returnObjList = this.daoUtil.union(list1, list2);
/* 379 */     return (T)returnObjList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Integer[] getPageDetails() {
/* 387 */     if (AggregatorRequestHandlerInterceptor.requestParamMap != null) {
/*     */       
/* 389 */       int pageNumber = 0;
/*     */       
/* 391 */       if ((AggregatorRequestHandlerInterceptor.requestParamMap.containsKey("page") && (AggregatorRequestHandlerInterceptor.requestParamMap
/* 392 */         .containsKey("pageSize") || AggregatorRequestHandlerInterceptor.requestParamMap
/*     */         
/* 394 */         .containsKey("page-size"))) || (this.HttpServletRequest
/* 395 */         .getAttribute("page") != null && this.HttpServletRequest
/* 396 */         .getAttribute("pageSize") != null)) {
/*     */         
/* 398 */         if (this.HttpServletRequest.getParameter("page") == null) {
/*     */ 
/*     */           
/* 401 */           pageNumber = ((Integer)this.HttpServletRequest.getAttribute("page") == null) ? pageNumber : ((Integer)this.HttpServletRequest.getAttribute("page")).intValue();
/*     */         } else {
/* 403 */           pageNumber = Integer.valueOf(this.HttpServletRequest.getParameter("page")).intValue();
/*     */         } 
/* 405 */         if (pageNumber != 0)
/* 406 */           pageNumber--; 
/* 407 */         this.pageDetails[0] = Integer.valueOf(pageNumber);
/* 408 */         if (this.HttpServletRequest.getParameter("pageSize") == null && this.HttpServletRequest
/* 409 */           .getParameter("page-size") == null) {
/* 410 */           this.pageDetails[1] = ((Integer)this.HttpServletRequest.getAttribute("pageSize") == null) ? null : (Integer)this.HttpServletRequest
/*     */             
/* 412 */             .getAttribute("pageSize");
/*     */         }
/* 414 */         else if (this.HttpServletRequest.getParameter("pageSize") != null) {
/* 415 */           this.pageDetails[1] = Integer.valueOf(this.HttpServletRequest.getParameter("pageSize"));
/* 416 */         } else if (this.HttpServletRequest.getParameter("page-size") != null) {
/* 417 */           this.pageDetails[1] = Integer.valueOf(this.HttpServletRequest.getParameter("page-size"));
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 429 */     return this.pageDetails;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Integer[] getRowNumber() {
/* 438 */     if (AggregatorRequestHandlerInterceptor.requestParamMap.containsKey("recordNum") || this.HttpServletRequest
/* 439 */       .getAttribute("recordNum") != null) {
/*     */       
/* 441 */       recordNumParam[0] = Integer.valueOf((AggregatorRequestHandlerInterceptor.requestParamMap.get("recordNum") == null) ? (
/* 442 */           (this.HttpServletRequest.getAttribute("recordNum") == null) ? 0 : ((Integer)this.HttpServletRequest
/* 443 */           .getAttribute("recordNum")).intValue()) : ((Integer)AggregatorRequestHandlerInterceptor.requestParamMap
/* 444 */           .get("recordNum")).intValue());
/*     */     
/*     */     }
/* 447 */     else if ((AggregatorRequestHandlerInterceptor.requestParamMap.containsKey("startRecordNum") || this.HttpServletRequest
/* 448 */       .getAttribute("startRecordNum") != null) && (AggregatorRequestHandlerInterceptor.requestParamMap
/* 449 */       .containsKey("endRecordNum") || this.HttpServletRequest
/* 450 */       .getAttribute("endRecordNum") != null)) {
/*     */       
/* 452 */       recordNumParam[0] = Integer.valueOf(
/* 453 */           (AggregatorRequestHandlerInterceptor.requestParamMap.get("startRecordNum") == null) ? (
/* 454 */           (this.HttpServletRequest.getAttribute("startRecordNum") == null) ? 0 : ((Integer)this.HttpServletRequest
/*     */           
/* 456 */           .getAttribute("startRecordNum")).intValue()) : ((Integer)AggregatorRequestHandlerInterceptor.requestParamMap
/* 457 */           .get("startRecordNum")).intValue());
/* 458 */       recordNumParam[1] = Integer.valueOf((AggregatorRequestHandlerInterceptor.requestParamMap.get("endRecordNum") == null) ? (
/* 459 */           (this.HttpServletRequest.getAttribute("endRecordNum") == null) ? 0 : ((Integer)this.HttpServletRequest
/* 460 */           .getAttribute("endRecordNum")).intValue()) : ((Integer)AggregatorRequestHandlerInterceptor.requestParamMap
/* 461 */           .get("endRecordNum")).intValue());
/*     */     }
/*     */     else {
/*     */       
/* 465 */       recordNumParam[0] = Integer.valueOf(0);
/*     */     } 
/*     */     
/* 468 */     return recordNumParam;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> T invokeRepository(Object dbModelBean, String repositoryName, String repoMethod, ArrayList<Object> queryParams, Integer page, Integer pageSize, Integer recordNum, boolean isPagination, boolean isList) throws Exception {
/* 478 */     T t = null;
/* 479 */     if (dbModelBean != null) {
/*     */       
/* 481 */       Object inputData = null;
/* 482 */       Class[] cArg = new Class[1];
/*     */       
/* 484 */       if (dbModelBean != null) {
/* 485 */         inputData = dbModelBean;
/*     */       }
/* 487 */       cArg[0] = inputData.getClass();
/*     */       
/* 489 */       t = (T)this.context.getBean(repositoryName).getClass().getDeclaredMethod(repoMethod, cArg).invoke(this.context.getBean(repositoryName), new Object[] { inputData });
/*     */ 
/*     */     
/*     */     }
/* 493 */     else if (queryParams != null) {
/*     */       
/* 495 */       if (isPagination) {
/* 496 */         if (page == null && pageSize == null) {
/* 497 */           queryParams.add(null);
/*     */         } else {
/*     */           
/* 500 */           PageRequest pageRequest = PageRequest.of(page.intValue(), pageSize.intValue());
/* 501 */           queryParams.add(pageRequest);
/*     */         } 
/*     */       }
/*     */ 
/*     */       
/* 506 */       if (isList && recordNum.intValue() != 0) {
/* 507 */         queryParams.add(recordNum);
/*     */       }
/* 509 */       Class[] cArg = new Class[queryParams.size()];
/* 510 */       Object[] data = new Object[queryParams.size()];
/* 511 */       Object[] inputData = new Object[queryParams.size()];
/* 512 */       for (int i = 0; i < queryParams.size(); i++) {
/*     */ 
/*     */         
/* 515 */         if (isPagination && queryParams.get(i) != null && queryParams.get(i).getClass() == PageRequest.class) {
/* 516 */           cArg[cArg.length - 1] = Pageable.class;
/*     */         
/*     */         }
/* 519 */         else if (queryParams.get(i) == null) {
/* 520 */           cArg[i] = String.class;
/*     */         } else {
/* 522 */           cArg[i] = queryParams.get(i).getClass();
/*     */         } 
/* 524 */         data[i] = queryParams.get(i);
/*     */ 
/*     */         
/* 527 */         inputData[i] = queryParams.get(i);
/*     */       } 
/*     */ 
/*     */       
/* 531 */       t = (T)this.context.getBean(repositoryName).getClass().getDeclaredMethod(repoMethod, cArg).invoke(this.context.getBean(repositoryName), inputData);
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 536 */       t = (T)this.context.getBean(repositoryName).getClass().getDeclaredMethod(repoMethod, new Class[0]).invoke(this.context.getBean(repositoryName), new Object[0]);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 544 */     return t;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> T fetchRepository(Object dbModelBean, String repoBeanName, String repositoryName, String repoMethod, Object rowNum, boolean isList, ArrayList<Object> queryParams) throws Exception {
/*     */     try {
/* 552 */       long dbStartTimeMillis = System.currentTimeMillis();
/*     */ 
/*     */       
/* 555 */       Object returnObjData = null;
/*     */       
/* 557 */       DBData dbData = new DBData();
/* 558 */       dbData.setDBINDICATOR("DD");
/* 559 */       dbData.setACCESS_TYPE("0");
/*     */       
/* 561 */       if (AggregatorConfigLoader.applicationDBRregionType.equals("24")) {
/*     */         
/* 563 */         dbData = (DBData)this.HttpServletRequest.getAttribute("DBData");
/*     */         
/* 565 */         dbData = fetchDbRegionDetails(repoBeanName, dbData);
/*     */       } 
/*     */       
/* 568 */       String DBIndicator = dbData.getDBINDICATOR();
/*     */       
/* 570 */       String primaryRepositoryName = RepoImplMapping2RepositoryClass(DBIndicator, repositoryName);
/*     */       
/* 572 */       String secondRepositoryName = "";
/* 573 */       Class<?> secondaryRepoClass = null;
/*     */       
/* 575 */       Method secondaryRepoImplMethod = null;
/* 576 */       boolean isSecondaryRepo = false;
/* 577 */       if (!"DD".equals(dbData.getDBINDICATOR())) {
/* 578 */         isSecondaryRepo = true;
/*     */         
/* 580 */         if ("DN".equals(dbData.getDBINDICATOR())) {
/* 581 */           secondRepositoryName = RepoImplMapping2RepositoryClass("DD", repositoryName);
/*     */         }
/*     */         else {
/*     */           
/* 585 */           secondRepositoryName = primaryRepositoryName;
/* 586 */           primaryRepositoryName = RepoImplMapping2RepositoryClass("RN", repositoryName);
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 592 */       Class<?> primaryRepoClass = Class.forName(primaryRepositoryName);
/*     */       
/* 594 */       if (isSecondaryRepo) {
/* 595 */         secondaryRepoClass = Class.forName(secondRepositoryName);
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 600 */       if (dbModelBean != null || rowNum != null) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 606 */         Object inputData = rowNum;
/*     */         
/* 608 */         Class[] cArg = new Class[1];
/*     */         
/* 610 */         if (dbModelBean != null) {
/* 611 */           inputData = dbModelBean;
/*     */         }
/* 613 */         cArg[0] = inputData.getClass();
/* 614 */         Method primaryRepoImplMethod = primaryRepoClass.getDeclaredMethod(repoMethod, cArg);
/*     */         
/* 616 */         if (isSecondaryRepo) {
/* 617 */           secondaryRepoImplMethod = secondaryRepoClass.getDeclaredMethod(repoMethod, cArg);
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 627 */         returnObjData = invokeRepositoryMethods(dbData, primaryRepoClass, secondaryRepoClass, primaryRepoImplMethod, secondaryRepoImplMethod, repoMethod, isList, rowNum, inputData, null);
/*     */ 
/*     */ 
/*     */       
/*     */       }
/* 632 */       else if (queryParams != null) {
/* 633 */         Class[] cArg = new Class[queryParams.size()];
/* 634 */         Object[] data = new Object[queryParams.size()];
/* 635 */         Object[] inputData = new Object[queryParams.size()];
/* 636 */         for (int i = 0; i < queryParams.size(); i++) {
/*     */           
/* 638 */           cArg[i] = queryParams.get(i).getClass();
/* 639 */           data[i] = queryParams.get(i);
/*     */           
/* 641 */           inputData[i] = queryParams.get(i);
/*     */         } 
/* 643 */         Method primaryRepoImplMethod = primaryRepoClass.getDeclaredMethod(repoMethod, cArg);
/*     */         
/* 645 */         if (isSecondaryRepo) {
/* 646 */           secondaryRepoImplMethod = secondaryRepoClass.getDeclaredMethod(repoMethod, cArg);
/*     */         }
/*     */ 
/*     */         
/* 650 */         returnObjData = invokeRepositoryMethods(dbData, primaryRepoClass, secondaryRepoClass, primaryRepoImplMethod, secondaryRepoImplMethod, repoMethod, isList, rowNum, null, inputData);
/*     */ 
/*     */       
/*     */       }
/*     */       else {
/*     */ 
/*     */         
/* 657 */         Method primaryRepoImplMethod = primaryRepoClass.getDeclaredMethod(repoMethod, new Class[0]);
/* 658 */         if (isSecondaryRepo) {
/* 659 */           secondaryRepoImplMethod = secondaryRepoClass.getDeclaredMethod(repoMethod, new Class[0]);
/*     */         }
/*     */ 
/*     */         
/* 663 */         returnObjData = invokeRepositoryMethods(dbData, primaryRepoClass, secondaryRepoClass, primaryRepoImplMethod, secondaryRepoImplMethod, repoMethod, isList, rowNum, null, null);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 671 */       return (T)returnObjData;
/*     */     }
/* 673 */     catch (Exception e) {
/*     */ 
/*     */       
/* 676 */       throw e;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> T invokeRepositoryMethods(DBData dbData, Class primaryRepoClass, Class secondaryRepoClass, Method primaryRepoImplMethod, Method secondaryRepoImplMethod, String repoMethod, boolean isList, Object rowNum, Object inputData, Object[] inputDataArr) throws Exception {
/* 704 */     Object object1 = null;
/* 705 */     Object returnObj = new Object();
/* 706 */     List<Object> returnObjList = new ArrayList();
/* 707 */     long startTime = System.currentTimeMillis();
/*     */ 
/*     */     
/*     */     try {
/* 711 */       if (isList) {
/* 712 */         if (inputData != null) {
/* 713 */           returnObjList = (List<Object>)primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), new Object[] { inputData });
/*     */         }
/* 715 */         else if (inputDataArr != null) {
/*     */           
/* 717 */           returnObjList = (List<Object>)primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), inputDataArr);
/*     */         } else {
/*     */           
/* 720 */           returnObjList = (List<Object>)primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), new Object[0]);
/*     */         } 
/* 722 */         object1 = returnObjList;
/*     */       
/*     */       }
/* 725 */       else if (inputData != null) {
/* 726 */         object1 = primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), new Object[] { inputData });
/* 727 */       } else if (inputDataArr != null) {
/*     */         
/* 729 */         object1 = primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), inputDataArr);
/*     */       } else {
/*     */         
/* 732 */         object1 = primaryRepoImplMethod.invoke(this.context.getBean(primaryRepoClass), new Object[0]);
/*     */       } 
/*     */ 
/*     */       
/* 736 */       if (dbData.getACCESS_TYPE().equals("1")) {
/*     */         List<Object> list;
/*     */         
/* 739 */         Integer count = Integer.valueOf(0);
/* 740 */         int rowNumber = 0;
/*     */         
/* 742 */         if (rowNum == null) {
/* 743 */           rowNumber = 10;
/*     */         } else {
/*     */           
/* 746 */           rowNumber = Integer.parseInt(rowNum.toString());
/*     */         } 
/* 748 */         if (isList) {
/*     */           
/* 750 */           if (returnObjList.size() != rowNumber) {
/* 751 */             count = (Integer)this.daoUtil.<Object>calculatePageSize(returnObjList, rowNumber);
/* 752 */             if (count.intValue() != 0) {
/* 753 */               List<Object> secondaryList = new ArrayList();
/* 754 */               Class[] cArg = new Class[1];
/* 755 */               cArg[0] = count.getClass();
/*     */               
/* 757 */               if (inputData != null) {
/*     */                 
/* 759 */                 secondaryList = (List<Object>)secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), new Object[] { count });
/* 760 */               } else if (inputDataArr != null) {
/*     */                 
/* 762 */                 secondaryList = (List<Object>)secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), inputDataArr);
/*     */               } else {
/*     */                 
/* 765 */                 secondaryList = (List<Object>)secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), new Object[] { count });
/*     */               } 
/* 767 */               returnObjList = this.daoUtil.union(returnObjList, secondaryList);
/*     */             } 
/*     */           } 
/*     */           
/* 771 */           list = returnObjList;
/*     */ 
/*     */         
/*     */         }
/* 775 */         else if (list == null) {
/*     */           
/* 777 */           if (inputData != null) {
/* 778 */             Object object = secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), new Object[] { inputData });
/* 779 */           } else if (inputDataArr != null) {
/*     */             
/* 781 */             Object object = secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), inputDataArr);
/*     */           } else {
/*     */             
/* 784 */             object1 = secondaryRepoImplMethod.invoke(this.context.getBean(secondaryRepoClass), new Object[0]);
/*     */           }
/*     */         
/*     */         } 
/*     */       } 
/* 789 */     } catch (Exception e) {
/*     */       
/* 791 */       this.logger.error("Error occurred in invokeRepositoryMethods due to Exception  ");
/*     */       
/* 793 */       throw new Exception();
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 799 */     return (T)object1;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String RepoImplMapping2RepositoryClass(String dbIndicator, String repoName) throws RuleAccessTechnicalException, IOException {
/* 805 */     String className = "";
/* 806 */     if (dbIndicator.equals("DD")) {
/* 807 */       className = "com.tcs.bancs.microservices.repository.day." + repoName + "DayRepo";
/*     */     }
/* 809 */     else if (dbIndicator.equals("DN")) {
/* 810 */       className = "com.tcs.bancs.microservices.repository.night." + repoName + "NightRepo";
/*     */     }
/* 812 */     else if (dbIndicator.equals("RN")) {
/* 813 */       className = "com.tcs.bancs.microservices.repository.ref." + repoName + "RefRepo";
/*     */     } 
/*     */     
/* 816 */     return className;
/*     */   }
/*     */ 
/*     */   
/*     */   public String RepoImplMapping2RepositoryBean(String dbIndicator, String repoName) throws RuleAccessTechnicalException, IOException {
/* 821 */     String className = "";
/* 822 */     if (dbIndicator.equals("DD")) {
/* 823 */       className = repoName + "DayRepo";
/* 824 */     } else if (dbIndicator.equals("DN")) {
/* 825 */       className = repoName + "NightRepo";
/* 826 */     } else if (dbIndicator.equals("RN")) {
/* 827 */       className = repoName + "RefRepo";
/*     */     } 
/* 829 */     return className;
/*     */   }
/*     */ }
