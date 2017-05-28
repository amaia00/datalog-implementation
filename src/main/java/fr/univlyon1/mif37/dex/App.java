package fr.univlyon1.mif37.dex;

import fr.univlyon1.mif37.dex.mapping.*;
import fr.univlyon1.mif37.dex.parser.MappingParser;
import fr.univlyon1.mif37.dex.parser.ParseException;
import fr.univlyon1.mif37.dex.utils.EvaluationPositive;
import fr.univlyon1.mif37.dex.utils.EvaluationSemipositiveOrStratified;
import fr.univlyon1.mif37.dex.utils.Stratified;
import fr.univlyon1.mif37.dex.utils.Translating;
import fr.univlyon1.mif37.dex.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final boolean DEBUG = false;

    public static void main(String[] args) throws ParseException {
        MappingParser mp = new MappingParser(System.in);

        Mapping mapping = mp.mapping();
        LOG.info("Parsed {} edb(s), {} idb(s) and {} tgd(s).",
                mapping.getEDB().size(),
                mapping.getIDB().size(),
                mapping.getTgds().size());

        if (DEBUG) {
            Util.printEDBs(mapping.getEDB());
            Util.printIdbs(mapping.getIDB());
            Util.printTgds(mapping.getTgds());
        }

        System.out.println();
        System.out.println();

        /* Pour tester si c'est positif */
        System.out.print("Le programme est positif? ");
        System.out.print(Stratified.isPositif(mapping));

        System.out.println();
        System.out.println();

        System.out.print("Le programme est semipositive? ");
        System.out.print(Stratified.isSemiPositif(mapping.getEDB(), mapping.getTgds()));

        System.out.println();
        System.out.println();

        /* Pour tester si c'est stratifie*/
        System.out.print("Le programme est stratifie? ");
        System.out.print(Stratified.isStratified(mapping));

        System.out.println();
        System.out.println();

        /* Pour recuperer les stratums du programme */
        Map stratum = null;
        try {
            stratum = Stratified.stratification(mapping.getEDB(), mapping.getIDB(), mapping.getTgds());
            System.out.println("Les stratums du programme: ");
            System.out.println(stratum.toString());
        } catch (Exception e) {
            System.out.println("Le programme n'est pas stratifiable");
        }

        System.out.println();
        System.out.println();

        /* Por arrêter Ctrl +  D*/

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(mapping);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* EvaluationPostive */
            List<Relation> allFacts;
            System.out.println("Evaluation");

            if (Stratified.isPositif(mapping))
                allFacts = EvaluationPositive.evaluate(mapping, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);
            else
                allFacts = EvaluationSemipositiveOrStratified.evaluate(mapping, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            allFacts.forEach(fact -> System.out.println(Util.getEDBString(fact)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();

        /* Translating */
        System.out.println("Translation to SQL:");
        Translating.translate(mapping.getEDB(), mapping.getIDB(), mapping.getTgds());

    }
}
