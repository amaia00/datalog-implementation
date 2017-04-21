package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Literal;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Stratified {

    public static boolean allVariablesInBody(Tgd tgd) {
        //tgd.getRight().getVars().
        //tgd.getLeft().stream().anyMatch()

        // tgd.getRight().getVars().forEach(v -> tgd.getLeft().stream().anyMatch(l -> l.getAtom().getVars().stream().anyMatch(v.getName())));
        return false;
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
