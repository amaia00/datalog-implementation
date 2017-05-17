package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.AbstractRelation;
import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
     * @param mapping le program qu'on est en train d'evaluer
     * @return la liste de constants
     */
    static Set<Relation> getAllConstants(Mapping mapping) {
        return new HashSet<>(mapping.getEDB());
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
        return edb.getAttributes().equals(possibleFact.getAttributes()) & edb.getName().equals(possibleFact.getName());
    }
}
