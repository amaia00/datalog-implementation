package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Literal;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 5/24/17.
 */
public class Fact {

    private Fact() {
        // On n'a pas besoin de constructeur, c'est une classe avec des méthodes statiques.
    }

    /**
     * @param factsByRule
     * @param literal
     * @param attributes
     * @param historical
     * @param repeat
     * @param position
     * @param tgd
     * @return
     */
    public static Optional<Relation> getNextFact(List<Relation> factsByRule, Literal literal, List<String> attributes,
                                                 List<Map.Entry<String, Relation>> historical, boolean repeat,
                                                 int position, Tgd tgd, Map<String, Integer> intents) {
        Optional<Relation> relationOptional = Optional.empty();

        if (repeat) {
            if (Util.sameOrderAttributes(historical.get(historical.size() - 1).getValue().getAttributes(), attributes)) {
                relationOptional = Optional.of(historical.get(historical.size() - 1).getValue());
            }
        } else {


            /**
             * Si on a déjà testé toutes les valeurs possibles on cherche un autre.
             */
            boolean assigned = false;
            if (!relationOptional.isPresent()) {
                relationOptional = factsByRule.stream()
                        .filter(edb -> literal.getAtom().getName()
                                .equals(edb.getName()) && Util.sameOrderAttributes(edb.getAttributes(),
                                attributes) && historical.stream().noneMatch(h -> h.getKey().equals(edb.getName()
                                .concat(String.valueOf(position))) && Util.sameOrderAttributes(edb.getAttributes(),
                                h.getValue().getAttributes()))).findFirst();

                assigned = relationOptional.isPresent();
            }

            /**
             *  Si n'est pas le dernière sous-règle on va vérifier d'abord qu'on a déjà essaie toutes les valeurs
             *  possibles dans les iterations anterieures  et sinon on va chercher le valeur dans l'historique pour
             *  ne pas modifier le valeur de ce sous règle.
             *
             *  Exemple:
             *  head(X,Y) :- regle(X), regle(Y)
             *
             *  Ainsi, on test:
             *  regle_{1}    regle_{2}
             *  -----------------------
             *  valeur1      valeur1
             *  valeur1      valeur2
             *  ...          ...
             */
            if (tgd.getLeftPositiveList().size() - 1 != position) {

                AtomicInteger positionCounter = new AtomicInteger();
                positionCounter.set(tgd.getLeftPositiveList().size() - 1);

                boolean founded = false;
                while (!founded) {

                    int qteIntents;
                    try {
                        qteIntents = intents.entrySet().stream().filter(i -> i.getKey().equals(literal.getAtom().getName()
                                .concat(positionCounter.toString()))).findFirst().get().getValue();
                    } catch (NoSuchElementException e) {
                        qteIntents = 0;
                    }

                    boolean  test = false;
                    if (qteIntents <= historical.stream().filter(h -> h.getKey().equals(literal.getAtom().getName()
                    .concat(positionCounter.toString())) && Util.sameOrderAttributes(h.getValue().getAttributes(),
                            attributes)).count()) {

                        if (!relationOptional.isPresent()) {
                            test = true;
                        }

                            try {
                                //historical.stream().sorted(Comparator.reverseOrder());
                                Collections.reverse(historical);

                                relationOptional = Optional.of(historical.stream().filter(h -> h.getKey()
                                        .equals(literal.getAtom().getName().concat(String.valueOf(position))))
                                        .findFirst().get().getValue());
                                founded = true;
                                assigned = false;
                                Collections.reverse(historical);
                            } catch (NoSuchElementException e) {
                                founded = true;
                                Collections.reverse(historical);
                            }


                    } else {
                        intents.put(literal.getAtom().getName().concat(positionCounter.toString()), -1);
                        historical.removeIf(h -> h.getKey().equals(literal.getAtom().getName()
                                .concat(positionCounter.toString())));
                    }

                    positionCounter.decrementAndGet();
                    founded = (founded || positionCounter.get() == position);
                }

            }

            if (assigned) {
                historical.add(historical.size(), new AbstractMap.SimpleEntry<>(literal.getAtom().getName()
                        .concat(String.valueOf(position)), relationOptional.get()));
            }

            intents.put(literal.getAtom().getName().concat(String.valueOf(position)),
                    intents.getOrDefault(literal.getAtom().getName().concat(String.valueOf(position)), 0) + 1);
        }

        return relationOptional;
    }
}
