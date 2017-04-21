package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Literal;
import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Stratified {

    /**
     * Que dans la règle on ne trouve pas des variables dans le head qui ne sont pas dans le corps,
     * alors le programme est safeCondition
     *
     * @param mapping du programme
     * @return si le programme a ou pas le safeCondition
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


    /**
     * Un programme c'est positif si aucun règle dans les tdgb sont negatifs.
     *
     * Ref: Example1.txt
     *
     * @param mapping le mapping du programme
     * @return si c'est ou pas positif
     */
    public static boolean isPositif (Mapping mapping) {
        AtomicBoolean positif = new AtomicBoolean();
        positif.set(true);
        mapping.getTgds().forEach(tgd -> {
            if (tgd.getLeft().stream().anyMatch(l -> !l.getFlag()))
                positif.set(false);
        });

        return positif.get();
    }

//    static Relation isSemiPositif(Collection<Relation> edbs, Tgd tgd) {
//
//        boolean is_true = false;
//        boolean is_false = false;
//
//        for (Relation edb : edbs) {
//            String fact = edb.getName();
//            Collection<Literal> left = tgd.getLeft();
//            for (Literal l : left) {
//                if (l.getAtom().getName().equals(fact)) {
//                    // si on trouve dans la règle un fait nie
//                    if (!l.getFlag()) {
//                        is_true = true;
//                    } else {
//                        // si on le retrouve positif
//                        is_false = true;
//                    }
//                }
//            }
//
//            if (is_false && !is_true) {
//                return edb;
//            }
//        }
//
//        return null;
//    }

    /**
     * C'est stratified si il a une negation dans le corps de'une regle d'une autre regle qui a été
     * définie avant
     *
     * Ref:
     *
     * @param mapping du programme
     * @return si le programme est stratifié ou pas
     */
    public static boolean isStratified(Mapping mapping) {
        AtomicBoolean stratified = new AtomicBoolean();
        stratified.set(false);

        if (mapping.getTgds().stream().anyMatch(tgd ->
            tgd.getLeft().stream().anyMatch(l ->
                    !l.getFlag() && mapping.getTgds().stream()
                            .anyMatch(tgd1 -> tgd1.getRight().getName().equals(l.getAtom().getName())))
        )) {
            stratified.set(true);
        }
        return stratified.get();
    }

//    public static Mapping fromSemipositiveToPositive(Mapping mapping) {
//        List<Relation> newEdbs = new ArrayList<>();
//
//        mapping.getTgds().forEach(tgd -> {
//            Relation relation = isSemiPositif(mapping.getEDB(), tgd);
//            String name = "new" + relation.getName();
//
//            List<String> attributs = Arrays.asList(relation.getAttributes());
//            Relation newRelation = new Relation(name, attributs);
//
//
//        });
//
//        Mapping mappingPositif = new Mapping();
//        mappingPositif.getTgds().addAll(mapping.getTgds());
//
//        return mapping;
//    }

    /**
     * Un programme c'est semipositif si il existe un règle dans laquelle il y a un fait qui est nié
     * Ref: Example2.txt
     * @param edbs
     * @param tgds
     * @return si le programme est ou pas semipositif
     */
    public static boolean isSemiPositif(Collection<Relation> edbs, Collection<Tgd> tgds){
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
