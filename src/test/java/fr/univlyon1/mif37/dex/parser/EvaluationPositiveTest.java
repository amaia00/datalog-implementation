package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;
import fr.univlyon1.mif37.dex.utils.EvaluationPositive;
import fr.univlyon1.mif37.dex.utils.Stratified;
import fr.univlyon1.mif37.dex.utils.Translating;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EvaluationPositiveTest {

    @Test
    public void example1_1() throws ParseException {
        Reader input = new InputStreamReader(EvaluationPositiveTest.class.getResourceAsStream("/example1_1.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(Stratified.isPositif(m));

        Map.Entry<Map, Map> edbAndTgdStratums;
        Exception exception = null;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationPositive.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);

            assertEquals(allFacts.stream().filter(f -> f.getName().equals("link")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("reachable")).count(), 9);


        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }

        assertNull(exception);

        assertEquals(4,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(4,m.getTgds().size());
        String sql = Translating.translate(m.getEDB(), m.getIDB(), m.getTgds());
        assertEquals("DROP TABLE link ;\n" +
                "CREATE TABLE link( \n" +
                "\tc1 VARCHAR(150), \n" +
                "\tc2 VARCHAR(150)\n" +
                ");INSERT INTO link VALUES ('Charpennes', 'Perrache');\n" +
                "INSERT INTO link VALUES ('PartDieu', 'Charpennes');\n" +
                "INSERT INTO link VALUES ('Debourg', 'PartDieu');\n" +
                "INSERT INTO link VALUES ('PartDieu', 'Debourg');\n" +
                "CREATE or REPLACE VIEW V_metro AS\n" +
                "SELECT link1.c1 as c1\n" +
                "FROM link link1\n" +
                ";CREATE or REPLACE VIEW V_metro AS\n" +
                "SELECT link1.c2 as c1\n" +
                "FROM link link1\n" +
                ";CREATE or REPLACE VIEW V_reachable AS\n" +
                "WITH rec_reachable AS ((\n" +
                "SELECT link1.c1 as c1, link1.c2 as c2\n" +
                "FROM link link1\n" +
                ")\n" +
                "UNION ALL (\n" +
                "SELECT link1.c1 as c1, reachable2.c2 as c2\n" +
                "FROM link link1, reachable reachable2\n" +
                "WHERE link1.c2 = reachable2.c1 \n" +
                ")\n" +
                ") SELECT * FROM rec_reachable ;",sql);
    }

    @Test
    public void example1_4() throws ParseException {
        Reader input = new InputStreamReader(EvaluationPositiveTest.class.getResourceAsStream("/example1_4.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(Stratified.isPositif(m));

        Map.Entry<Map, Map> edbAndTgdStratums;
        Exception exception = null;
        try {
            edbAndTgdStratums = Stratified.getRulesByStratum(m);

            Map<Integer, List<Relation>> edbByOrderOfEvaluation = edbAndTgdStratums.getKey();
            Map<Integer, List<Tgd>> tgdByOrderOfEvaluation = edbAndTgdStratums.getValue();

            /* Evaluation */
            List<Relation> allFacts = EvaluationPositive.evaluate(m, tgdByOrderOfEvaluation, edbByOrderOfEvaluation);


            assertEquals(allFacts.stream().filter(f -> f.getName().equals("metro")).count(), 4);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("creachable")).count(), 7);
            assertEquals(allFacts.stream().filter(f -> f.getName().equals("unreachable")).count(), 7);


        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }

        assertNull(exception);

        assertEquals(11,m.getEDB().size());
        assertEquals(1,m.getIDB().size());
        assertEquals(1,m.getTgds().size());

        String sql = Translating.translate(m.getEDB(), m.getIDB(), m.getTgds());
        assertEquals("DROP TABLE creachable ;\n" +
                "CREATE TABLE creachable( \n" +
                "\tc1 VARCHAR(150), \n" +
                "\tc2 VARCHAR(150)\n" +
                ");DROP TABLE metro ;\n" +
                "CREATE TABLE metro( \n" +
                "\tc1 VARCHAR(150)\n" +
                ");INSERT INTO metro VALUES ('Charpennes');\n" +
                "INSERT INTO metro VALUES ('Perrache');\n" +
                "INSERT INTO metro VALUES ('PartDieu');\n" +
                "INSERT INTO metro VALUES ('Debourg');\n" +
                "INSERT INTO creachable VALUES ('Charpennes', 'Charpennes');\n" +
                "INSERT INTO creachable VALUES ('Charpennes', 'PartDieu');\n" +
                "INSERT INTO creachable VALUES ('Charpennes', 'Debourg');\n" +
                "INSERT INTO creachable VALUES ('Perrache', 'Charpennes');\n" +
                "INSERT INTO creachable VALUES ('Perrache', 'PartDieu');\n" +
                "INSERT INTO creachable VALUES ('Perrache', 'Perrache');\n" +
                "INSERT INTO creachable VALUES ('Perrache', 'Debourg');\n" +
                "CREATE or REPLACE VIEW V_unreachable AS\n" +
                "SELECT metro1.c1 as c1, metro2.c1 as c2\n" +
                "FROM metro metro1, metro metro2, creachable creachable3\n" +
                "WHERE metro1.c1 = creachable3.c1 \n" +
                "AND metro2.c1 = creachable3.c2 \n" +
                ";",sql);
    }
}
