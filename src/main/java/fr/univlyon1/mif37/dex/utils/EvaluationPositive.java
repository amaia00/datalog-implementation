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
public class EvaluationPositive {
    private static final boolean DEBUG = false;
    private static final List<Map.Entry<String, Relation>> historical = new ArrayList<>();
    private static final Map<String, Integer> intents = new HashMap<>();
    private static final List<Relation> factsByRule = new ArrayList<>();
    private static final NavigableMap<String, List<String>> mapConstants = new TreeMap<>();
    private static final Map<String, String> mapVariables = new HashMap<>();

    private EvaluationPositive() {
        /* On cache le constructeur */
    }

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
            newFacts.addAll(edbByOrderOfEvaluation.get(partition.get()));

            /*
             * On parcour chaque règle par l'ordre que la stratification a defini avant.
             */
            List<Relation> edbsFounded = new ArrayList<>();
            factCounterAfter.set(0);

            tgdByOrderOfEvaluation.get(partition.get()).forEach(tgd -> {

                if (DEBUG)
                    System.out.println("TGD Rigth: " + tgd.getRight().getName());
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


                mapConstants.clear();
                mapVariables.clear();

                factsByRule.clear();
                factsByRule.addAll(newFacts);

                historical.clear();
                intents.clear();

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
                    exhaustiveSearchByPredicat(tgd, relation);

                    verifierAndAddNewsFacts(mapping, newFacts, counterFacts, tgd,
                            relation.get());
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

        return Util.removeDuplicates(newFacts);
    }

    /**
     * @param mapping      mapping du programme
     * @param newFacts     les faits ajoutes dans l'evaluation
     * @param counterFacts le compteur de faits
     * @param tgd          tout le TGD du programme
     * @param relation     la relation qu'on va inferer
     */
    static void verifierAndAddNewsFacts(Mapping mapping, List<Relation> newFacts,
                                                AtomicInteger counterFacts, Tgd tgd, Relation relation) {
        if (relation != null) {
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
            addNewsFact(mapping, attributes, newFacts, counterFacts, tgd);

            /* On enlève le fait déjà utilisé pour ne pas avoir de doublants et pour finir le boucle.*/
            factsByRule.removeIf(relation1 -> Util.equalsRelation(relation1, relation));
        }
    }

    /**
     * @param tgd          la règle qu'on est en train d'évaluer
     * @param relation     un nouveau fait possible pour ce règle.
     */
    private static void exhaustiveSearchByPredicat(Tgd tgd, AtomicReference<Relation> relation) {
         /*
         * Pour chaque sous-règle dans le corps de la règle on cherche les constantes dans edbsFounded
         * On garde dans le map `mapConstants` les règles qui sont dans le corps avec les attributs
         * qui sont dans le fait
         */
        AtomicBoolean exhaustiveSearch = new AtomicBoolean();
        exhaustiveSearch.set(true);

        // TODO
        AtomicInteger maxIteration = new AtomicInteger();
        maxIteration.set(30);

        AtomicInteger iterationCounter = new AtomicInteger();
        iterationCounter.set(0);

        while (exhaustiveSearch.get()) {
            exhaustiveSearch.set(false);

            AtomicInteger positionLiteral = new AtomicInteger();
            positionLiteral.set(0);

            AtomicBoolean notRuleMatch = new AtomicBoolean();

            tgd.getLeft().forEach(literal -> {
                iterationCounter.incrementAndGet();

                if (!notRuleMatch.get()) {
                    List<String> attributes = new ArrayList<>();

                   /*
                    * Pour chaque variable on cherche s'il existe déjà une variable affecté dans la collection
                    * `mapVariables`, si c'est le cas on l'ajoute dans les attributs, sinon, on cherche une règle
                    * dans l'EDB qui a toutes les attributes déjà affectés.
                    *
                    */

                    loopOverRules(literal, attributes, exhaustiveSearch, tgd, relation, positionLiteral);
                    positionLiteral.incrementAndGet();

                    /*
                     * Si relation est ça veut dire qu'il n'y a plus des règles à appliquer
                     */
                    if (relation.get() != null) {
                        addConstantsAndVariables(literal, attributes);
                    } else {
                        notRuleMatch.set(true);

                        if (!exhaustiveSearch.get() && iterationCounter.get() < maxIteration.get())
                                exhaustiveSearch.set(true);
                    }
                }
            });
        }
    }

    /**
     *
     * @param literal
     * @param attributes
     * @param exhaustiveSearch
     * @param tgd
     * @param relation
     * @param positionLiteral
     */
    private static void loopOverRules(Literal literal, List<String> attributes, AtomicBoolean exhaustiveSearch,
                                      Tgd tgd, AtomicReference<Relation> relation,AtomicInteger positionLiteral) {
        AtomicInteger position = new AtomicInteger();
        position.set(0);

        AtomicBoolean notFounded = new AtomicBoolean();
        AtomicBoolean repeat = new AtomicBoolean();
        AtomicBoolean byMapVariables = new AtomicBoolean();

        literal.getAtom().getVars().forEach(variable -> {
            if (!notFounded.get()) {
                relation.set(getNextPossibleFact(variable, attributes, literal, exhaustiveSearch,
                        repeat.get(), positionLiteral.get(), tgd, byMapVariables));

                repeat.set(true);
                if (relation.get() == null) {
                    notFounded.set(true);
                }
            }

            position.incrementAndGet();
        });

    }

    /**
     *
     * @param literal le literal
     * @param attributes les attributes du nouevau fait qu'on va inferer.
     */
    private static void addConstantsAndVariables(Literal literal, List<String> attributes) {
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

    /**
     * Cette méthode ajoute un nouveau fait en vérifiant qu'il n'existe pas encore entre les faits qu'on a déjà
     *
     * @param mapping      du programme
     * @param attributes   les attributs du nouveau fait
     * @param newFacts     la liste de facts qui ont été déjà ajoutés
     * @param counterFacts le compteur des nouveaux faits
     * @param tgd          la règle qu'on est en train d'évaluer
     */
    private static void addNewsFact(Mapping mapping, List<String> attributes, List<Relation> newFacts,
                                    AtomicInteger counterFacts, Tgd tgd) {
        /*
         * On vérifie que le nouveau fait n'existe pas encore dans la BD de faits.
         */
        if (mapping.getEDB().stream().noneMatch(edb -> Util.equalsRelation(edb, new Relation(tgd
                .getRight().getName(), attributes)))) {

            if (attributes.stream().allMatch(Objects::nonNull)) {
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
     * @param variable         la variable qu'on est en train d'évaluer et chercher sa valeur
     * @param attributes       les attributs de la règle qu'on est en train d'évaluer
     * @param literal          le literal qu'on est en train de traiter
     * @param exhaustiveSearch un drapeau pour vérifier si on devrai continuer en essaiant des autres fait por ce règle.
     * @return un nouveau fait possible pour ce règle.
     */
    static Relation getNextPossibleFact(Variable variable, List<String> attributes,
                                        Literal literal, AtomicBoolean exhaustiveSearch, boolean repeat,
                                        int iteration, Tgd tgd, AtomicBoolean byMapVariables) {

        AtomicReference<Relation> relation = new AtomicReference<>();


        Optional<Relation> relationOptional = Fact.getNextFact(factsByRule, literal, historical, repeat,
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

