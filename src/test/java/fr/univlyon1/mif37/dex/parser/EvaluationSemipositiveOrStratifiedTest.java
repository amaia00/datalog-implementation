package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;
import fr.univlyon1.mif37.dex.utils.EvaluationSemipositiveOrStratified;
import fr.univlyon1.mif37.dex.utils.Stratified;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationSemipositiveOrStratifiedTest {

    @Test
    public void example1_2() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example1_2.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(4,m.getEDB().size());
        assertEquals(3,m.getIDB().size());
        assertEquals(5,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 9);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);




        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void example1_3() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example1_3.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(13,m.getEDB().size());
        assertEquals(1,m.getIDB().size());
        assertEquals(1,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 9);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void example2() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example2.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(13,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(3,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("link")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 9);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void example2_1() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example2_1.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(3,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(4,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("r")).count(), 1);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("q")).count(), 2);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("t")).count(), 1);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("s")).count(), 1);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("p")).count(), 1);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void example3() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example3.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(4,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(4,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("link")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 4);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void example5_1() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositiveOrStratifiedTest.class.getResourceAsStream("/example5_1.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(4,m.getEDB().size());
        assertEquals(3,m.getIDB().size());
        assertEquals(5,m.getTgds().size());

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationSemipositiveOrStratified.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("link")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 9);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
