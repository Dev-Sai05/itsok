/*    */ package com.tcs.bancs.microservices.comparator;
/*    */ 
/*    */ import com.tcs.bancs.microservices.domain.SubServiceStep;
/*    */ import java.util.Comparator;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SubServiceStepComparator
/*    */   implements Comparator<SubServiceStep>
/*    */ {
/*    */   public int compare(SubServiceStep o1, SubServiceStep o2) {
/* 14 */     Object exec_ord1 = o1.getSubStepOrder();
/* 15 */     Object exec_ord2 = o2.getSubStepOrder();
/*    */     
/* 17 */     exec_ord1 = (exec_ord1 == null) ? "" : exec_ord1;
/* 18 */     exec_ord2 = (exec_ord2 == null) ? "" : exec_ord2;
/*    */     
/* 20 */     if (!exec_ord1.toString().matches("\\d+")) {
/* 21 */       exec_ord1 = "99";
/*    */     }
/* 23 */     if (!exec_ord2.toString().matches("\\d+")) {
/* 24 */       exec_ord2 = "99";
/*    */     }
/* 26 */     if (Integer.parseInt(exec_ord1.toString()) <= Integer.parseInt(exec_ord2.toString())) {
/* 27 */       return -1;
/*    */     }
/* 29 */     return 1;
/*    */   }
/*    */ }

