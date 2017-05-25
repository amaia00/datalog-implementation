package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Util {
    private static final int ONE = 1;

    private Util() {
        /* On ajoute un constructeur privé étant donnée qu'il s'agit d'une classe util */
    }

    /**
     * La méthode retourne la liste de constants dans le EDB
     *
     * @param factsByRule tous les faits du programme + ça qu'on a inferer
     * @return la liste de constants
     */
    static List<String> getAllConstants(List<Relation> factsByRule) {
        Set<String> constants = new HashSet<>();
        factsByRule.forEach(edb ->
                constants.addAll(Arrays.asList(edb.getAttributes()))
        );

        return constants.stream().collect(Collectors.toList());
    }

    /**
     * Cette méthode crée un HashMap avec la valeur '1' pour indiquer l'utilisation du règle au moins une fois.
     *
     * @param edbs les faits dans le programme
     * @param idbs le nom des regles
     * @return le hashMap avec la valeur 1
     */
    static Map<String, Integer> createStratum(Collection<Relation> edbs, Collection<AbstractRelation> idbs) {
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
     * Symbol Definitions. The definition def(s) of a program symbol s is the set of all program clauses that
     * have the symbol p in their head atom.
     *
     * @param edbs tous les faits du programme
     * @param tgds  toutes les règles du programme
     * @param ruleHead le nom de la règle
     * @return la liste de clauses dans laquelle le symbol est présent
     */
    static List<Object> getSymbols(Collection<Relation> edbs, Collection<Tgd> tgds, String ruleHead) {
        List<Object> symbols = new ArrayList<>();

        tgds.forEach(tgd -> {
            if (tgd.getRight().getName().equals(ruleHead))
                symbols.add(tgd);
        });

        edbs.forEach(edb -> {
            if (edb.getName().equals(ruleHead))
                symbols.add(edb);
        });

        return symbols;
    }

    /**
     * Cette méthode retourne une chaîne de caracteres pour imprimer un fait dans le format datalog
     *
     * @param edb un fait du programme
     * @return la chaîne de caractères
     */
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

    /**
     * Cette méthode retourne une chaîne de caracteres pour imprimer une règle dans le format datalog
     *
     * @param tgd le règle
     * @return la chaîne de caractères formatee
     */
    public static String getTgdString(Tgd tgd) {
        StringBuilder toString = new StringBuilder(tgd.getRight().getName() + "(");

        tgd.getRight().getVars().forEach(v -> toString.append(v.getName() + ", "));
        toString.append(") :- ");

        tgd.getLeft().forEach(l -> {
            toString.append(l.getFlag() + " " + l.getAtom().getName() + " (");
            l.getAtom().getVars().forEach(v -> toString.append(v.getName() + ", "));
            toString.append("), ");
        });

        return toString.toString();
    }


    /**
     *
     * @param edb
     * @param possibleFact
     * @return
     */
    public static boolean equalsRelation(Relation edb, Relation possibleFact) {
        return Arrays.equals(new List[]{Arrays.asList(edb.getAttributes())},
                new List[]{Arrays.asList(possibleFact.getAttributes())})
                && edb.getName().equals(possibleFact.getName());
    }

    public static boolean sameOrderAttributes(String[] attributes1, String[] attributes2) {
        return sameOrderAttributes(Arrays.asList(attributes1), Arrays.asList(attributes2));
    }

    public static boolean sameOrderAttributes(String[] attributes1, List<String> attributes2) {
        return sameOrderAttributes(Arrays.asList(attributes1), attributes2);
    }

    public static boolean sameOrderAttributes(List<Object> attributes1, List<String> attributes2) {
        Iterator<Object> iteratorRelation1 = attributes1.iterator();
        Iterator<String> iteratorRelation2 = attributes2.iterator();

        while (iteratorRelation1.hasNext()) {
            String attribute1 = (String) iteratorRelation1.next();
            String attribute2;

            if (iteratorRelation2.hasNext())
                attribute2 = iteratorRelation2.next();
            else
                return true;

            if (!attribute1.equals(attribute2)) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param attributesOfRule
     * @param variableValue
     * @param position
     * @return
     */
    public static boolean sameAttributeAtThePosition(String[] attributesOfRule, String variableValue, int position) {
        List<String> attributes = Arrays.asList(attributesOfRule);
        if (position <= attributes.size()) {
            if (attributes.get(position).equals(variableValue))
                return true;
        }

        return false;
    }


    static List<Relation> removeDuplicates(List<Relation> facts) {
        List<Relation> uniqueList = new ArrayList<>();
        for (Relation edb: facts) {
            if (uniqueList.stream().noneMatch(f -> equalsRelation(edb, f)))
                uniqueList.add(edb);
        }

        return uniqueList;
    }
}
