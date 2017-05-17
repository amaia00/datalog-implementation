package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Relation;

import java.util.Collection;
import java.util.*;

/**
 * Created by firas on 17/05/2017.
 */
public class Translating {

    private Translating(){

    }

    public static void translate(Collection<Relation> edbs){
        Map<String, Integer> tables = new HashMap<>();
        Map<String, String[]> values = new HashMap<>();
        for(Relation edb : edbs) {
            if(!tables.containsKey(edb.getName())){
                tables.put(edb.getName(),edb.getAttributes().length);
            }else{
                if(tables.get(edb.getName())!=edb.getAttributes().length){
                    tables.put(edb.getName(),edb.getAttributes().length);
                }
            }
            values.put(edb.getName(),edb.getAttributes());
        }
        // CREATE Tables Code
        for(Map.Entry<String, Integer> s : tables.entrySet()){
            System.out.println(createTable(s.getKey(),s.getValue()));
        }
        // INSERT values code
        for(Map.Entry<String, String[]> v : values.entrySet()){
            System.out.println(v.getKey()+ " == "+ v.getValue().length);
        }

    }

    public static String createTable(String name, Integer num){
        String statement = "CREATE TABLE "+name+"( \n";
        for (int i = 1; i<=num;i++){
            statement+="\t"+name+"."+i+" VARCHAR(150)";
            if(i!=num){statement+=", ";}
            statement+="\n";
        }
        statement += ");";
        return statement;
    }
}
