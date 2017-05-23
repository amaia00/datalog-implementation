package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class EvaluationStratifie {
    private EvaluationStratifie() {
        /* On cache le constructeur */
    }

    /**
     *
     * @param mapping
     * @param tgdByOrderOfEvaluation
     * @param edbByOrderOfEvaluation
     * @return
     */
    public static Set<Relation> evaluateSemiposifProgram(Mapping mapping, Map<Integer, List<Tgd>> tgdByOrderOfEvaluation,
                                                         Map<Integer, List<Relation>> edbByOrderOfEvaluation) {
        AtomicInteger partition = new AtomicInteger();
        partition.set(1);
        List<Relation> newFacts = new ArrayList<>();

        AtomicInteger newFactsSizeBefore = new AtomicInteger();
        AtomicInteger factCounterAfter = new AtomicInteger();
        factCounterAfter.set(0);

        AtomicInteger counterFacts = new AtomicInteger();
        counterFacts.set(0);

        /*
         * On va parcourir tous les partitions et si on ne trouve pas des nouveaux faits on arrête le parcours.
         * */
        while (partition.get() <= tgdByOrderOfEvaluation.size()) {
            /* On ajoute les faits de cette partition dans les newFacts */
            newFacts.addAll(edbByOrderOfEvaluation.get(partition.get()));

            /*
             * On parcour chaque règle par l'ordre que la stratification a defini avant.
             */
            List<Relation> edbsFounded = new ArrayList<>();
            factCounterAfter.set(0);

            tgdByOrderOfEvaluation.get(partition.get()).forEach(tgd -> {
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


                NavigableMap<String, List<String>> mapConstants = new TreeMap<>();
                Map<String, String> mapVariables = new HashMap<>();

                List<Relation> factsByRule = new ArrayList<>();
                factsByRule.addAll(newFacts);

                boolean firstTime = true;

                while ((firstTime || newFactsSizeBefore.get() != newFacts.size()) && !factsByRule.isEmpty()) {
                    firstTime = false;
                    newFactsSizeBefore.set(newFacts.size());

                    /* Cette variable represente le fait utilisé pour deduire la règle */
                    AtomicReference<Relation> relation = new AtomicReference<>();
                    mapVariables.clear();

                    /*
                     * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
                     * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
                     * qui sont dans le fait
                     */
                    AtomicBoolean exhaustiveSearch = new AtomicBoolean();
                    exhaustiveSearch.set(true);
                    exhaustiveSearchByPredicatSemipositif(mapConstants, mapVariables, tgd, relation, factsByRule);

                    if (relation.get() != null) {
                        /*
                        * On affecte les constantes aux attributs qui sont dans la tête de la règle.
                        * Example: r(x) :- t(x), p(x)
                        * Fait: t(A), p(A)
                        * Donc, on garde A comme l'attribut de r pour en déduire r(A).
                        */
                        List<String> attributes = new ArrayList<>();
                        tgd.getRight().getVars().forEach(var -> attributes.add(mapVariables.get(var.getName())));


                        /*
                         * On ajoute le nouevau fact
                         */
                        addNewsFact(mapping, attributes, newFacts, counterFacts, factsByRule, tgd);
                        /* On enlève le fait déjà utilisé pour ne pas avoir de doublants et pour finir le boucle.*/
                        factsByRule.removeIf(relation1 -> Util.equalsRelation(relation1, relation.get()));
                    }
                }

                /*
                 * On ne trouve pas des nouveaux faits, donc on arrête le parcours en assignant la valeur de
                 * la taille du TGD à la variable particion.
                 */
                partition.set(tgdByOrderOfEvaluation.size() + 1);
            });

            /* Après d'avoir ajouter tous les faits possibles pour la partition d'avant on passe à la suivante*/
            partition.incrementAndGet();
        }

        newFacts.forEach(fact -> System.out.println(Util.getEDBString(fact)));
        return new HashSet<>(newFacts);
    }

    /**
     * @param mapConstants une liste des faits avec ses attributs qui ont été pris en compte pour l'évalutaion de
     *                     cette règle.
     * @param mapVariables une liste des variables avec les valeurs assignés
     * @param tgd          la règle qu'on est en train d'évaluer
     * @param relation     un nouveau fait possible pour ce règle.
     * @param factsByRule  tous les faits (anciens et nouveaux) qui ont été pris en compte pour cette partition.
     */
    private static void exhaustiveSearchByPredicat(NavigableMap<String, List<String>> mapConstants,
                                                   Map<String, String> mapVariables,
                                                   Tgd tgd, AtomicReference<Relation> relation,
                                                   List<Relation> factsByRule) {
         /*
         * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
         * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
         * qui sont dans le fait
         */
        AtomicBoolean exhaustiveSearch = new AtomicBoolean();
        exhaustiveSearch.set(true);

        while (exhaustiveSearch.get()) {
            exhaustiveSearch.set(false);
            tgd.getLeft().forEach(literal -> {
                List<String> attributes = new ArrayList<>();

               /*
                * Pour chaque variable on cherche s'il existe déjà une variable affecté dans la collection
                * `mapVariables`, si c'est le cas on l'ajoute dans les attributs, sinon, on cherche une règle
                * dans l'EDB qui a toutes les attributes déjà affectés.
                *
                * TODO Verifier le cas d'une variable au millieu de la règle.
                */
                AtomicInteger position = new AtomicInteger();
                position.set(0);
                literal.getAtom().getVars().forEach(variable -> {
                    relation.set(getNextPossibleFact(mapVariables, variable, attributes,
                            position, factsByRule, mapConstants, literal, exhaustiveSearch));

                    position.incrementAndGet();
                });

                /*
                * Si relation est ça veut dire qu'il n'y a plus des règles à appliquer
                */
                if (relation.get() != null) {
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
                }

            });
        }
    }


    /**
     * @param mapConstants une liste des faits avec ses attributs qui ont été pris en compte pour l'évalutaion de
     *                     cette règle.
     * @param mapVariables une liste des variables avec les valeurs assignés
     * @param tgd          la règle qu'on est en train d'évaluer
     * @param relation     un nouveau fait possible pour ce règle.
     * @param factsByRule  tous les faits (anciens et nouveaux) qui ont été pris en compte pour cette partition.
     */
    private static void exhaustiveSearchByPredicatSemipositif(NavigableMap<String, List<String>> mapConstants,
                                                              Map<String, String> mapVariables,
                                                              Tgd tgd, AtomicReference<Relation> relation,
                                                              List<Relation> factsByRule) {
         /*
         * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
         * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
         * qui sont dans le fait
         */
        AtomicBoolean exhaustiveSearch = new AtomicBoolean();
        exhaustiveSearch.set(true);

        while (exhaustiveSearch.get()) {
            exhaustiveSearch.set(false);
            tgd.getLeft().forEach(literal -> {
                List<String> attributes = new ArrayList<>();

                if (literal.getFlag()) {
                    /*
                    * Pour chaque variable on cherche s'il existe déjà une variable affecté dans la collection
                    * `mapVariables`, si c'est le cas on l'ajoute dans les attributs, sinon, on cherche une règle
                    * dans l'EDB qui a toutes les attributes déjà affectés.
                    *
                    * TODO Verifier le cas d'une variable au millieu de la règle.
                    */
                    AtomicInteger position = new AtomicInteger();
                    position.set(0);
                    literal.getAtom().getVars().forEach(variable -> {
                        relation.set(getNextPossibleFactSemipositive(mapVariables, variable, attributes,
                                position, factsByRule, mapConstants, literal, exhaustiveSearch, Util.getAllConstants(factsByRule)));

                        position.incrementAndGet();
                    });

                    /*
                    * Si relation est ça veut dire qu'il n'y a plus des règles à appliquer
                    */
                    if (relation.get() != null) {
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
                    }

                } else {
                    AtomicInteger position = new AtomicInteger();
                    position.set(0);
                    literal.getAtom().getVars().forEach(variable -> {
                        relation.set(getNextPossibleFact(mapVariables, variable, attributes,
                                position, factsByRule, mapConstants, literal, exhaustiveSearch));

                        position.incrementAndGet();
                    });

                }


            });
        }
    }

    /**
     * Cette méthode ajoute un nouveau fait en vérifiant qu'il n'existe pas encore entre les faits qu'on a déjà
     *
     * @param mapping      du programme
     * @param attributes   les attributs du nouveau fait
     * @param newFacts     la liste de facts qui ont été déjà ajoutés
     * @param counterFacts le compteur des nouveaux faits
     * @param factsByRule  tous les faits (anciens et nouveaux) qui ont été pris en compte pour cette partition.
     * @param tgd          la règle qu'on est en train d'évaluer
     */
    private static void addNewsFact(Mapping mapping, List<String> attributes, List<Relation> newFacts,
                                    AtomicInteger counterFacts, List<Relation> factsByRule, Tgd tgd) {
        /*
         * On vérifie que le nouveau fait n'existe pas encore dans la BD de faits.
         */
        if (mapping.getEDB().stream().noneMatch(edb -> Util.equalsRelation(edb, new Relation(tgd
                .getRight().getName(), attributes)))) {
            newFacts.add(counterFacts.get(), new Relation(tgd.getRight()
                    .getName(), attributes));
            factsByRule.add(new Relation(tgd.getRight()
                    .getName(), attributes));
            counterFacts.incrementAndGet();
        }

    }

    /**
     * Cette méthode cherche les valeurs possibles pour chaque attribute de sous-règle en vérifiant les faits
     * définis et les nouveaux faits trouvés à partir de l'évaluation anterieur.
     *
     * @param mapVariables     une liste des variables avec les valeurs assignés
     * @param variable         la variable qu'on est en train d'évaluer et chercher sa valeur
     * @param attributes       les attributs de la règle qu'on est en train d'évaluer
     * @param position         la position de l'attribut
     * @param factsByRule      tous les faits (anciens et nouveaux) qui ont été pris en compte pour cette partition.
     * @param mapConstants     une liste des faits avec ses attributs qui ont été pris en compte pour l'évalutaion de
     *                         cette règle.
     * @param literal          le literal qu'on est en train de traiter
     * @param exhaustiveSearch un drapeau pour vérifier si on devrai continuer en essaiant des autres fait por ce règle.
     * @return un nouveau fait possible pour ce règle.
     */
    private static Relation getNextPossibleFact(Map<String, String> mapVariables, Variable variable, List<String> attributes,
                                                AtomicInteger position, List<Relation> factsByRule,
                                                NavigableMap<String, List<String>> mapConstants, Literal literal,
                                                AtomicBoolean exhaustiveSearch) {
        AtomicReference<Relation> relation = new AtomicReference<>();
        if (mapVariables.containsKey(variable.getName())) {
            attributes.add(position.get(), mapVariables.get(variable.getName()));
        } else {
            Optional<Relation> relationOptional = factsByRule.stream()
                    .filter(edb -> literal.getAtom().getName()
                            .equals(edb.getName()) && Util.sameOrderAttributes(Arrays.asList(edb.getAttributes()),
                            attributes)).findFirst();

            if (relationOptional.isPresent()) {
                relation.set(relationOptional.get());
                attributes.clear();
                attributes.addAll(Arrays.asList(relation.get().getAttributes()));
            } else {
                relation.set(null);

                Map.Entry<String, List<String>> lastRule = mapConstants.lastEntry();
                if (lastRule != null) {
                    factsByRule.removeIf(f -> f.getName().equals(lastRule.getKey())
                            && Util.sameOrderAttributes(Arrays.asList(f.getAttributes()), lastRule.getValue()));

                    if (factsByRule.stream().anyMatch(f -> f.getName().equals(lastRule.getKey()))) {
                        exhaustiveSearch.set(true);
                    }
                    // TODO: A revoir si on ne peut pas uniquement effacer les derniers entrees
                    mapVariables.clear();
                    mapConstants.clear();
                    attributes.clear();
                }
            }
        }
        return relation.get();
    }


    private static Relation getNextPossibleFactSemipositive(Map<String, String> mapVariables, Variable variable, List<String> attributes,
                                                            AtomicInteger position, List<Relation> factsByRule,
                                                            NavigableMap<String, List<String>> mapConstants, Literal literal,
                                                            AtomicBoolean exhaustiveSearch, List<String> constants) {
        AtomicReference<Relation> relation = new AtomicReference<>();
        if (mapVariables.containsKey(variable.getName())) {
            attributes.add(position.get(), mapVariables.get(variable.getName()));
        } else {
            /* On prend une constant */
            boolean founded = false;
            while (!constants.isEmpty() || founded) {
                String possibleConstant = constants.stream().findFirst().get();

                if (factsByRule.stream().anyMatch(fact -> fact.getName().equals(literal.getAtom().getName()) &&
                        Arrays.asList(fact.getAttributes()).containsAll(attributes))) {
                    relation.set(null);
                    Map.Entry<String, List<String>> lastRule = mapConstants.lastEntry();
                    if (lastRule != null) {
                        factsByRule.removeIf(f -> f.getName().equals(lastRule.getKey())
                                && Util.sameOrderAttributes(Arrays.asList(f.getAttributes()), lastRule.getValue()));

                        if (factsByRule.stream().anyMatch(f -> f.getName().equals(lastRule.getKey()))) {
                            exhaustiveSearch.set(true);
                        }
                        // TODO: A revoir si on ne peut pas uniquement effacer les derniers entrees
                        mapVariables.clear();
                        mapConstants.clear();
                        attributes.clear();
                    }
                } else {
                    List<String> allAttributs = Arrays.asList(relation.get().getAttributes());

                    allAttributs.add(possibleConstant);
                    attributes.clear();
                    attributes.addAll(allAttributs);
                    founded = true;
                }

                constants.remove(possibleConstant);
            }


        }
        return relation.get();
    }
}

