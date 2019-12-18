package com.tigerit.exam;


import static com.tigerit.exam.IO.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 */
public class Solution implements Runnable {
    Integer tb;
    InputOutput sc=new InputOutput();
    Table []table=new Table[15];
    Column []columns=new Column[15];
    ArrayList<ArrayList<Integer>> []record = new ArrayList[15]; // data of all tables
    HashMap<String,Table> MapTable=new HashMap<String, Table>();
    @Override
    public void run(){
        Integer test=sc.readLineAsInteger();
        for(int testcase=1;testcase<=test;testcase++){
            Integer noOfTable=sc.readLineAsInteger();

            for (tb=0;tb<noOfTable;tb++){
                table[tb]=new Table();
                //input table name
                String tableName=sc.readLine();

                table[tb].setName(tableName);

                Integer noOfCol,noOfRow;
                //input column and row as string
                String line=sc.readLine();
                //separate row and columns by space
                String []pattern=line.split(" ");

                noOfCol=Integer.parseInt(pattern[0]);
                noOfRow=Integer.parseInt(pattern[1]);

                table[tb].setCol(noOfCol);
                table[tb].setRow(noOfRow);
                table[tb].setId(tb);
                //mapping table
                MapTable.put(tableName,table[tb]);

                //input column names
                InputColumns(table[tb],tb);

                //input records
                InputRecord(table[tb]);
            }
            sc.printLine("Test: " + testcase);
            Integer noOfQuery=sc.readLineAsInteger();

            for (int q=1;q<=noOfQuery;q++){
                ArrayList<String> []tokenLine = new ArrayList[5];
                tokenLine=InputQuery();
                Process(tokenLine);
                sc.readLine(); // empty line
            }
        }
    }
    public ArrayList<String>[] InputQuery(){
        ArrayList<String> []tokenLine = new ArrayList[5];
        for(int i = 0; i < 4; i++) {
            tokenLine[i] = new ArrayList<String>();
            String line = sc.readLine();
            tokenLine[i] = tokenize(line);
        }
        return tokenLine;
    }
    public void Process(ArrayList<String> []tokenLine){
        String firstTableShortName = new String();
        String firstTable = tokenLine[1].get(1);

        Table tab=MapTable.get(firstTable); // table id of first table
        int firstTableId =tab.getId();
        if(tokenLine[1].size()>2)
            firstTableShortName = tokenLine[1].get(2);
        Table tab2=MapTable.get(tokenLine[2].get(1));
        int secondTableId = tab2.getId(); // table id of second table

        int table_id;

        //before equal sign
        ArrayList<String> separate = separation(tokenLine[3].get(1)); // first_table.first_column
        if(separate.get(0).equals(firstTable) || separate.get(0).equals(firstTableShortName))
            table_id = firstTableId;
        else table_id = secondTableId;


        // column id of first table
        int firstColId = columns[table_id].getColumnId(separate.get(1));


        // after equal sign
        // second_table.second_column
        separate = separation(tokenLine[3].get(2));

        if(separate.get(0).equals(firstTable) || separate.get(0).equals(firstTableShortName))
            table_id = firstTableId;
        else table_id = secondTableId;

        //  column id of second table
        int secondColId = columns[table_id].getColumnId(separate.get(1));


        // printed columns
        ArrayList<Item> selectQuery = new ArrayList<Item>();
        boolean selectAll = false;

        if (tokenLine[0].size()==1) {
            selectAll = true;
        }
        else {

            // take each pair of table_name.column_name from first line
            for(int i = 1; i < tokenLine[0].size(); i++) {

                Item item = new Item();
                ArrayList<String> cur = separation(tokenLine[0].get(i)); // partition at dot

                if (cur.get(0).equals(firstTable) || cur.get(0).equals(firstTableShortName))
                    item.tableId = firstTableId;
                else item.tableId = secondTableId;

                item.ColumnId =columns[item.tableId].getColumnId(cur.get(1));
                selectQuery.add(item);
            }
        }

        //store join query result
        ArrayList<ArrayList<Integer>> joinQueryResult = new ArrayList<ArrayList<Integer>>();


        joinQueryResult=joinQueryProcess(selectQuery,firstTableId,secondTableId,firstColId,secondColId,selectAll);


        // sort the rows in lexicographical order
        Collections.sort(joinQueryResult, new Comparator<ArrayList<Integer>>() {
            @Override
            public int compare(ArrayList<Integer> r1, ArrayList<Integer> r2){
                for(int i = 0; i < r1.size(); i++) {
                    if(r1.get(i)>r2.get(i)) return 1;
                    if(r1.get(i) < r2.get(i)) return -1;
                }
                return 0;
            }
        });

        // print the column names...
        printColumn(tokenLine,firstTableId,secondTableId,selectAll);

        // print new line
        sc.printLine("");

        // print the result table
        for(ArrayList<Integer> row:joinQueryResult) {
            for(int k = 0; k < row.size();k++) {
                if(k>0) System.out.print(" ");
                System.out.print(row.get(k));
            }
            sc.printLine("");
        }
        sc.printLine("");



    }

