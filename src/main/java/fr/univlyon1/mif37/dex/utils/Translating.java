package fr.univlyon1.mif37.dex.utils;

import com.sun.org.apache.xpath.internal.operations.Bool;
import fr.univlyon1.mif37.dex.mapping.*;

import java.util.Collection;
import java.util.*;

/**
 * Created by firas on 17/05/2017.
 */
public class Translating {

    private Translating(){

    }

    public static void translate(Collection<Relation> edbs, Collection<AbstractRelation>
            idbs, Collection<Tgd> tgds){
        Map<String, Integer> tables = new HashMap<>();
        String values = "";
        for(Relation edb : edbs) {
            if(!tables.containsKey(edb.getName())){
                tables.put(edb.getName(),edb.getAttributes().length);
            }else{
                if(tables.get(edb.getName())!=edb.getAttributes().length){
                    tables.put(edb.getName(),edb.getAttributes().length);
                }
            }
            values+=createValues(edb.getName(),edb.getAttributes());
        }
        // CREATE Tables Code
        for(Map.Entry<String, Integer> s : tables.entrySet()){
            System.out.println(createTable(s.getKey(),s.getValue()));
        }
        // INSERT values code
        System.out.println(values);

        for(AbstractRelation idb : idbs) {
            //System.out.println(idb.getName());

        }

        List<String> rec = getRecursive(tgds);
        for (Tgd tgd : tgds) {
            if(!rec.contains(tgd.getRight().getName())){
                System.out.println(ruleSQL(tgd));
            }
        }
        for(String r : rec){
            System.out.println(ruleSQLRecursive(tgds, r));
        }


    }

    /**
     * La méthode retourne un String de creation d'une table dans la base de donnees
     * @param name le nom de table
     * @param num le numero de "columns"
     * @return String de creation de table
     */
    public static String createTable(String name, Integer num){
        String statement = "";
        statement += "DROP TABLE "+name+" ;\n";
        statement += "CREATE TABLE "+name+"( \n";

        for (int i = 1; i<=num;i++){
            statement+="\tc"+i+" VARCHAR(150)";
            if(i!=num){statement+=", ";}
            statement+="\n";
        }
        statement += ");";
        return statement;
    }

    /**
     * La méthode retourne un String de insertation des valeurs dans la BD
     * @param name le nom de table
     * @param values les valuers
     * @return String d'insertation
     */
    public static String createValues(String name, String[] values){
        String statement = "INSERT INTO "+name+" VALUES (";
        for (int i = 0; i<values.length;i++){
            statement+="'"+values[i]+"'";
            if(i!=(values.length-1)){statement+=", ";}
        }
        statement += ");\n";
        return statement;
    }

