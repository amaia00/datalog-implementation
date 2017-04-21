package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Literal;
import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

}
