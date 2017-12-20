package com.wssc.filenet.migration.ce;

    public class list {

        public static void main(String[] args) {
        	
        	String appendEndQuery = ") ORDER BY DOCUMENTTITLE";
String CommaSeparated = "item1,item-2,ACCKBE3452A02-CBI-39,BOAK-CBI-343";     
//ArrayList<String> items = new ArrayList(Arrays.asList(CommaSeparated.split("\\s*,\\s*")));
String replaced = CommaSeparated.replace(",", "%' OR '%");

   // System.out.println(replaced);

String query= "SELECT  * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) AND (DOCUMENTTITLE like '%"+replaced+"%'"+appendEndQuery;
System.out.println(query);
        }
    }





