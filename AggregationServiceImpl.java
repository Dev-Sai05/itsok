/*      */ package com.tcs.bancs.microservices.aggregator;
/*      */ 
/*      */ import com.google.gson.Gson;
/*      */ import com.google.gson.GsonBuilder;
/*      */ import com.google.gson.JsonObject;
/*      */ import com.tcs.bancs.microservices.config.CacheConfig;
/*      */ import com.tcs.bancs.microservices.config.InputSourceType;
/*      */ import com.tcs.bancs.microservices.domain.Body;
/*      */ import com.tcs.bancs.microservices.domain.ServiceInputField;
/*      */ import com.tcs.bancs.microservices.domain.ServiceOutputCollection;
/*      */ import com.tcs.bancs.microservices.domain.ServiceOutputField;
/*      */ import com.tcs.bancs.microservices.domain.ServiceOutputObject;
/*      */ import com.tcs.bancs.microservices.domain.ServiceStep;
/*      */ import com.tcs.bancs.microservices.domain.ServiceTranslation;
/*      */ import com.tcs.bancs.microservices.exception.AggregatorFrameworkException;
/*      */ import com.tcs.bancs.microservices.exception.RuleAccessTechnicalException;
/*      */ import com.tcs.bancs.microservices.util.AggregationUtils;
/*      */ import com.tcs.bancs.microservices.util.DataTypeReturn;
/*      */ import com.tcs.bancs.microservices.util.ErrorCode;
/*      */ import com.tcs.bancs.microservices.util.ReflectionUtility;
/*      */ import com.tcs.bancs.microservices.util.Singleton;
/*      */ import com.tcs.jsonata.JSONata4JavaInvoker;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.OutputStream;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.lang.reflect.Method;
/*      */ import java.net.HttpURLConnection;
/*      */ import java.net.URL;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import javax.servlet.http.HttpServletRequest;
/*      */ import org.slf4j.Logger;
/*      */ import org.slf4j.LoggerFactory;
/*      */ import org.springframework.beans.factory.annotation.Autowired;
/*      */ import org.springframework.context.annotation.Import;
/*      */ import org.springframework.http.HttpHeaders;
/*      */ import org.springframework.stereotype.Service;
/*      */ import org.springframework.web.bind.annotation.PathVariable;
/*      */ import org.springframework.web.bind.annotation.RequestHeader;
/*      */ import org.springframework.web.bind.annotation.RequestParam;
/*      */ import org.springframework.web.bind.annotation.ResponseBody;
/*      */ import org.springframework.web.servlet.HandlerMapping;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ @Service
/*      */ @Import({CacheConfig.class})
/*      */ public class AggregationServiceImpl
/*      */ {
/*   65 */   public static HashMap<String, Body> serviceXmlObjectMap = new HashMap<>();
/*   66 */   public static final String XMLPATH = CacheConfig.frameworkConfigProperties.getProperty("microservices.aggregator.framework.serviceXmlConfigPath");
/*   67 */   static Logger logger = LoggerFactory.getLogger(AggregationServiceImpl.class);
/*      */   
/*      */   static final String xmlExtension = ".xml";
/*      */   
/*      */   @Autowired
/*      */   ServiceStepExecutor serviceExecutor;
/*   73 */   public static Map pathParamMap = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Autowired
/*      */   HttpServletRequest HttpServletRequest;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @ResponseBody
/*      */   public Object init(@PathVariable Map<String, String> multiPathVar, @PathVariable String serviceName, @RequestParam Map<String, String> allRequestParams, @RequestHeader HttpHeaders headers, HttpServletRequest request) throws Exception {
/*   92 */     long startTime = System.currentTimeMillis();
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     try {
/*   98 */       if (multiPathVar == null) {
/*   99 */         multiPathVar = (Map<String, String>)this.HttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
/*      */       }
/*      */       
/*  102 */       logger.debug("Aggregation called for Service --> " + serviceName);
/*  103 */       logger.debug("Request Parameter Map --> " + allRequestParams);
/*  104 */       logger.debug("Header Map --> " + headers);
/*  105 */       logger.debug("Path Variable Map --> " + multiPathVar);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  119 */       HashMap<String, Object> serviceFieldMap = new HashMap<>();
/*  120 */       HashMap<String, Object> collectedRow = new HashMap<>();
/*  121 */       HashMap<String, Object> stepcollectedRow = new HashMap<>();
/*  122 */       LinkedHashMap<String, Object> finalOutFieldMap = new LinkedHashMap<>();
/*      */ 
/*      */ 
/*      */       
/*  126 */       String xmlConfigFileName = serviceName + ".xml";
/*  127 */       logger.debug("Aggregator config xml file name -->" + xmlConfigFileName);
/*      */ 
/*      */       
/*  130 */       Body body = loadServiceXml(xmlConfigFileName);
/*      */ 
/*      */       
/*  133 */       processInputToServiceFields(body, null, allRequestParams, multiPathVar, headers, serviceFieldMap);
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  138 */       processServiceSteps(body, serviceFieldMap, collectedRow, stepcollectedRow);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  146 */       processServiceOutput(body.getService().getServiceOutput().getServiceOutputFieldList(), body
/*  147 */           .getService().getServiceOutput().getServiceOutputCollection(), body
/*  148 */           .getService().getServiceOutput().getServiceOutputObject(), serviceFieldMap, finalOutFieldMap, collectedRow);
/*      */ 
/*      */ 
/*      */       
/*  152 */       Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
/*      */       
/*  154 */       Object finalOutFieldMapStr = gson.toJson(finalOutFieldMap);
/*      */       
/*  156 */       if (body.getService().getServiceTranslation() != null) {
/*      */ 
/*      */ 
/*      */         
/*  160 */         Object finalOutBean = processServiceTranslation(finalOutFieldMapStr, serviceFieldMap, body
/*  161 */             .getService().getServiceTranslation());
/*  162 */         return finalOutBean;
/*      */       } 
/*      */       
/*  165 */       logger.info("FINALOUTFIELDMAP  ====> " + finalOutFieldMap);
/*      */ 
/*      */ 
/*      */       
/*  169 */       return finalOutFieldMapStr;
/*      */     }
/*  171 */     catch (Exception e) {
/*      */ 
/*      */       
/*  174 */       logger.error("Error in executing service " + serviceName + " due to Exception ");
/*      */       
/*  176 */       throw e;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object processServiceTranslation(Object finalOutFieldMapStr, HashMap<String, Object> serviceFieldMap, ServiceTranslation serviceTranslation) {
/*  208 */     long startTime = System.currentTimeMillis();
/*      */ 
/*      */     
/*  211 */     String isRequired = serviceTranslation.getRequired();
/*  212 */     String jsonHeader = "";
/*      */     
/*  214 */     if (isRequired.equalsIgnoreCase("Y")) {
/*      */       
/*      */       try {
/*      */         
/*  218 */         if (serviceTranslation.getHeader() != null && !serviceTranslation.getHeader().isEmpty()) {
/*      */ 
/*      */           
/*  221 */           Object rqHeader = serviceFieldMap.get("RqHeader");
/*  222 */           jsonHeader = "\"Header\":" + rqHeader.toString() + ",";
/*      */         } 
/*      */ 
/*      */         
/*  226 */         String jsonAtaFileName = serviceTranslation.getJsonAtaFile();
/*  227 */         boolean isError = false;
/*      */ 
/*      */         
/*  230 */         JsonObject jsonObj = (JsonObject)(new Gson()).fromJson(finalOutFieldMapStr.toString(), JsonObject.class);
/*  231 */         if (jsonObj != null && (
/*  232 */           jsonObj.get("code") != null || jsonObj.get("message") != null) && jsonObj
/*  233 */           .get("ERROR").getAsString().equalsIgnoreCase("ERROR")) {
/*  234 */           jsonAtaFileName = "ErrorMessage";
/*  235 */           isError = true;
/*      */         } 
/*      */         
/*  238 */         finalOutFieldMapStr = "{" + jsonHeader + "\"Data\":" + finalOutFieldMapStr.toString() + "}";
/*      */ 
/*      */         
/*  241 */         JSONata4JavaInvoker jsonInvoker = new JSONata4JavaInvoker();
/*      */         
/*  243 */         logger.debug("Input JSON String  == >  " + finalOutFieldMapStr.toString());
/*  244 */         finalOutFieldMapStr = jsonInvoker.invokeJsonAtaMethod(finalOutFieldMapStr.toString(), jsonAtaFileName);
/*  245 */         logger.debug("Output JSON String  == >  " + finalOutFieldMapStr.toString());
/*      */         
/*  247 */         if (isError)
/*      */         {
/*      */           
/*  250 */           return finalOutFieldMapStr; } 
/*  251 */         if (serviceTranslation.getOut_model_name() != null)
/*      */         {
/*  253 */           String outModelName = serviceTranslation.getOut_model_name();
/*      */           
/*  255 */           Class<?> modelClass = Class.forName(outModelName);
/*  256 */           Object modelBean = modelClass.newInstance();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  281 */           modelBean = Singleton.objectMapper.readValue(finalOutFieldMapStr.toString(), modelClass);
/*      */ 
/*      */           
/*  284 */           return modelBean;
/*      */         }
/*      */       
/*      */       }
/*  288 */       catch (Exception e) {
/*      */         
/*  290 */         logger.error(" Error occurred in processServiceTranslation due to Exception ");
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  297 */     return finalOutFieldMapStr;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void processInputToServiceFields(Body body, JsonObject inpJsonObj, Map<String, String> allRequestParams, Map<String, String> multiPathVar, HttpHeaders headers, HashMap<String, Object> serviceFieldMap) throws Exception {
/*  338 */     long startTime = System.currentTimeMillis();
/*      */ 
/*      */     
/*      */     try {
/*  342 */       HashMap<String, Object> preProcessMap = new HashMap<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  354 */       if (body.getService().getServiceInput() == null) {
/*      */ 
/*      */         
/*  357 */         if (inpJsonObj != null)
/*      */         {
/*  359 */           for (String key : inpJsonObj.keySet()) {
/*  360 */             serviceFieldMap.put(key, inpJsonObj.get(key));
/*      */           }
/*      */         }
/*  363 */         Map<String, String[]> servletRequestParams = null;
/*  364 */         if (allRequestParams == null) {
/*  365 */           servletRequestParams = this.HttpServletRequest.getParameterMap();
/*  366 */           for (String key : servletRequestParams.keySet()) {
/*  367 */             String[] strArr = servletRequestParams.get(key);
/*      */             
/*  369 */             serviceFieldMap.put(key, strArr[0]);
/*      */           } 
/*      */         } else {
/*  372 */           for (String key : allRequestParams.keySet())
/*      */           {
/*  374 */             serviceFieldMap.put(key, allRequestParams.get(key));
/*      */           }
/*      */         } 
/*  377 */         if (headers != null)
/*      */         {
/*  379 */           for (String key : headers.keySet()) {
/*  380 */             serviceFieldMap.put(key, headers.get(key));
/*      */           }
/*      */         }
/*      */       } else {
/*  384 */         for (ServiceInputField serviceInputField : body.getService().getServiceInput()
/*  385 */           .getServiceInputFieldList()) {
/*  386 */           String inFieldName = serviceInputField.getIn_field_name();
/*  387 */           String inFieldSource = serviceInputField.getSource();
/*  388 */           String serviceFieldName = serviceInputField.getService_field_name();
/*  389 */           String preProcessing = serviceInputField.getPre_process();
/*      */           
/*  391 */           String repoFieldFormat = serviceInputField.getFormat();
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  396 */           if (preProcessing != null && !preProcessing.equalsIgnoreCase("")) {
/*  397 */             for (String key : allRequestParams.keySet()) {
/*  398 */               serviceFieldMap.put(key, allRequestParams.get(key));
/*      */             }
/*      */             
/*  401 */             for (String key : allRequestParams.keySet()) {
/*  402 */               preProcessMap.put(key, allRequestParams.get(key));
/*      */             }
/*      */           } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  414 */           Object fieldValue = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  421 */           if (inFieldSource.equals(InputSourceType.GET_PARAM.toString())) {
/*  422 */             if (allRequestParams == null) {
/*  423 */               fieldValue = (this.HttpServletRequest.getParameter(inFieldName) != null) ? this.HttpServletRequest.getParameter(inFieldName) : "";
/*      */             } else {
/*  425 */               fieldValue = allRequestParams.containsKey(inFieldName) ? allRequestParams.get(inFieldName) : "";
/*      */ 
/*      */             
/*      */             }
/*      */ 
/*      */           
/*      */           }
/*  432 */           else if (inFieldSource.equals(InputSourceType.CONSTANT.toString())) {
/*  433 */             fieldValue = String.valueOf(preProcessing);
/*  434 */           } else if (inFieldSource.equals(InputSourceType.UTILITY.toString())) {
/*      */             
/*  436 */             if (allRequestParams == null) {
/*  437 */               fieldValue = (this.HttpServletRequest.getParameter(inFieldName) != null) ? this.HttpServletRequest.getParameter(inFieldName) : "";
/*      */             } else {
/*  439 */               fieldValue = allRequestParams.containsKey(inFieldName) ? allRequestParams.get(inFieldName) : "";
/*      */             } 
/*      */ 
/*      */             
/*  443 */             if (fieldValue == null || fieldValue.equals("")) {
/*  444 */               fieldValue = multiPathVar.containsKey(inFieldName) ? multiPathVar.get(inFieldName) : "";
/*      */             }
/*  446 */             serviceFieldMap.put(serviceFieldName, fieldValue);
/*  447 */             fieldValue = AggregationUtils.callUtilityFromText(preProcessing, serviceFieldMap, preProcessMap, null);
/*      */           }
/*  449 */           else if (inFieldSource.equals(InputSourceType.HEADER.toString())) {
/*  450 */             fieldValue = headers.containsKey(inFieldName) ? headers.get(inFieldName) : "";
/*      */           }
/*  452 */           else if (inFieldSource.equals(InputSourceType.PATHPARAM.toString())) {
/*  453 */             fieldValue = multiPathVar.containsKey(inFieldName) ? multiPathVar.get(inFieldName) : "";
/*      */           } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  467 */           serviceFieldMap.put(serviceFieldName, fieldValue);
/*      */         } 
/*      */       } 
/*  470 */     } catch (Exception e) {
/*  471 */       throw e;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void processServiceSteps(Body body, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow, HashMap<String, Object> stepcollectedRow) throws Exception {
/*  482 */     long startTime = System.currentTimeMillis();
/*      */ 
/*      */     
/*      */     try {
/*  486 */       List<ServiceStep> serviceStepList = body.getService().getServiceStepsList();
/*  487 */       HashMap<String, ServiceStep> serviceStepMap = new HashMap<>();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  497 */       for (ServiceStep serviceStep1 : serviceStepList) {
/*  498 */         serviceStepMap.put(serviceStep1.getId(), serviceStep1);
/*      */       }
/*      */ 
/*      */       
/*  502 */       List<Object> modifiedList = new ArrayList();
/*  503 */       ServiceStep serviceStep = null;
/*      */       
/*  505 */       if (!serviceStepList.isEmpty()) {
/*  506 */         serviceStep = serviceStepList.get(0);
/*      */       }
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  512 */       this.serviceExecutor.executeServiceStep(serviceStep, serviceFieldMap, collectedRow, false, serviceStepMap, serviceStep
/*  513 */           .getId(), stepcollectedRow, modifiedList);
/*      */ 
/*      */     
/*      */     }
/*  517 */     catch (Exception e) {
/*  518 */       logger.error("Error occurred in processServiceSteps due to Exception  ");
/*  519 */       throw e;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private String callWebService(String stepServiceInputJson, String strUrl) throws Exception {
/*      */     try {
/*  532 */       URL url = new URL(strUrl);
/*  533 */       HttpURLConnection conn = (HttpURLConnection)url.openConnection();
/*  534 */       conn.setDoOutput(true);
/*  535 */       conn.setRequestMethod("POST");
/*  536 */       conn.setRequestProperty("Content-Type", "application/json");
/*      */       
/*  538 */       OutputStream os = conn.getOutputStream();
/*  539 */       os.write(stepServiceInputJson.getBytes());
/*  540 */       os.flush();
/*      */       
/*  542 */       BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
/*      */ 
/*      */       
/*  545 */       StringBuffer strbOut = new StringBuffer(); String output;
/*  546 */       while ((output = br.readLine()) != null) {
/*  547 */         strbOut.append(output);
/*      */       }
/*      */       
/*  550 */       return strbOut.toString();
/*      */     }
/*  552 */     catch (Exception e) {
/*  553 */       throw e;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void processServiceOutput(List<ServiceOutputField> serviceOutputFieldList, List<ServiceOutputCollection> serviceOutputCollectionList, List<ServiceOutputObject> serviceOutputObjectList, HashMap<String, Object> serviceFieldMap, LinkedHashMap<String, Object> finalOutFieldMap, HashMap<String, Object> collectedRow) throws RuleAccessTechnicalException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ParseException {
/*  566 */     long startTime = System.currentTimeMillis();
/*      */ 
/*      */ 
/*      */     
/*  570 */     HashMap<String, Object> serviceFieldMapTemp = serviceFieldMap;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  575 */     String errorMessage = serviceFieldMap.containsKey("ERROR_MESSAGE") ? (String)serviceFieldMap.get("ERROR_MESSAGE") : null;
/*      */     
/*  577 */     int errorCode = serviceFieldMap.containsKey("ERROR_CODE") ? ((Integer)serviceFieldMap.get("ERROR_CODE")).intValue() : 0;
/*      */     
/*  579 */     if (errorMessage == null || errorCode == 0) {
/*  580 */       if (serviceOutputFieldList != null && !serviceOutputFieldList.isEmpty())
/*      */       {
/*  582 */         finalOutFieldMap = (LinkedHashMap<String, Object>)processFields(finalOutFieldMap, serviceOutputFieldList, serviceFieldMap, collectedRow);
/*      */       }
/*      */ 
/*      */ 
/*      */       
/*  587 */       if (serviceOutputCollectionList != null && !serviceOutputCollectionList.isEmpty())
/*      */       {
/*  589 */         finalOutFieldMap = (LinkedHashMap<String, Object>)processList(finalOutFieldMap, serviceOutputFieldList, serviceOutputCollectionList, serviceFieldMap, false, null, collectedRow);
/*      */       }
/*      */ 
/*      */ 
/*      */       
/*  594 */       if (serviceOutputObjectList != null && !serviceOutputObjectList.isEmpty())
/*      */       {
/*  596 */         finalOutFieldMap = (LinkedHashMap<String, Object>)processObject(finalOutFieldMap, serviceOutputFieldList, serviceOutputObjectList, serviceFieldMapTemp, false, null, collectedRow);
/*      */       
/*      */       }
/*      */     }
/*      */     else {
/*      */       
/*  602 */       finalOutFieldMap.put("code", Integer.valueOf(errorCode));
/*  603 */       finalOutFieldMap.put("message", errorMessage);
/*  604 */       finalOutFieldMap.put("ERROR", "ERROR");
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object processFields(LinkedHashMap<String, Object> finalOutFieldMap, List<ServiceOutputField> serviceOutputFieldList, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow) throws RuleAccessTechnicalException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ParseException {
/*  618 */     DataTypeReturn retDataType = new DataTypeReturn();
/*      */ 
/*      */ 
/*      */     
/*  622 */     for (ServiceOutputField serviceOutputField : serviceOutputFieldList) {
/*  623 */       String valueType = serviceOutputField.getOut_field_type();
/*  624 */       String repoFieldName = serviceOutputField.getOut_field_name();
/*  625 */       Object serviceFieldValue = extractOutValueFromMap(valueType, serviceOutputField, serviceFieldMap, collectedRow);
/*      */ 
/*      */       
/*  628 */       finalOutFieldMap.put(repoFieldName, serviceFieldValue);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  636 */     return finalOutFieldMap;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object processList(LinkedHashMap<String, Object> finalOutFieldMap, List<ServiceOutputField> serviceOutputFieldList, List<ServiceOutputCollection> serviceOutputCollectionList, HashMap<String, Object> serviceFieldMap, boolean isChild, Object parentObj, HashMap<String, Object> collectedRow) throws ClassNotFoundException, InstantiationException, IllegalAccessException, RuleAccessTechnicalException, IOException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ParseException {
/*  651 */     for (ServiceOutputCollection serviceOutputCollection : serviceOutputCollectionList) {
/*      */ 
/*      */ 
/*      */       
/*  655 */       List<String> collectionList = new ArrayList<>();
/*      */       
/*  657 */       String collectionRef = "";
/*      */ 
/*      */ 
/*      */       
/*  661 */       if (serviceOutputCollection.getRef().contains(",")) {
/*      */         
/*  663 */         collectionList = Arrays.asList(serviceOutputCollection.getRef().split(","));
/*      */       } else {
/*  665 */         collectionList.add(serviceOutputCollection.getRef());
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/*  670 */       String outCollectionRef = serviceOutputCollection.getOutRef();
/*      */       
/*  672 */       HashMap<String, Object> collectionFieldMap = new HashMap<>();
/*      */       
/*  674 */       ArrayList<LinkedHashMap<String, Object>> outCollectionsValueList = new ArrayList();
/*      */       
/*  676 */       for (String data : collectionList) {
/*      */         
/*  678 */         String collRef = data;
/*      */         
/*  680 */         collectionRef = "Collection" + collRef;
/*      */         
/*  682 */         if (serviceFieldMap.containsKey(collectionRef) || serviceFieldMap
/*  683 */           .containsKey("FieldMap" + collRef)) {
/*      */ 
/*      */ 
/*      */           
/*  687 */           List<HashMap<String, Object>> outCollectionFieldsList = serviceFieldMap.containsKey(collectionRef) ? (List<HashMap<String, Object>>)serviceFieldMap.get(collectionRef) : (serviceFieldMap.containsKey("FieldMap" + collRef) ? (List<HashMap<String, Object>>)serviceFieldMap.get("FieldMap" + collRef) : new ArrayList<>());
/*      */ 
/*      */           
/*  690 */           for (HashMap<String, Object> collectionFieldsMap : outCollectionFieldsList) {
/*  691 */             LinkedHashMap<String, Object> collectionOutFieldsMap = new LinkedHashMap<>();
/*      */             
/*  693 */             processServiceOutput(serviceOutputCollection.getServiceOutputFieldList(), serviceOutputCollection
/*  694 */                 .getServiceOutputCollection(), serviceOutputCollection
/*  695 */                 .getServiceOutputObject(), collectionFieldsMap, collectionOutFieldsMap, collectedRow);
/*      */ 
/*      */             
/*  698 */             outCollectionsValueList.add(collectionOutFieldsMap);
/*      */           } 
/*      */         } 
/*      */       } 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  706 */       if (isChild) {
/*      */ 
/*      */         
/*  709 */         ReflectionUtility.invokeSetter(parentObj.getClass(), parentObj, outCollectionRef, outCollectionsValueList);
/*      */ 
/*      */ 
/*      */         
/*      */         continue;
/*      */       } 
/*      */ 
/*      */       
/*  717 */       finalOutFieldMap.put(outCollectionRef, outCollectionsValueList);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  725 */     return finalOutFieldMap;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object processObjectFields(List<ServiceOutputObject> serviceOutputObjectList, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow) throws ClassNotFoundException, InstantiationException, IllegalAccessException, RuleAccessTechnicalException, IOException, ParseException {
/*  732 */     Object beanObj = null;
/*  733 */     for (ServiceOutputObject serviceOutputObject : serviceOutputObjectList) {
/*      */       
/*  735 */       List<ServiceOutputField> serviceOutputFieldList = serviceOutputObject.getServiceOutputFieldList();
/*  736 */       String className = serviceOutputObject.getRef();
/*  737 */       String fieldName = serviceOutputObject.getOutRef();
/*  738 */       Class<?> beanClass = Class.forName(className);
/*  739 */       beanObj = beanClass.newInstance();
/*      */       
/*  741 */       for (ServiceOutputField serviceOutputField : serviceOutputFieldList) {
/*      */         
/*  743 */         String outFieldType = serviceOutputField.getOut_field_type();
/*  744 */         String outFieldName = serviceOutputField.getOut_field_name();
/*      */         
/*  746 */         Object serviceFieldValue = extractOutValueFromMap(outFieldType, serviceOutputField, serviceFieldMap, collectedRow);
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         try {
/*  752 */           ReflectionUtility.invokeSetter(beanClass, beanObj, outFieldName, serviceFieldValue);
/*      */         }
/*  754 */         catch (IllegalArgumentException|InvocationTargetException e) {
/*      */ 
/*      */           
/*  757 */           logger.error("Error occurred in processObjectFields due to IllegalArgumentException Exception  ");
/*      */         }
/*  759 */         catch (NoSuchMethodException e) {
/*      */ 
/*      */           
/*  762 */           logger.error("Error occurred in processObjectFields due to NoSuchMethodException Exception  ");
/*      */         }
/*  764 */         catch (SecurityException e) {
/*      */ 
/*      */           
/*  767 */           logger.error("Error occurred in processObjectFields due to SecurityException Exception  ");
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  775 */     return beanObj;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object processObject(LinkedHashMap<String, Object> finalOutFieldMap, List<ServiceOutputField> serviceOutputFieldList, List<ServiceOutputObject> serviceOutputObjectList, HashMap<String, Object> serviceFieldMap, boolean isChild, Object parentObj, HashMap<String, Object> collectedRow) throws IllegalAccessException, ClassNotFoundException, InstantiationException, RuleAccessTechnicalException, IOException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ParseException {
/*  789 */     boolean isChildObj = false;
/*  790 */     for (ServiceOutputObject serviceOutputObject : serviceOutputObjectList) {
/*  791 */       String className = serviceOutputObject.getRef();
/*  792 */       String fieldName = serviceOutputObject.getOutRef();
/*  793 */       Class<?> beanClass = Class.forName(className);
/*  794 */       Object beanObj = beanClass.newInstance();
/*      */       
/*  796 */       serviceOutputFieldList = serviceOutputObject.getServiceOutputFieldList();
/*  797 */       LinkedHashMap<String, Object> finalOutFieldMapTemp = new LinkedHashMap<>();
/*      */       
/*  799 */       if (serviceOutputObject.getServiceOutputObjectList() != null) {
/*      */         
/*  801 */         isChildObj = true;
/*      */ 
/*      */         
/*  804 */         List<ServiceOutputObject> serviceOutputObjectListChild = serviceOutputObject.getServiceOutputObjectList();
/*  805 */         List<ServiceOutputField> serviceOutputFieldListChild = serviceOutputObject.getServiceOutputFieldList();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  813 */         finalOutFieldMapTemp = (LinkedHashMap<String, Object>)processObject(finalOutFieldMapTemp, serviceOutputFieldListChild, serviceOutputObjectListChild, serviceFieldMap, isChildObj, beanObj, collectedRow);
/*      */       } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  821 */       if (serviceOutputObject.getServiceOutputCollectionList() != null) {
/*      */ 
/*      */         
/*  824 */         List<ServiceOutputCollection> serviceOutputCollList = serviceOutputObject.getServiceOutputCollectionList();
/*      */         
/*  826 */         isChildObj = true;
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  831 */         processList(finalOutFieldMap, serviceOutputFieldList, serviceOutputCollList, serviceFieldMap, isChildObj, beanObj, collectedRow);
/*      */       } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  842 */       if (serviceOutputObject.getServiceOutputFieldList() != null)
/*      */       {
/*  844 */         for (ServiceOutputField serviceOutputField : serviceOutputObject.getServiceOutputFieldList()) {
/*      */           
/*  846 */           String outFieldType = serviceOutputField.getOut_field_type();
/*  847 */           String outFieldName = serviceOutputField.getOut_field_name();
/*      */           
/*  849 */           Object serviceFieldValue = extractOutValueFromMap(outFieldType, serviceOutputField, serviceFieldMap, collectedRow);
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           try {
/*  855 */             ReflectionUtility.invokeSetter(beanClass, beanObj, outFieldName, serviceFieldValue);
/*      */           }
/*  857 */           catch (IllegalArgumentException|InvocationTargetException e) {
/*      */ 
/*      */             
/*  860 */             logger.error("Error occurred in processObject due to IllegalArgumentException Exception");
/*      */           }
/*  862 */           catch (NoSuchMethodException e) {
/*      */ 
/*      */             
/*  865 */             logger.error("Error occurred in processObject due to NoSuchMethodException Exception  ");
/*      */           }
/*  867 */           catch (SecurityException e) {
/*      */ 
/*      */             
/*  870 */             logger.error("Error occurred in processObject due to SecurityException Exception ");
/*      */           } 
/*      */         } 
/*      */       }
/*      */ 
/*      */       
/*  876 */       if (isChild) {
/*      */         
/*  878 */         DataTypeReturn retDataType = new DataTypeReturn();
/*  879 */         Class methodType = DataTypeReturn.MethodDataType(parentObj.getClass().getName(), fieldName, "");
/*      */ 
/*      */         
/*  882 */         String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
/*      */         
/*  884 */         Method method = parentObj.getClass().getDeclaredMethod(methodName, new Class[] { methodType });
/*      */         
/*  886 */         Object obj = new Object();
/*  887 */         obj = retDataType.setMethodObject(methodType, beanObj);
/*      */ 
/*      */         
/*  890 */         method.invoke(parentObj, new Object[] { beanObj });
/*      */ 
/*      */ 
/*      */         
/*  894 */         finalOutFieldMap.put(fieldName, parentObj);
/*      */         continue;
/*      */       } 
/*  897 */       finalOutFieldMap.put(fieldName, beanObj);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  906 */     return finalOutFieldMap;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object extractOutValueFromMap(String outFieldType, ServiceOutputField serviceOutputField, HashMap<String, Object> serviceFieldMap, HashMap<String, Object> collectedRow) throws RuleAccessTechnicalException, IOException, ParseException {
/*  918 */     Object serviceFieldValue = null;
/*  919 */     String postProcess = serviceOutputField.getPost_process();
/*  920 */     if (outFieldType.equals(InputSourceType.CONSTANT.toString())) {
/*  921 */       serviceFieldValue = serviceOutputField.getPost_process();
/*  922 */     } else if (outFieldType.equals(InputSourceType.SERVICE_CONTEXT.toString())) {
/*  923 */       String serviceFieldName = serviceOutputField.getService_field_name();
/*      */       
/*  925 */       if (serviceFieldName.contains("FieldMap") || serviceFieldName
/*  926 */         .contains("Collection")) {
/*  927 */         String[] tokens = serviceFieldName.split("\\.");
/*      */         
/*  929 */         String key = tokens[0];
/*  930 */         HashMap<String, Object> subStepFieldMap = new HashMap<>();
/*      */         
/*  932 */         if (tokens.length <= 1) {
/*      */           
/*  934 */           subStepFieldMap = serviceFieldMap.containsKey(key) ? (HashMap<String, Object>)serviceFieldMap.get(key) : new HashMap<>();
/*      */         }
/*      */         else {
/*      */           
/*  938 */           for (int i = 0; i < tokens.length; i++) {
/*      */             
/*  940 */             List<Object> collectionList = new ArrayList();
/*      */             
/*  942 */             subStepFieldMap = serviceFieldMap.containsKey(key) ? (HashMap<String, Object>)serviceFieldMap.get(key) : new HashMap<>();
/*      */           } 
/*      */         } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  951 */         serviceFieldValue = subStepFieldMap.containsKey(tokens[1]) ? subStepFieldMap.get(tokens[1]) : "";
/*      */       
/*      */       }
/*      */       else {
/*      */ 
/*      */         
/*  957 */         serviceFieldValue = serviceFieldMap.containsKey(serviceFieldName) ? serviceFieldMap.get(serviceFieldName) : "";
/*      */       }
/*      */     
/*  960 */     } else if (outFieldType.equals(InputSourceType.UTILITY.toString())) {
/*  961 */       serviceFieldValue = AggregationUtils.callUtilityFromText(postProcess, serviceFieldMap, null, collectedRow);
/*      */     } 
/*      */ 
/*      */     
/*  965 */     if (serviceFieldValue != null && 
/*  966 */       serviceFieldValue.getClass() == String.class) {
/*  967 */       serviceFieldValue = serviceFieldValue.toString().trim();
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  974 */     return serviceFieldValue;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private Object extractOutValueFromList(String outFieldType, ServiceOutputField serviceOutputField, HashMap<String, Object> serviceFieldMap, String[] keys, List<Object> collectionList, HashMap<String, Object> collectedRow) throws RuleAccessTechnicalException, IOException, ParseException {
/*  982 */     HashMap<String, Object> subStepFieldMap = new HashMap<>();
/*  983 */     for (Object obj : collectionList) {
/*      */       
/*  985 */       subStepFieldMap = (HashMap<String, Object>)obj;
/*  986 */       subStepFieldMap = (HashMap<String, Object>)extractOutValueFromMap(outFieldType, serviceOutputField, serviceFieldMap, collectedRow);
/*      */       
/*  988 */       if (subStepFieldMap.size() != 0) {
/*  989 */         return subStepFieldMap;
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  997 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Body loadServiceXml(String xmlConfigFileName) throws AggregatorFrameworkException {
/* 1005 */     Body body = new Body();
/*      */ 
/*      */ 
/*      */     
/* 1009 */     if (serviceXmlObjectMap.containsKey(xmlConfigFileName)) {
/* 1010 */       logger.debug("Parsed XML object found in the xmlObjectMap!");
/* 1011 */       body = serviceXmlObjectMap.get(xmlConfigFileName);
/*      */     } else {
/*      */       
/* 1014 */       logger.debug("XML object not found in the xmlObjectMap. Loading now from memory and loading into the map!");
/*      */       
/*      */       try {
/* 1017 */         body = (Body)Singleton.unmarshaller.unmarshal(new File(XMLPATH + File.separator + xmlConfigFileName));
/* 1018 */       } catch (Exception e) {
/*      */ 
/*      */ 
/*      */         
/* 1022 */         throw new AggregatorFrameworkException(ErrorCode.INVALID_SERVICE_XML.name(), 500, e.getLocalizedMessage());
/*      */       } 
/* 1024 */       serviceXmlObjectMap.put(xmlConfigFileName, body);
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1032 */     return body;
/*      */   }
/*      */ }

