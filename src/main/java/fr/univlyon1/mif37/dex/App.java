package fr.univlyon1.mif37.dex;

import fr.univlyon1.mif37.dex.mapping.*;
import fr.univlyon1.mif37.dex.parser.MappingParser;
import fr.univlyon1.mif37.dex.parser.ParseException;
import fr.univlyon1.mif37.dex.utils.Stratified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

            tgd.getLeft().stream().forEach(l -> {
                System.out.print(l.getFlag() + " " + l.getAtom().getName() + " (");
                l.getAtom().getVars().forEach(v -> System.out.print(v.getName() + ", "));
                System.out.print("), ");
            });
        }


        System.out.println();
        System.out.println();

        System.out.println(Stratified.ExtendedSafetyCondition(edbs, tgds));

        /* Por arrêter Ctrl +  D*/


    }
}
