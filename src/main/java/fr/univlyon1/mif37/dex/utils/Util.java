package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.AbstractRelation;
import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Util {
    private static final int ONE = 1;

    /**
     * La méthode retourne la liste de constants dans le EDB
     *
     * @param mapping le program qu'on est en train d'evaluer
     * @return la liste de constants
     */
    static Set<Relation> getAllConstants (Mapping mapping) {
        return new HashSet<>(mapping.getEDB());
    }

    /**
     *
     *
     *
     * @param edbs les faits dans le programme
     * @param idbs le nom des regles
     * @return
     */
    static Map<String, Integer> createStratum (Collection<Relation> edbs, Collection<AbstractRelation> idbs) {
        HashMap<String, Integer> stratum = new HashMap<>();
        for (Relation edb : edbs) {
            stratum.put(edb.getName(), ONE);
        }

        for (AbstractRelation idb : idbs) {
            stratum.put(idb.getName(), ONE);
        }
        return stratum;
    }


    /**
     *
     * Retourne la liste des IDBS + EDBS avec un liste pour calculer la définition de chaque
     * clause
     *
     * @param mapping le programme
     * @return la liste des clauses
     */
    static Map getSymbols (Mapping mapping) {
        Map<String, Set<Integer>> clauses = new HashMap<>();

        mapping.getEDB().forEach(edb -> clauses.put(edb.getName(), null));
        mapping.getIDB().forEach(idb -> clauses.put(idb.getName(), null));

        return clauses;
    }

    /**
     * Symbol Definitions. The definition def(s) of a program symbol s is the set of all program clauses that
     * have the symbol p in their head atom.
     *
     * @param mapping le programme
     * @param symbol le symbole qu'on est en train de traiter
     * @return la liste de clauses dans laquelle le symbol est présent
     */
    static Set<Integer> getSymbolDefinition (Mapping mapping, String symbol) {
        Set<Integer> definition = new HashSet<>();

        AtomicInteger clauseCounter = new AtomicInteger();
        clauseCounter.set(0);
        mapping.getEDB().forEach(edb -> {
            clauseCounter.incrementAndGet();
            if (edb.getName().equals(symbol))
                definition.add(clauseCounter.get());
        });

        mapping.getTgds().forEach(tgd -> {
            clauseCounter.incrementAndGet();
            if (tgd.getRight().getName().equals(symbol))
                definition.add(clauseCounter.get());

            if (tgd.getLeft().stream().anyMatch(l -> l.getAtom().getName().equals(symbol))) {
                definition.add(clauseCounter.get());
            }
        });

        return definition;
    }


//    /**
//     * WRONG!
//     *
//     * @param symbolsDefinitions tous les symbols du programme avec son definition
//     * @return les slices du programme
//     */
////    static Map<Integer, List<String>> getProgramSlices(Map<String, Set<Integer>> symbolsDefinitions,
//                                                       Map<String, Integer> stratumMap,
//                                                       Mapping mapping) {
//        Map<Integer, List<String>> slices = new HashMap<>();
//        int qteSymbols = 1;
//
//        stratumMap.forEach((symbol, stratum) -> {
////            symbolsDefinitions.forEach((symbol, clauses) -> {
////                if (clauses.contains(qteSymbols)) {
////                    List<String> list = slices.get(qteSymbols);
////                    if (list != null)
////                        slices.get(qteSymbols).add(symbol);
////                    else {
////                        list = new ArrayList<>();
////                        list.add(symbol);
////                        slices.put(qteSymbols, list);
////                    }
////                }
////            });
//
//            symbolsDefinitions.entrySet().stream().filter(symbolName -> symbol.equals(symbolName.getKey()))
//                    .findFirst().get().getValue().forEach(clauseNumber -> {
//                List<String> list = slices.get(symbol);
//                if (list == null)
//                    list = new ArrayList<>();
//
//                if (clauseNumber <= mapping.getEDB().size()) {
//                    list.add(mapping.getEDB().)
//                }
//
//
//            });
//
//        });
//
//
//        return slices;
//    }

    public static String getEDBString(Relation edb) {
        StringBuilder toString = new StringBuilder(edb.getName() + "(");

        AtomicInteger counter = new AtomicInteger();
        counter.set(-1);

        Arrays.asList(edb.getAttributes()).forEach(v -> {
            if (counter.incrementAndGet() < edb.getAttributes().length)
                toString.append(v).append(", ");
        });
        toString.append(")");

        return toString.toString();
    }


    public static void evaluation(Mapping mapping) {


    }


}
