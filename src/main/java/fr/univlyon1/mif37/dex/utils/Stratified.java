package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Collection;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Stratified {

    /**
     *
     * @param mapping
     * @return
     */
    public static boolean safeConfition(Mapping mapping) {

        AtomicBoolean safeConfition = new AtomicBoolean();
        safeConfition.set(true);
        mapping.getTgds().forEach(tgd -> {
            if (!tgd.isSafe()) {
                safeConfition.set(false);
            }
        });

        return safeConfition.get();
    }

    public static boolean isSemiPositif(Tgd tgd) {
        return false;
    }

    /**
     *
     * @return
     */
    public static boolean safeExtendedCondition() {
        return false;
    }

    /**
     *
     * @param mapping
     * @return
     */
    public static boolean isStratified(Mapping mapping) {
        return (safeConfition(mapping) && safeExtendedCondition());
    }

    public static Mapping fromSemipositiveToPositive(Mapping mapping) {
        mapping.getTgds().forEach(tgd -> {
            if (isSemiPositif(tgd)) {

            }
        });

        return mapping;
    }

    public static boolean ExtendedSafetyCondition(Collection<Relation> edbs, Collection<Tgd> tgds){
        for (Relation edb : edbs) {
            String fact = edb.getName();
            boolean is_true = false;
            boolean is_false = false;
            for (Tgd tgd : tgds) {
                Collection<Literal> left = tgd.getLeft();
                for (Literal l : left) {
                    if(l.getAtom().getName().equals(fact)){
                        if(l.getFlag()){is_true=true;}else{is_false=true;}
                    }
                }
            }
            if(is_false&&!is_true){return false;}
        }
        return true;
    }

    public static HashMap createStratum(Collection<Relation> edbs, Collection<AbstractRelation> idbs){
        HashMap stratum = new HashMap();
        for (Relation edb : edbs) {
            stratum.put(edb.getName(), new Integer(1));
        }
        for (AbstractRelation idb : idbs) {
            stratum.put(idb.getName(), new Integer(1));
        }
        return stratum;
    }

    public static boolean checkCount(HashMap s){
        int size = s.size();
        Iterator it = s.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if((int)pair.getValue()>size){return false;}
        }
        return true;
    }

    public static HashMap Stratification(Collection<Relation> edbs, Collection<AbstractRelation> idbs, Collection<Tgd> tgds){
        HashMap stratum = createStratum(edbs,idbs);
        boolean changed = false;
        do{
            changed=false;
            for (Tgd tgd : tgds) {
                Collection<Literal> left = tgd.getLeft();
//                System.out.println("=======================");
//                System.out.println(stratum.toString());
//                System.out.println(tgd.getRight().getName());
//                System.out.println(stratum.get(tgd.getRight().getName()));
//                System.out.println("=======================");
                int head_val = (int) stratum.get(tgd.getRight().getName());
                for (Literal l : left) {
                    int left_val = (int) stratum.get(l.getAtom().getName());
                    if(l.getFlag()){
                        if(left_val>head_val){
                            stratum.put(tgd.getRight().getName(), left_val);
                            changed=true;
                        }
                    }else{
                        if(left_val>=head_val){
                            stratum.put(tgd.getRight().getName(), left_val+1);
                            changed=true;
                        }
                    }
                }
            }
        }while(changed&&checkCount(stratum));
        System.out.println(stratum.toString());
        return stratum;
    }




}
