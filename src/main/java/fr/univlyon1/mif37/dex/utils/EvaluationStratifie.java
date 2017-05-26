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
    private static final boolean DEBUG = false;

    private EvaluationStratifie() {
        /* On cache le constructeur */
    }

    /**
     * @param mapping                le mappind gu programme
     * @param tgdByOrderOfEvaluation les TGD ordonnés par stratum
     * @param edbByOrderOfEvaluation les EDB ordonnées par stratum
     *
     * @return les nouveaux faits inferes.
     */
    public static List<Relation> evaluate(Mapping mapping, Map<Integer, List<Tgd>> tgdByOrderOfEvaluation,
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
            if (partition.get() <= edbByOrderOfEvaluation.size())
                newFacts.addAll(edbByOrderOfEvaluation.get(partition.get()));

            /*
             * On parcour chaque règle par l'ordre que la stratification a defini avant.
             */
            List<Relation> edbsFounded = new ArrayList<>();
            factCounterAfter.set(0);

            while (!tgdByOrderOfEvaluation.containsKey(partition.get()))
                partition.incrementAndGet();

            tgdByOrderOfEvaluation.get(partition.get()).forEach(tgd -> {

                if (DEBUG) {
                    System.out.println("tgd rigth " + tgd.getRight().getName());
                }
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

                int maxIterationsPossibles = factsByRule.size() * 3;
                mapVariables.clear();

                List<Map.Entry<String, Relation>> historical = new ArrayList<>();
                Map<String, Integer> intents = new HashMap<>();

                while ((firstTime || newFactsSizeBefore.get() != newFacts.size()) && !factsByRule.isEmpty()) {
                    firstTime = false;
                    newFactsSizeBefore.set(newFacts.size());

                    /* Cette variable represente le fait utilisé pour deduire la règle */
                    AtomicReference<Relation> relation = new AtomicReference<>();
                    //mapVariables.clear();

                    /*
                     * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
                     * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
                     * qui sont dans le fait
                     */
                    AtomicBoolean exhaustiveSearch = new AtomicBoolean();
                    exhaustiveSearch.set(true);

                    exhaustiveSearchByPredicatSemipositif(mapConstants, mapVariables, tgd, relation, factsByRule,
                            maxIterationsPossibles, historical, intents);

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
                        //factsByRule.removeIf(relation1 -> Util.equalsRelation(relation1, relation.get()));
                    }
                }

                /*
                 * On ne trouve pas des nouveaux faits, donc on arrête le parcours en assignant la valeur de
                 * la taille du TGD à la variable particion.
                 */
                //partition.set(tgdByOrderOfEvaluation.size() + 1);
            });

            /* Après d'avoir ajouter tous les faits possibles pour la partition d'avant on passe à la suivante*/
            partition.incrementAndGet();
        }

        return Util.removeDuplicates(newFacts);
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
                                                              List<Relation> factsByRule, int maxIterationsPossibles,
                                                              List<Map.Entry<String, Relation>> historical,
                                                              Map<String, Integer> intents) {
         /*
         * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
         * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
         * qui sont dans le fait
         */
        AtomicBoolean exhaustiveSearch = new AtomicBoolean();
        exhaustiveSearch.set(true);

        AtomicInteger countIterations = new AtomicInteger();
        countIterations.set(0);

        mapVariables.clear();

        while (exhaustiveSearch.get() && countIterations.get() <= maxIterationsPossibles) {
            exhaustiveSearch.set(false);
            AtomicInteger positionLiteral = new AtomicInteger();
            positionLiteral.set(0);
            tgd.getLeftList().forEach(literal -> {
                List<String> attributes = new ArrayList<>();

                countIterations.incrementAndGet();

                if (literal.getFlag()) {
                    /*
                    * Pour chaque variable on cherche s'il existe déjà une variable affecté dans la collection
                    * `mapVariables`, si c'est le cas on l'ajoute dans les attributs, sinon, on cherche une règle
                    * dans l'EDB qui a toutes les attributes déjà affectés.
                    */
                    AtomicInteger position = new AtomicInteger();
                    position.set(0);

                    AtomicBoolean repeat = new AtomicBoolean();
                    AtomicBoolean byMapVariables = new AtomicBoolean();

                    literal.getAtom().getVars().forEach(variable -> {
                        relation.set(getNextPositivePossibleFact(mapVariables, variable, attributes,
                                position, factsByRule, mapConstants, literal, exhaustiveSearch, historical, repeat.get(),
                                positionLiteral.get(), tgd, intents, byMapVariables));

                        // TODO verifier si c'est correct dans ce cas
                        if (!byMapVariables.get())
                            repeat.set(true);

                        position.incrementAndGet();
                    });

                } else {
                    AtomicInteger position = new AtomicInteger();
                    position.set(0);
//                    literal.getAtom().getVars().forEach(variable -> {
                    relation.set(getNextPossibleFactNegativeRule(mapVariables, literal.getAtom().getVars(), attributes,
                            position, factsByRule, literal, exhaustiveSearch));

                    position.incrementAndGet();
//                    });
                }

                positionLiteral.incrementAndGet();

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
                    if (DEBUG) {
                        System.out.println("Literal: " + literal.getAtom().getName());
                    }
                    literal.getAtom().getVars().forEach(var -> mapVariables.put(var.getName(), attributes
                            .get(index.incrementAndGet())));
                } else {
                    if (positionLiteral.get() >= tgd.getLeftList().size()) {
                        mapVariables.clear();
                        mapConstants.clear();
                    }
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

            if (newFacts.stream().noneMatch(fact -> fact.getName().equals(tgd.getRight().getName()) &&
                    Util.sameOrderAttributes(Arrays.asList(fact.getAttributes()), attributes))) {
                newFacts.add(counterFacts.get(), new Relation(tgd.getRight()
                        .getName(), attributes));
                factsByRule.add(new Relation(tgd.getRight()
                        .getName(), attributes));
                counterFacts.incrementAndGet();
            }
        }

    }

    /**
     * Cette méthode cherche les valeurs possibles pour chaque attribute de sous-règle en vérifiant les faits
     * définis et les nouveaux faits trouvés à partir de l'évaluation anterieur.
     *
     * @param mapVariables     une liste des variables avec les valeurs assignés
     * @param variables        les variables qu'on est en train d'évaluer et chercher sa valeur
     * @param attributes       les attributs de la règle qu'on est en train d'évaluer
     * @param position         la position de l'attribut
     * @param factsByRule      tous les faits (anciens et nouveaux) qui ont été pris en compte pour cette partition.
     * @param literal          le literal qu'on est en train de traiter
     * @param exhaustiveSearch un drapeau pour vérifier si on devrai continuer en essaiant des autres fait por ce règle.
     *
     * @return un nouveau fait possible pour ce règle.
     */
    private static Relation getNextPossibleFactNegativeRule(Map<String, String> mapVariables,
                                                            Collection<Variable> variables, List<String> attributes,
                                                            AtomicInteger position, List<Relation> factsByRule,
                                                            Literal literal, AtomicBoolean exhaustiveSearch) {

        AtomicReference<Relation> relation = new AtomicReference<>();

        if (variables.stream().allMatch(variable -> mapVariables.containsKey(variable.getName()))) {
            List<String> allValues = new ArrayList<>();
            mapVariables.forEach((name, value) -> {
                if (variables.stream().anyMatch(v -> v.getName().equals(name)))
                    allValues.add(value);
            });

            if (factsByRule.stream().noneMatch(fact -> fact.getName().equals(literal.getAtom().getName()) &&
                    Arrays.asList(fact.getAttributes()).containsAll(attributes) &&
                    Util.sameOrderAttributes(Arrays.asList(fact.getAttributes()), allValues))) {

//                allValues.forEach((name, value) -> attributes.add(position.getAndIncrement(), value));
                //attributes.add(position.get(), mapVariables.get(variable.getName()));
                attributes.addAll(allValues);
                relation.set(new Relation(literal.getAtom().getName(), attributes));
                //exhaustiveSearch.set(true);

            } else {
                relation.set(null);
                exhaustiveSearch.set(true);
            }
        } else {

            AtomicInteger numberVariables = new AtomicInteger();
            numberVariables.set(0);
            List<String> allConstants = Util.getAllConstants(factsByRule);

            variables.forEach(var -> {
                if (mapVariables.containsKey(var.getName())) {
                    attributes.add(position.get(), mapVariables.get(var.getName()));
                } else {
                    while (!allConstants.isEmpty()) {

                        /* On prend une constant */
                        String possibleConstant = allConstants.stream().findFirst().get();

                        /* S'il n'existe pas une règle avec ce nom et ce constant dans la liste de faits (inclus ça
                        qu'on déjà inferé) ça veut dire que la règle est vrai parce qu'elle est faux */
                        if (factsByRule.stream().noneMatch(fact -> fact.getName().equals(literal.getAtom().getName()) &&
                                Arrays.asList(fact.getAttributes()).contains(possibleConstant) &&
                                Arrays.asList(fact.getAttributes()).containsAll(attributes))) {

                            /* l'implementation de list qui retourne la fonction asList ne support pas l'operation
                             * add, donc, on fait une autre copy `allAttributes = new ArrayList<>(allAttributes)`
                             * pour pouvoir ajouter des elements.*/
                            List<String> allAttributes = new ArrayList<>(attributes);
                            allAttributes.add(possibleConstant);

                            attributes.clear();
                            attributes.addAll(allAttributes);

                            relation.set(new Relation(literal.getAtom().getName(), attributes));
                            mapVariables.put(var.getName(), possibleConstant);
                        }

                        allConstants.remove(possibleConstant);
                    }
                }

            });


        }

        return relation.get();
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
     * @param historical       les anciennes variables de l'iteration anterieur.
     *
     * @return un nouveau fait possible pour ce règle.
     */
    static Relation getNextPositivePossibleFact(Map<String, String> mapVariables, Variable variable,
                                                List<String> attributes,
                                                AtomicInteger position, List<Relation> factsByRule,
                                                NavigableMap<String, List<String>> mapConstants,
                                                Literal literal, AtomicBoolean exhaustiveSearch,
                                                List<Map.Entry<String, Relation>> historical,
                                                boolean repeat, int iteration, Tgd tgd, Map<String, Integer> intents,
                                                AtomicBoolean byMapVariables) {

        AtomicReference<Relation> relation = new AtomicReference<>();


        Optional<Relation> relationOptional = Fact.getNextFact(factsByRule, literal, attributes, historical, repeat,
                iteration, tgd, intents, mapVariables, variable, byMapVariables);

        if (relationOptional.isPresent()) {
            relation.set(relationOptional.get());
            attributes.clear();
            attributes.addAll(Arrays.asList(relation.get().getAttributes()));
        } else {
            relation.set(null);

            Map.Entry<String, List<String>> lastRule = mapConstants.lastEntry();

            if (lastRule != null) {

                if (factsByRule.stream().anyMatch(f -> f.getName().equals(lastRule.getKey()))) {
                    exhaustiveSearch.set(true);
                }

                mapVariables.clear();
                mapConstants.clear();
                attributes.clear();
            }
        }


        return relation.get();
    }
}

