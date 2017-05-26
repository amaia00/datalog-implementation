package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;
import fr.univlyon1.mif37.dex.utils.EvaluationPositive;
import fr.univlyon1.mif37.dex.utils.Stratified;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationPositiveTest {

    @Test
    public void example1_1() throws ParseException {
        Reader input = new InputStreamReader(EvaluationPositiveTest.class.getResourceAsStream("/example1_1.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(Stratified.isPositif(m));

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationPositive.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("link")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 7);


        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(4,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(4,m.getTgds().size());

    }

    // TODO Verifier evaluation positive
    @Test
    public void example1_4() throws ParseException {
        Reader input = new InputStreamReader(EvaluationPositiveTest.class.getResourceAsStream("/example1_4.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(Stratified.isPositif(m));

        Map.Entry<Map, Map> edbAndTgdStratums;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationPositive.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("creachable")).count(), 7);


        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(11,m.getEDB().size());
        assertEquals(1,m.getIDB().size());
        assertEquals(1,m.getTgds().size());

    }
}
