package fr.univlyon1.mif37.dex.utils;

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
public class Evaluation {
    private Evaluation() {
        /* On cache le constructeur */
    }

    public static Set<Relation> evaluate (Mapping mapping, List<Tgd> tgdByOrderOfEvaluation) {
        Set<Relation> newFacts = new HashSet<>();

        AtomicInteger factCounterBefore = new AtomicInteger();
        AtomicInteger factCounterAfter = new AtomicInteger();
        factCounterAfter.set(0);

        /*
         * On parcour chaque règle par l'ordre que la stratification a defini avant.
         */
        tgdByOrderOfEvaluation.forEach(tgd -> {
            List<Relation> edbsFounded = new ArrayList<>();
            factCounterBefore.set(0);
            factCounterAfter.set(0);

            /*
             * On cherche pour chaque sous-regle dans le corps les noms de faits qui sont dans l'EDB.
             * Example: r(x) :- t(x), p(x)
             * Fait: t(A), p(B)
             *
             * On recupère dans edbsFounded = {t(A), p(b)}
             */
            mapping.getEDB().forEach(fact -> {
                factCounterBefore.incrementAndGet();
                tgd.getLeft().forEach(rule -> {
                    if (fact.getName().equals(rule.getAtom().getName())) {
                        edbsFounded.add(factCounterAfter.get(), fact);
                        factCounterAfter.incrementAndGet();
                    }
                });
            });


            Map<String, List<String>> mapConstants=  new HashMap<>();
            Map<String, String> mapVariables=  new HashMap<>();

            if (edbsFounded.size() == tgd.getLeft().size()) {
                /*
                 * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
                 * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
                 * qui sont dans le fait
                 */
                tgd.getLeft().forEach(literal -> {
                    List<String> attributes = new ArrayList<>();

                    /*
                     * Pour chaque variable on cherche s'il existe déjà une variable affecté dans la collection
                     * `mapVariables`, si c'est le cas on l'ajoute dans les attributs, sinon, on cherche une règle
                     * dans l'EDB qui a toutes les attributes déjà affectés.
                     */
                    literal.getAtom().getVars().forEach(variable -> {
                        if (mapVariables.containsKey(variable.getName())) {
                            attributes.add(mapVariables.get(variable.getName()));
                        } else {
                            mapping.getEDB().forEach(edb -> {
                                if (literal.getAtom().getName().equals(edb.getName()) && Arrays.asList(edb.getAttributes())
                                        .containsAll(attributes)) {
                                    attributes.clear();
                                    attributes.addAll(Arrays.asList(edb.getAttributes()));
                                }
                            });
                        }
                    });

                    /*
                     * On ajoute dans la collection de mapConstants la règle avec les constantes qui ont été affectés
                     * lors de l'inference
                     * c-a-d au lieu de p(x), on aura p(A)
                     */
                    mapConstants.put(literal.getAtom().getName(), attributes);

                    /*
                     * On ajoute dans la collection de mapVariables quelle variables ont été affectés avec quelle valeur
                     * e.g. $x -> A, $y -> B
                     */
                    AtomicInteger index = new AtomicInteger();
                    index.set(-1);
                    literal.getAtom().getVars().forEach(var -> mapVariables.put(var.getName(), attributes
                            .get(index.incrementAndGet())));

                });

                /*
                * On affecte les constantes aux attributs qui sont dans la tête de la règle.
                * Example: r(x) :- t(x), p(x)
                * Fait: t(A), p(A)
                * Donc, on garde A comme l'attribut de r pour en déduire r(A).
                *
                */
                List<String> attributes = new ArrayList<>();
                tgd.getRight().getVars().forEach(var -> attributes.add(mapVariables.get(var.getName())));

                try {
                    /*
                     * On vérifie que le nouveau fait n'existe pas encore dans la BD de faits.
                     */
                    if (!mapping.getEDB().stream().anyMatch(edb -> Util.equalsRelation(edb, new Relation(tgd.getRight()
                            .getName(), attributes))))
                        newFacts.add(new Relation(tgd.getRight()
                                .getName(), attributes));
                }catch (NullPointerException e) {
                    System.out.println("ERROR");
                }
            }
        });

        return newFacts;
    }

}