    //join query helper function
    private ArrayList<ArrayList<Integer>> joinQueryProcess(ArrayList<Item> selectQuery, Integer firstTableId, Integer secondTableId, Integer firstColId, Integer secondColId, boolean selectAll){
        ArrayList<ArrayList<Integer>> joinQueryResult = new ArrayList<ArrayList<Integer>>();
        // start to find the matching

        // for each row of first table
        for(int i = 0; i < record[firstTableId].size(); i++) {


            // for each row of second table
            for(int j = 0; j < record[secondTableId].size(); j++) {

                // if both values at specific columns are equal, then..
                if(record[firstTableId].get(i).get(firstColId) == record[secondTableId].get(j).get(secondColId)) {

                    // create a new row
                    ArrayList<Integer> cur = new ArrayList<Integer>();

                    if(selectAll == true) { // insert all column data from both table
                        cur.addAll(record[firstTableId].get(i));
                        cur.addAll(record[secondTableId].get(j));
                        joinQueryResult.add(cur);
                        continue;
                    }
                    // add the values of the selected columns
                    for(int k = 0; k < selectQuery.size(); k++) {
                        Integer value = 0;
                        if(selectQuery.get(k).tableId == firstTableId)
                            value = record[firstTableId].get(i).get(selectQuery.get(k).ColumnId);
                        else
                            value = record[secondTableId].get(j).get(selectQuery.get(k).ColumnId);
                        cur.add(value);
                    }
                    joinQueryResult.add(cur);
                }
            }
        }
       return joinQueryResult;
    }

    //print selected column helper function
    private void printColumn(ArrayList<String> []tokenLine,Integer firstTableId,Integer secondTableId,boolean selectAll){
        if(selectAll == true) {
            // print all column names of both table
            boolean sp = false;
            for(int i = 0; i < columns[firstTableId].getColList().size(); i++) {
                if(sp) System.out.print(" ");
                System.out.print(columns[firstTableId].getColList().get(i));
                sp = true;
            }
            for(int i = 0; i < columns[secondTableId].getColList().size(); i++) {
                if(sp) System.out.print(" ");
                System.out.print(columns[secondTableId].getColList().get(i));
                sp = true;
            }
        }
        else {
            // print the selected column names
            for(int i = 1; i < tokenLine[0].size(); i++) {
                if(i>1) System.out.print(" ");
                System.out.print(separation(tokenLine[0].get(i)).get(1));
            }
        }
    }



    //helper function for column input
    private void InputColumns(Table newtable,Integer noOfTable){
        Integer t;
        Integer c;
        t=newtable.getId();
        c=newtable.getCol();
        columns[noOfTable]=new Column();
        ArrayList<String> collist=new ArrayList<String>();
        HashMap<String,Integer> colid=new HashMap<String, Integer>();
        String line=sc.readLine();
        String []lines=line.split(" ");
        for(int i=0;i<c;i++){
            colid.put(lines[i],i);
            collist.add(lines[i]);
        }
        columns[noOfTable].setColId(colid);
        columns[noOfTable].setColList(collist);
        columns[noOfTable].tableId=t;

    }



    // string Tokenization helper function
    private ArrayList<String> tokenize(String text){
        ArrayList<String> tokens = new ArrayList<String>();
        String word = new String();
        int i = 0;
        while(text.charAt(i)!=' '){
            word += text.charAt(i);
            i++;
        }

        // add first token: select, from, join, where
        tokens.add(word);
        word = "";

        i++;
        if(text.charAt(i) == '*') {
            return tokens;
        }
        for(; i <= text.length(); i++) {
            if(i == text.length() || text.charAt(i) == ' ' || text.charAt(i)==',' || text.charAt(i)=='=') {
                if(word.length()>0)
                    tokens.add(word);
                word = "";
                while(i + 1 < text.length() && ( text.charAt(i+1)==' ' || text.charAt(i+1) == ',' || text.charAt(i+1)== '=' ) )
                    i++;
            }
            else word += text.charAt(i);
        }
        return tokens;
    }

    //helper function for record input
    private void InputRecord(Table newTable){
        Integer t=newTable.getId();
        Integer c=newTable.getCol();
        Integer r=newTable.getRow();
        record[t] = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i <r; i++) {
            ArrayList<Integer> rec = new ArrayList<Integer>();
            String line = sc.readLine();
            String []lines = line.split(" ");
            for(int j = 0;j<c; j++) {
                Integer value = Integer.parseInt(lines[j]);
                rec.add(value);
            }
            record[t].add(rec);
        }
    }


    // separate the table  name and column names
    private ArrayList<String> separation(String text){
        ArrayList<String> token = new ArrayList<String>();
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '.') {
                token.add(text.substring(0,i));
                token.add(text.substring(i+1));
                return token;
            }
        }
        token.add(text);
        return token;
    }



    class Table{
        private String name;
        private Integer id,col,row;
         public Table(){

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getCol() {
            return col;
        }

        public void setCol(Integer col) {
            this.col = col;
        }

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }
    }


    //Column
    class Column {
        private Integer tableId;
        private ArrayList<String> colList = new ArrayList<String>(); // list of all column names of  a table
        private HashMap<String, Integer> colId = new HashMap<String, Integer>();
        public Column(){

        }

        public Integer getTableId() {
            return tableId;
        }

        public void setTableId(Integer tableId) {
            this.tableId = tableId;
        }

        public ArrayList<String> getColList() {
            return colList;
        }

        public void setColList(ArrayList<String> colList) {
            this.colList = colList;
        }

        public HashMap<String, Integer> getColId() {
            return colId;
        }

        public void setColId(HashMap<String, Integer> colId) {
            this.colId = colId;
        }
        public Integer getColumnId(String  c){
            return this.colId.get(c);
        }
        public String getColumnName(Integer c){
            for (String key : this.colId.keySet()) {
                if (c == this.colId.get(key)) {
                    return  key;
                }
            }
            return null;
        }
    }


    //Input Output helper class
    class InputOutput{
        public InputOutput(){

        }
        private   final BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
        public  String readLine(){
            String value;
            try{
                value=  reader.readLine();
            }catch (Exception e){
                value=null;
            }
            return value;
        }
        public  Integer readLineAsInteger(){
            return Integer.parseInt(readLine());
        }
        public  void printLine(Object ob){
            System.out.println(ob);
        }
    }


    class Item{
        Integer tableId,ColumnId;
        public Item(){

        }
    }

}
