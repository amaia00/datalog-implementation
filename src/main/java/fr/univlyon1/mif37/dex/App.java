package fr.univlyon1.mif37.dex;

import fr.univlyon1.mif37.dex.mapping.*;
import fr.univlyon1.mif37.dex.parser.MappingParser;
import fr.univlyon1.mif37.dex.parser.ParseException;
import fr.univlyon1.mif37.dex.utils.EvaluationPositive;
import fr.univlyon1.mif37.dex.utils.Stratified;
import fr.univlyon1.mif37.dex.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws ParseException {
        MappingParser mp = new MappingParser(System.in);

        Mapping mapping = mp.mapping();
        LOG.info("Parsed {} edb(s), {} idb(s) and {} tgd(s).",
                mapping.getEDB().size(),
                mapping.getIDB().size(),
                mapping.getTgds().size());

        /* get EDBs*/
        System.out.println("EDB: ");
        Collection<Relation> edbs = mapping.getEDB();
        for (Relation edb : edbs) {
            System.out.println();
            System.out.print(edb.getName() + "(");
            List<String> attributs = Arrays.asList(edb.getAttributes());
            attributs.forEach(p -> System.out.print(p + ", "));
            System.out.print(")");
        }

        System.out.println();
        System.out.println();

        /* get IDBS*/
        System.out.println("IDB: ");
        Collection<AbstractRelation> idbs = mapping.getIDB();
        for (AbstractRelation idb : idbs) {
            System.out.println();
            System.out.print(idb.getName() + "(");

            List<AbstractArgument> attributs = Arrays.asList(idb.getAttributes());
            attributs.forEach(p -> System.out.print(p.getVar().getName() + ","));
            System.out.print(")");
        }
        System.out.println();
        System.out.println();


        /* get TGBS*/
        System.out.println("TGBS:");
        Collection<Tgd> tgds = mapping.getTgds();
        for (Tgd tgd : tgds) {
            System.out.println();
            System.out.print(tgd.getRight().getName() + "(");
            tgd.getRight().getVars().forEach(v -> System.out.print(v.getName() + ", "));
            System.out.print(") :- ");

            tgd.getLeft().forEach(l -> {
                System.out.print(l.getFlag() + " " + l.getAtom().getName() + " (");
                l.getAtom().getVars().forEach(v -> System.out.print(v.getName() + ", "));
                System.out.print("), ");
            });
        }

        System.out.println();
        System.out.println();

        /* Pour tester si a la condition safety extended*/
        System.out.print("Le programme a la condition safetyExtended? ");
        System.out.print(Stratified.isSemiPositif(edbs, tgds));

        System.out.println();
        System.out.println();

        /* Pour tester si c'est stratifie*/
        System.out.print("Le programme est stratifie? ");
        System.out.print(Stratified.isStratified(mapping));

        System.out.println();
        System.out.println();

        /* Pour tester si c'est positif */
        System.out.print("Le programme est positif? ");
        System.out.print(Stratified.isPositif(mapping));

        System.out.println();
        System.out.println();

        /* Pour recuperer les stratums du programme */
        Map stratum = null;
        try {
            stratum = Stratified.stratification(edbs, idbs, tgds);
            System.out.println("Les stratums du programme: ");
            System.out.println(stratum.toString());
        } catch (Exception e) {
            System.out.println("Le programme n'est pas stratifiable");
        }


        /* Por arrêter Ctrl +  D*/

        Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = new HashMap<>();
        Map<Integer, List<Relation>> edbByOrderOfEvaluation = new HashMap<>();
        try {
            Map<Integer, List<Object>> slices = Stratified.getSlices(mapping);
            for (Map.Entry<Integer, List<Object>> slice : slices.entrySet()) {
                System.out.println(slice.getKey().toString() + ":");

                List<Object> rules = slice.getValue();
                rules.forEach(rule -> {

                    try {
                        Relation edb = (Relation)rule;
                        if (edbByOrderOfEvaluation.containsKey(slice.getKey())) {
                            edbByOrderOfEvaluation.get(slice.getKey()).add(edb);
                        }else {
                            List<Relation> list = new ArrayList<>();
                            list.add(edb);
                            edbByOrderOfEvaluation.put(slice.getKey(), list);
                        }

                        System.out.println(Util.getEDBString(edb));
                    }catch (Exception ex){
                        //nothing
                    }

                    try {
                        Tgd tgd = (Tgd) rule;
                        if (tgdByOrderOfEvaluation.containsKey(slice.getKey()))
                            tgdByOrderOfEvaluation.get(slice.getKey()).add(tgd);
                        else {
                            List<Tgd> list = new ArrayList<>();
                            list.add(tgd);
                            tgdByOrderOfEvaluation.put(slice.getKey(), list);
                        }
                        System.out.println(Util.getTgdString(tgd));
                    }catch (Exception ex) {
                        //nothing
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* EvaluationPostive */

        try {
            System.out.println("EvaluationPostive positive");
            Set<Relation> newFacts;
            //if (Stratified.isPositif(mapping))
                newFacts = EvaluationPositive.evaluate(mapping, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);
            //else {
             //   newFacts = EvaluationStratifie.evaluateSemiposifProgram(mapping, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);
            //}
            //newFacts.forEach(fact -> System.out.println(Util.getEDBString(fact)));
        }catch (Exception e) {
            e.printStackTrace();
        }


    }
}
