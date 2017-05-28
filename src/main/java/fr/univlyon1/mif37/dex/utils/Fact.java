package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Literal;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;
import fr.univlyon1.mif37.dex.mapping.Variable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * @param historical
     * @param repeat
     * @param position
     * @param tgd
     *
     * @return
     */
    public static Optional<Relation> getNextFact(List<Relation> factsByRule, Literal literal,
                                                 List<Map.Entry<String, Relation>> historical, boolean repeat,
                                                 int position, Tgd tgd, Map<String, Integer> intents,
                                                 Map<String, String> mapVariables, Variable variable,
                                                 AtomicBoolean byMapVariables) {
        Optional<Relation> relationOptional = Optional.empty();

        List<String> attributesForThisRule = getAttributesForThisRule(literal, mapVariables);
        boolean assigned;

        if (repeat) {
            if (Util.sameOrderAttributes(historical.get(historical.size() - 1).getValue().getAttributes(),
                    attributesForThisRule)) {
                relationOptional = getLastValue(position, literal, historical);
            }
        } else {

            /* Si les variables sont déjà assignés avec valeurs */
            if (mapVariables.containsKey(variable.getName())) {

                relationOptional = factsByRule.stream()
                        .filter(edb -> literal.getAtom().getName()
                                .equals(edb.getName()) && Util.sameOrderAttributes(edb.getAttributes(),
                                attributesForThisRule) && historical.stream().noneMatch(h -> h.getKey().equals(edb.getName()
                                .concat(String.valueOf(position))) && Util.sameOrderAttributes(edb.getAttributes(),
                                h.getValue()))).findFirst();

                assigned = relationOptional.isPresent();
                if (assigned)
                    byMapVariables.set(true);

            } else {

                /*
                  Si on a déjà testé toutes les valeurs possibles on cherche un autre.
                 */

                relationOptional = factsByRule.stream()
                        .filter(edb -> literal.getAtom().getName()
                                .equals(edb.getName()) && Util.sameOrderAttributes(edb.getAttributes(),
                                attributesForThisRule) && historical.stream().noneMatch(h -> h.getKey().equals(edb.getName()
                                .concat(String.valueOf(position))) && Util.sameOrderAttributes(edb.getAttributes(),
                                h.getValue().getAttributes()))).findFirst();

                //assigned = relationOptional.isPresent();
                assigned = true;

                /*
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
                    positionCounter.set(position + 1);

                    boolean founded = false;
                    while (!founded || position != positionCounter.get()) {

                        /*
                         * On vérifie si on peut en continuer en utilisant le même fait pour les iterationes
                         * suivantes
                         */
                        String literalName = tgd.getLeftList().get(positionCounter.get()).getAtom().getName();
                        if (checkIntentsWithHistorical(literalName, positionCounter.get(), intents, historical,
                                attributesForThisRule) || stillRules(positionCounter.get(), literalName,
                                historical)) {

                            /*
                             * On recupère le dernière valeur du literal, parce qu'on peut continuer en essaiant si
                             * c'est possible inferer un nouveau fait à partir de cette valeur.
                             */
                            Optional<Relation> tmp = getLastValue(position, literal, historical);
                            if (tmp.isPresent()) {
                                relationOptional = tmp;
                                assigned = false;
                            }

                            founded = true;

                        } else {

                            /*
                             * On verifie si c'est ce literal qui doit changer ça valeur ou est bien le
                             * suivant. (Même principe qu'avant)
                             */
                            if (positionCounter.get() + 1 <= tgd.getLeftPositiveList().size() - 1) {
                                int newPosition = positionCounter.get() + 1;
                                String newName = tgd.getLeftList().get(newPosition).getAtom().getName();

                                if (checkIntentsWithHistorical(newName, newPosition, intents, historical,
                                        attributesForThisRule)|| stillRules(newPosition, newName,
                                        historical)) {


                                    Optional<Relation> tmp = getLastValue(position, literal, historical);
                                    if (tmp.isPresent()) {
                                        relationOptional = tmp;
                                        assigned = false;
                                    }

                                    founded = true;

                                } else {
                                    /*
                                     * On enlève tout l'historique de l'autre literal et on change notre valeur
                                     * par un nouveau fait.
                                     */
                                    removeFromHistorical(intents, literal, positionCounter.get(), historical,
                                            literalName);
                                }
                            } else {
                                // idem
                                removeFromHistorical(intents, literal, positionCounter.get(), historical, literalName);
                            }
                        }

                        positionCounter.decrementAndGet();
                        founded = (founded || positionCounter.get() == position);
                    }

                }
            }

            insertRegistreInHistorical(assigned, relationOptional, literal, position, historical);
        }

        insertIntent(position, intents, literal);

        return relationOptional;
    }

    /**
     * Cette méthode insere un nouveau registre dans l'historique
     *
     * @param assigned si un nouveau valeur a été choisi
     * @param relationOptional la relation choisi
     * @param literal le literal de la règle
     * @param position la position du literal
     * @param historical l'historique
     */
    private static void insertRegistreInHistorical(boolean assigned, Optional<Relation> relationOptional,
                                                   Literal literal, int position,
                                                   List<Map.Entry<String, Relation>> historical) {
        if (assigned && relationOptional.isPresent()) {
            historical.add(historical.size(), new AbstractMap.SimpleEntry<>(literal.getAtom().getName()
                    .concat(String.valueOf(position)), relationOptional.get()));
        } else if (!relationOptional.isPresent())
            historical.add(historical.size(), new AbstractMap.SimpleEntry<>(literal.getAtom().getName()
                    .concat(String.valueOf(position)), null));
    }

    /**
     *
     * Cette méthode insere une nouvelle tentative de fait sur un literal donné
     *
     * @param position la position du literal
     * @param intents les tentatives des faits
     * @param literal le literal de la règle
     */
    private static void insertIntent(int position, Map<String, Integer> intents,
                                     Literal literal) {
            intents.put(literal.getAtom().getName().concat(String.valueOf(position)),
                    intents.getOrDefault(literal.getAtom().getName().concat(String.valueOf(position)), 0) + 1);
    }

    /**
     * Cette méthode calcule la quantité des tentatives des faits sur un literal donné
     *
     * @param literalName le nom du literal
     * @param position la position du literal
     * @param intents les tentatives de faits pour un literal donné
     *
     * @return la quantité des tentatives
     */
    private static int getIntentsByPosition(String literalName, int position,
                                            Map<String, Integer> intents) {
        int qteIntents;
        try {
            qteIntents = intents.entrySet().stream().filter(i -> i.getKey().equals(literalName
                    .concat(String.valueOf(position)))).findFirst().get().getValue();
        } catch (NoSuchElementException e) {
            qteIntents = 0;
        }


        return qteIntents;
    }

    /**
     * Enlève toutes les tentatives d'un fait pour une clause donnée.
     *
     * @param intents les tentatives des faits
     * @param literal le literal
     * @param position la position du literal
     * @param historical l'historique
     * @param literalName le nom du literal
     */
    private static void removeFromHistorical(Map<String, Integer> intents, Literal literal, int position,
                                             List<Map.Entry<String, Relation>> historical, String literalName) {

        intents.put(literal.getAtom().getName().concat(String.valueOf(position)), -1);
        historical.removeIf(h -> h.getKey().equals(literalName.concat(String.valueOf(position))));
    }

    /**
     *
     * Cette méthode vérifie si la quantité des tentatives sur un fait n'est pas supérieur à la
     * quantité des nouvelles entrées dans l'historique
     *
     * @param literalName le nom du literal
     * @param position la position du literal dans la règle.
     * @param intents les tentatives des faits
     * @param historical l'historique
     * @param attributes les attributes du literal
     * @return
     */
    private static boolean checkIntentsWithHistorical(String literalName, int position,
                                                      Map<String, Integer> intents,
                                                      List<Map.Entry<String, Relation>> historical,
                                                      List<String> attributes) {
        int qteIntents = getIntentsByPosition(literalName, position, intents);

        return (qteIntents <= historical.stream().filter(h -> h.getKey().equals(literalName
                .concat(String.valueOf(position))) && Util.sameOrderAttributes(h.getValue(),
                attributes)).count());

    }

    /**
     *
     * Cette méthode vérifie si la dernière entrée n'est pas vide
     *
     * @param position la position du literal dans la règle.
     * @param literalName le nom du literal
     * @param historical l'historique
     * @return
     */
    private static boolean stillRules(int position, String literalName,
                                      List<Map.Entry<String, Relation>> historical){

        Relation relation = null;
        try {
            Collections.reverse(historical);

            relation = historical.stream().filter(h -> h.getKey()
                    .equals(literalName.concat(String.valueOf(position))))
                    .findFirst().get().getValue();
            Collections.reverse(historical);
        } catch (NoSuchElementException e) {
            Collections.reverse(historical);
        }


        return historical.isEmpty() || relation != null;
    }


    /**
     *
     * Cette méthode retourne la dernière entrée dans l'historique avec le literal envoyé.
     *
     * @param position la position du literal dans la règle.
     * @param literal le literal
     * @param historical l'historique
     *
     * @return la dernière entrée
     */
    private static Optional<Relation> getLastValue(int position, Literal literal,
                                                   List<Map.Entry<String, Relation>> historical) {
        Optional<Relation> relationOptional;
        try {
            Collections.reverse(historical);

            relationOptional = Optional.of(historical.stream().filter(h -> h.getKey()
                    .equals(literal.getAtom().getName().concat(String.valueOf(position))))
                    .findFirst().get().getValue());
            Collections.reverse(historical);
        } catch (NoSuchElementException e) {
            Collections.reverse(historical);
            relationOptional = Optional.empty();
        }

        return relationOptional;
    }


    /**
     *
     * @param literal
     * @param mapVariables
     * @return
     */
    private static List<String> getAttributesForThisRule(Literal literal, Map<String, String> mapVariables) {
        List<String> attributesForThisRule = new ArrayList<>();
        AtomicInteger positionAttribute = new AtomicInteger();
        positionAttribute.set(0);
        literal.getAtom().getVars().forEach(var -> {
            if (mapVariables.containsKey(var.getName())) {
                attributesForThisRule.add(positionAttribute.get(), mapVariables.get(var.getName()));
            } else {
                attributesForThisRule.add(positionAttribute.get(), null);
            }

            positionAttribute.incrementAndGet();
        });

        return attributesForThisRule;
    }

}