    /**
     * Une fonction pour déterminer si la regle est récursif ou pas
     * @param tgd
     * @return boolean
     */
    public static boolean isRecursive(Tgd tgd){
        String head = tgd.getRight().getName();
        for(Literal l : tgd.getLeft()) {
            if(l.getAtom().getName().equals(head)){
                return true;
            }
        }
        return false;
    }
    /*
    isRecursive pour Tgds
     */
    public static boolean isRecursive(Collection<Tgd> tgds){
        for(Tgd tgd : tgds) {
            String head = tgd.getRight().getName();
            for (Literal l : tgd.getLeft()) {
                if (l.getAtom().getName().equals(head)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getRecursive(Collection<Tgd> tgds){
        List<String> rec = new ArrayList<>();
        for(Tgd tgd : tgds) {
            String head = tgd.getRight().getName();
            for (Literal l : tgd.getLeft()) {
                if (l.getAtom().getName().equals(head)) {
                    rec.add(tgd.getRight().getName());
                }
            }
        }
        return rec;
    }
    /**
     * Une fonction pour déterminer si le code est positif ou pas
     * @param tgd
     * @return boolean
     */
    public static boolean isPositive(Tgd tgd){
        for(Literal l : tgd.getLeft()) {
            if(!l.getFlag()){
                return false;
            }
        }
        return true;
    }

    /**
     * Une méthode pour générer le code sql pour les règles
     * @param tgd
     * @return String (sql)
     */
    public static String ruleSQL(Tgd tgd){
        String statement = "";
        statement += "CREATE or REPLACE VIEW V_"+tgd.getRight().getName()+" AS\n";
        statement +=ruleSelect(tgd);
        statement +=ruleFrom(tgd.getLeft());
        if(isPositive(tgd)){
            statement +=ruleWhere(tgd.getLeft());
        }else{
            statement +=ruleWhere_negative(tgd.getLeft());
        }
        return statement+";";
    }

    public static String ruleSQLRecursive(Collection<Tgd> tgds, String name){
        String statement = "";
        statement += "CREATE or REPLACE VIEW V_" + name + " AS\n";
        statement += "WITH rec_"+name+" AS (";
        Boolean first = true;
        for(Tgd tgd : tgds) {
            if(tgd.getRight().getName().equals(name)){
                if(first){statement += "(\n";first=false;}else{statement += "UNION ALL (\n";}
                statement += ruleSelect(tgd);
                statement += ruleFrom(tgd.getLeft());
                if (isPositive(tgd)) {
                    statement += ruleWhere(tgd.getLeft());
                } else {
                    statement += ruleWhere_negative(tgd.getLeft());
                }
                statement += ")\n";
            }
        }
        statement += ") SELECT * FROM rec_"+name+" ";
        return statement+";";
    }

    /**
     * Une méthode pour créer la ligne "SELECT"
     * @param tgd
     * @return SELECT String
     */
    public static String ruleSelect(Tgd tgd){
        String res = "SELECT ";
        int size = tgd.getRight().getVars().size();
        int i = 1;
        for(Variable v : tgd.getRight().getVars()){
            res += findVariable(v.getName(), tgd.getLeft()) + " as c"+i;
            if(i!=size){res+=", ";}
            i++;
        }
        return res+"\n";
    }

    /**
     * Une méthode pour créer la ligne "FROM"
     * @param left
     * @return
     */
    public static String ruleFrom(Collection<Literal> left){
        String res = "FROM ";
        int size = left.size();
        int i = 1;
        for(Literal l : left){
            res+= l.getAtom().getName()+" "+ l.getAtom().getName()+i;
            if(i!=size){res+=", ";}
            i++;
        }
        return res+"\n";
    }

    /**
     * Une méthode pour créer les conditions de "WHERE"
     * @param left
     * @return
     */
    public static String ruleWhere(Collection<Literal> left){
        String res = "";
        List<String> temp = new ArrayList<>();
        int i = 1;
        int k = 0;
        for(Literal l : left){
            int j = 1;
            for(Variable v : l.getAtom().getVars()){
                if(!temp.contains(l.getAtom().getName()+i+".c"+j)){
                    temp.add(l.getAtom().getName()+i+".c"+j);
                    String find = findUnusedVariable2(v.getName(),l.getAtom().getName()+ i +
                            ".c" + j,left,temp);
                    if(!find.equals("")){
                        if(k==0){res += "WHERE ";}
                        if(i!=1){res+="AND ";}
                        res+=l.getAtom().getName()+i+".c"+j+" = "+find+" \n";
                        temp.add(find);
                        k++;
                    }
                }
                j++;
            }
            i++;
        }
        return res+"";
    }


    // TODO code optimization
    public static String ruleWhere_negative(Collection<Literal> left){
        String res = "";
        List<String> temp = new ArrayList<>();
        HashMap<String, Map> where_list2  = new HashMap<>();
        int i = 1;
        Map<String, Boolean> flag_list = new HashMap<>();
        Map<String, Boolean> flag_list2 = new HashMap<>();
        for(Literal l : left){
            int j = 1;
            Map<String, String> where_list = new HashMap<>();
            for (Variable v : l.getAtom().getVars()) {
                String var_id = l.getAtom().getName() + i + ".c" + j;
                if (!temp.contains(var_id)) {
                    String i_res = findUnusedVariable2(v.getName(),l.getAtom().getName() + i
                            + ".c" + j, left, temp);
                    if(!i_res.equals("")){
                        where_list.put(var_id, i_res);
                        if(l.getFlag()){
                            flag_list2.put(var_id,true);
                        }else{
                            flag_list2.put(var_id,false);
                        }
                    }
                }
                j++;
            }
            where_list2.put(l.getAtom().getName()+ i, where_list);
            if(l.getFlag()){
                flag_list.put(l.getAtom().getName()+ i,true);
            }else{
                flag_list.put(l.getAtom().getName()+ i,false);
            }
            i++;
        }

        String where_stm = "";
        for (Map.Entry<String, Map> e : where_list2.entrySet()) {
            Map<String, String> value = e.getValue();
            if(flag_list.get(e.getKey())==true){
                int k = 0;
                for (Map.Entry<String, String> e2 : value.entrySet()) {
                    if(flag_list2.get(e2.getValue())!=false) {
                        if (k != 0) {res += "AND ";}
                        where_stm += e2.getKey()+" = "+ e2.getValue() + "\n";
                        k++;
                    }
                }
            }else{
                for(int u = 0;u<value.size();u++){
                    int k = 0;
                    for (Map.Entry<String, String> e2 : value.entrySet()) {
                        if(k!=0){
                            res += "AND ";
                        }
                        where_stm += e2.getKey();
                        if(u==k){
                            where_stm += " <> ";
                        }else{
                            where_stm += " = ";
                        }
                        where_stm += e2.getValue() + "\n";
                        k++;
                    }
                    if(u!=value.size()-1)
                        where_stm+="OR ";
                }
            }
        }
        if(where_list2.size()>0&&!where_stm.equals("")){
            res+="WHERE "+where_stm;
        }
        return res+"\n";
    }



    public static String findVariable(String var, Collection<Literal> left){
        int i = 1;
        for(Literal l : left){
            int j = 1;
            for(Variable v : l.getAtom().getVars()){
                if(v.getName().equals(var)){
                    return l.getAtom().getName()+i+".c"+j;
                }
                j++;
            }
            i++;
        }
        return "";
    }


    public static String findUnusedVariable2(String var,String var_name,
                                             Collection<Literal> left, List<String> temp){
        int i = 1;
        for(Literal l : left){
            int j = 1;
            for(Variable v : l.getAtom().getVars()){
                if(v.getName().equals(var)&&!(var_name.equals(l.getAtom().getName()+i
                        +".c"+j))){
                    return l.getAtom().getName()+i+".c"+j;
                }
                j++;
            }
            i++;
        }
        return "";
    }
}
