/*
 * Copyright (C) 2015 PassGo Technology, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.passgo.libproj;


public class passwd {
    public String pwd;
    int k,l,n;
    /** Creates a new instance of passwd */
    public passwd(String pwd) {
        this.pwd=pwd;
    }
    
    public String readablecode() {
        String  code="";
        for (int n=0; n<pwd.length(); n+=2){
         int  k =(int)pwd.charAt(n)-48;
         int  l=(int)pwd.charAt(n+1)-48;
       if (k!=0)  code=code+" ("+k+","+l+")  ";
    
       else switch (l) { 
       
       case 0:
                code=code+"  |   ";
                break;
       case 1: 
                code=code+" Black ";
                break;
       case 2: 
                code=code+" Red ";
                break;
       case 3: 
                code=code+" Blue ";
                break;
       case 4:
                code=code+" Yellow ";
                break;
       case 5:
                code=code+" Pink ";
                break;
       case 6:
                code=code+" Magenta ";
                break;
       case 7:
                code=code+" Green ";
                break;
       case 8:
                code=code+" Cyan ";
    
                  }
        
       
    }
     return code;
    }
    
     public float length() {
         float length=0;  
         for (n=0; n<pwd.length(); n+=2) {
           k=(int)pwd.charAt(n)-48; l=(int)pwd.charAt(n+1)-48; 
            if(k!=0) length++;
         }
           return length;          
     }
    
       public float longestStrokeLength() {
         float strokeLength=0, longestStrokeLength=0;  
         for (n=0; n<pwd.length(); n+=2) {
           k=(int)pwd.charAt(n)-48; l=(int)pwd.charAt(n+1)-48; 
            if(k!=0) strokeLength++;
           else if(l==0) {
               if (strokeLength>longestStrokeLength) longestStrokeLength=strokeLength;
               strokeLength=0;
         }
                }
          return longestStrokeLength;    
       }
     
      public float strokeCount() {
         float strokeCount=0;  
         for (n=0; n<pwd.length(); n+=2) {
           k=(int)pwd.charAt(n)-48; l=(int)pwd.charAt(n+1)-48; 
            if(k==0 && l==0) strokeCount++;
             }
          return strokeCount;    
       }   

         public float colorCount() {
         float colorCount=0;
           if (pwd.length()!=0 && (int)pwd.charAt(0)-48!=0) colorCount=1; 
         for (n=0; n<pwd.length(); n+=2) {
           k=(int)pwd.charAt(n)-48; l=(int)pwd.charAt(n+1)-48; 
            if(k==0 && l!=0) colorCount++;
             }
          return colorCount;    
       }   
      
          public float dotCount() {
         float dotCount=0;
         if (pwd.length()==0) return 0;
         if ((int)pwd.charAt(0)-48!=0 && (int)pwd.charAt(2)-48==0) {
                     dotCount++;
                 };
         for (n=2; n<pwd.length()-3; n+=2){  
             if ((int)pwd.charAt(n)-48!=0) {
                 if ((int)pwd.charAt(n-2)-48==0 && (int)pwd.charAt(n+2)-48==0) {
                     dotCount++;
                 }
             }
         }
         return dotCount;    
       }   
       
         
         public int startfromsorc() {
         
          if (pwd.length()==0) return 1;
          else{
                  k=(int)pwd.charAt(0)-48; l=(int)pwd.charAt(1)-48; 
            
            if(k!=0) {
               if ((k==1 && l==1)|(k==1 && l==9)|(k==9 && l==1)|(k==9 && l==9)|(k==3 && l==3)|(k==3 && l==7)
               |(k==5 && l==5)|(k==7 && l==3)|(k==7 && l==7)) return 1;
               else return 0;
             } else {
             k=(int)pwd.charAt(2)-48; l=(int)pwd.charAt(3)-48;  
              if ((k==1 && l==1)|(k==1 && l==9)|(k==9 && l==1)|(k==9 && l==9)|(k==3 && l==3)|(k==3 && l==7)
               |(k==5 && l==5)|(k==7 && l==3)|(k==7 && l==7)) return 1;
               else return 0;
             }
          }
         }   


         public int endbysorc() {
           
             k=(int)pwd.charAt(pwd.length()-4)-48; l=(int)pwd.charAt(pwd.length()-3)-48; 
            
               if ((k==1 && l==1)|(k==1 && l==9)|(k==9 && l==1)|(k==9 && l==9)|(k==3 && l==3)|(k==3 && l==7)
               |(k==5 && l==5)|(k==7 && l==3)|(k==7 && l==7)) return 1;
               else return 0;
         
         }

}
    
