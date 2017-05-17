package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Evaluation {
    private Evaluation() {
        /* On cache le constructeur */
    }

    public static Set<Relation> evaluate(Mapping mapping, Map<Integer, List<Tgd>> tgdByOrderOfEvaluation,
                                         Map<Integer, List<Relation>> edbByOrderOfEvaluation) {
        AtomicInteger partition = new AtomicInteger();
        partition.set(1);
        Set<Relation> newFacts = new HashSet<>();

        AtomicInteger newFactsSizeBefore = new AtomicInteger();
        AtomicInteger factCounterAfter = new AtomicInteger();
        factCounterAfter.set(0);

        /*
         * On va parcourir tous les partitions et si on ne trouve pas des nouveaux faits on arrête le parcours.
         * */
        while (partition.get() <= tgdByOrderOfEvaluation.size()) {
            /* On ajoute les faits de cette partition dans les newFacts */
            newFacts.addAll(edbByOrderOfEvaluation.get(partition.get()));

            /*
             * On parcour chaque règle par l'ordre que la stratification a defini avant.
             */
            tgdByOrderOfEvaluation.get(partition.get()).forEach(tgd -> {
                List<Relation> edbsFounded = new ArrayList<>();
                factCounterAfter.set(0);

                /*
                 * On cherche pour chaque sous-regle dans le corps les noms de faits qui sont dans l'EDB.
                 * Example: r(x) :- t(x), p(x)
                 * Fait: t(A), p(B)
                 *
                 * On recupère dans edbsFounded = {t(A), p(b)}
                 */
                mapping.getEDB().forEach(fact ->
                        tgd.getLeft().forEach(rule -> {
                            if (fact.getName().equals(rule.getAtom().getName())) {
                                edbsFounded.add(factCounterAfter.get(), fact);
                                factCounterAfter.incrementAndGet();
                            }
                        })
                );


                Map<String, List<String>> mapConstants = new HashMap<>();
                Map<String, String> mapVariables = new HashMap<>();

                List<Relation> factsByRule = new ArrayList<>();
                factsByRule.addAll(edbsFounded);
                while (factsByRule.size() > tgd.getLeft().size()) {
                    /* Cette variable represente le fait utilisé pour deduire la règle */
                    AtomicReference<Relation> relation = new AtomicReference<>();
                    mapVariables.clear();

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
                         *
                         * TODO Verifier le cas d'une variable au millieu de la règle.
                         */

                        literal.getAtom().getVars().forEach(variable -> {
                            if (mapVariables.containsKey(variable.getName())) {
                                attributes.add(mapVariables.get(variable.getName()));
                            } else {
                                Optional<Relation> relationOptional = factsByRule.stream()
                                        .filter(edb -> literal.getAtom().getName()
                                        .equals(edb.getName()) && Arrays.asList(edb.getAttributes())
                                        .containsAll(attributes)).findFirst();

                                if (relationOptional.isPresent()) {
                                    relation.set(relationOptional.get());
                                    attributes.clear();
                                    attributes.addAll(Arrays.asList(relation.get().getAttributes()));
                                }
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
                    */
                    List<String> attributes = new ArrayList<>();
                    tgd.getRight().getVars().forEach(var -> attributes.add(mapVariables.get(var.getName())));

                    /*
                     * On vérifie que le nouveau fait n'existe pas encore dans la BD de faits.
                     */
                    if (mapping.getEDB().stream().noneMatch(edb -> Util.equalsRelation(edb, new Relation(tgd.getRight()
                            .getName(), attributes))))
                        newFacts.add(new Relation(tgd.getRight()
                                .getName(), attributes));

                    /*
                     * On vérifie que il y a des nouveaux faits.
                     */
                    if (newFactsSizeBefore.get() != newFacts.size()) {
                        newFactsSizeBefore.set(newFacts.size());
                    } else {
                        /* On ne trouve pas des nouveaux faits, donc on arrête le parcours en assignant la valeur de
                         * la taille du TGD à la variable particion.
                         */
                        partition.set(tgdByOrderOfEvaluation.size() + 1);

                    }

                    /* On enlève le fait déjà utilisé pour ne pas avoir de doublants et pour finir le boucle.*/
                    factsByRule.removeIf(relation1 -> Util.equalsRelation(relation1, relation.get()));
                }
            });

            /* Après d'avoir ajouter tous les faits possibles pour la partition d'avant on passe à la suivante*/
            partition.incrementAndGet();
        }

        return newFacts;
    }

}
