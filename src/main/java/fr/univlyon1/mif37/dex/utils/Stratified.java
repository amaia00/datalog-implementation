package fr.univlyon1.mif37.dex.utils;


import fr.univlyon1.mif37.dex.mapping.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Stratified {

    private Stratified() {
        /* On cache le constructeur*/
    }


    /**
     * Que dans la règle on ne trouve pas des variables dans le head qui ne sont pas dans le corps,
     * alors le programme est safeCondition
     *
     * @param mapping du programme
     * @return si le programme a ou pas le safeCondition
     */
    public static boolean safeConfition(Mapping mapping) {

        AtomicBoolean condition = new AtomicBoolean();
        condition.set(true);

        mapping.getTgds().forEach(tgd -> {
            if (!tgd.isSafe()) {
                condition.set(false);
            }
        });

        return condition.get();
    }


    /**
     * Un programme c'est positif si aucun règle dans les tdgb sont negatifs.
     * <p>
     * Ref: Example1.txt
     *
     * @param mapping le mapping du programme
     * @return si c'est ou pas positif
     */
    public static boolean isPositif(Mapping mapping) {
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
     * <p>
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
     *
     * @param edbs les faits du programme
     * @param tgds les regles du programme
     * @return si le programme est ou pas semipositif
     */
    public static boolean isSemiPositif(Collection<Relation> edbs, Collection<Tgd> tgds) {

        for (Relation edb : edbs) {
            boolean isTrue = false;
            boolean isFalse = false;
            String fact = edb.getName();

            for (Tgd tgd : tgds) {
                Collection<Literal> left = tgd.getLeft();
                for (Literal l : left) {
                    if (l.getAtom().getName().equals(fact)) {
                        if (l.getFlag()) {
                            isTrue = true;
                        } else {
                            isFalse = true;
                        }
                    }
                }
            }

            if (isFalse && !isTrue) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     *
     * @param stratum la liste de prédicats avec le stratum
     * @return si on doit continuer l'algo de stratification ou pas.
     */
    public static boolean checkCount(Map stratum) throws Exception {
        int size = stratum.size();
        Iterator it = stratum.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((int) pair.getValue() > size) {
                throw new Exception("C'est pas stratifiable");
            }
        }
        return true;
    }

    /**
     *
     * La stratification vérifié si le programme est stratifiable et retourne la liste
     * des prédicats avec son stratums
     *
     * @param edbs les faits dans le programme
     * @param idbs le nom des regles
     * @param tgds les regles du programme
     * @return le stratum des prédicats
     */
    public static Map<String, Integer> stratification(Collection<Relation> edbs, Collection<AbstractRelation> idbs, Collection<Tgd> tgds) throws Exception {
        Map<String, Integer> stratum = Util.createStratum(edbs, idbs);
        boolean changed;

        do {
            changed = false;
            for (Tgd tgd : tgds) {
                Collection<Literal> left = tgd.getLeft();
                int headVal = stratum.get(tgd.getRight().getName());
                for (Literal l : left) {
                    int leftVal = stratum.get(l.getAtom().getName());

                    if (l.getFlag()) {
                        if (leftVal > headVal) {
                            stratum.put(tgd.getRight().getName(), leftVal);
                            changed = true;
                        }
                    } else {

                        if (leftVal >= headVal) {
                            stratum.put(tgd.getRight().getName(), leftVal + 1);
                            changed = true;
                        }
                    }
                }
            }
        } while (changed && checkCount(stratum));

        return stratum;
    }


}
