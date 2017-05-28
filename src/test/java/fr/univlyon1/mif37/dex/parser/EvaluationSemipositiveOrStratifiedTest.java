package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Relation;
import fr.univlyon1.mif37.dex.mapping.Tgd;
import fr.univlyon1.mif37.dex.utils.EvaluationSemipositiveOrStratified;
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
        Exception exception = null;
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
            exception = e;
        }

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
                ";CREATE or REPLACE VIEW V_unreachable AS\n" +
                "SELECT metro1.c1 as c1, metro2.c1 as c2\n" +
                "FROM metro metro1, metro metro2, reachable reachable3\n" +
                "WHERE reachable3.c2 <> metro2.c1\n" +
                "AND reachable3.c1 = metro1.c1\n" +
                "OR reachable3.c2 = metro2.c1\n" +
                "AND reachable3.c1 <> metro1.c1\n" +
                "\n" +
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
        assertNull(exception);
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
        Exception exception = null;
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
            exception = e;
        }

        String sql = Translating.translate(m.getEDB(), m.getIDB(), m.getTgds());
        assertEquals("DROP TABLE metro ;\n" +
                "CREATE TABLE metro( \n" +
                "\tc1 VARCHAR(150)\n" +
                ");DROP TABLE reachable ;\n" +
                "CREATE TABLE reachable( \n" +
                "\tc1 VARCHAR(150), \n" +
                "\tc2 VARCHAR(150)\n" +
                ");INSERT INTO metro VALUES ('Charpennes');\n" +
                "INSERT INTO metro VALUES ('Perrache');\n" +
                "INSERT INTO metro VALUES ('PartDieu');\n" +
                "INSERT INTO metro VALUES ('Debourg');\n" +
                "INSERT INTO reachable VALUES ('Charpennes', 'Perrache');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Charpennes');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'PartDieu');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Debourg');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Perrache');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Charpennes');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Debourg');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'PartDieu');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Perrache');\n" +
                "CREATE or REPLACE VIEW V_unreachable AS\n" +
                "SELECT metro1.c1 as c1, metro2.c1 as c2\n" +
                "FROM metro metro1, metro metro2, reachable reachable3\n" +
                "WHERE reachable3.c2 <> metro2.c1\n" +
                "AND reachable3.c1 = metro1.c1\n" +
                "OR reachable3.c2 = metro2.c1\n" +
                "AND reachable3.c1 <> metro1.c1\n" +
                "\n" +
                ";",sql);

        assertNull(exception);
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
        Exception exception = null;
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
            exception = e;
        }

        String sql = Translating.translate(m.getEDB(), m.getIDB(), m.getTgds());
        assertEquals("DROP TABLE link ;\n" +
                "CREATE TABLE link( \n" +
                "\tc1 VARCHAR(150), \n" +
                "\tc2 VARCHAR(150)\n" +
                ");DROP TABLE reachable ;\n" +
                "CREATE TABLE reachable( \n" +
                "\tc1 VARCHAR(150), \n" +
                "\tc2 VARCHAR(150)\n" +
                ");INSERT INTO link VALUES ('Charpennes', 'Perrache');\n" +
                "INSERT INTO link VALUES ('PartDieu', 'Charpennes');\n" +
                "INSERT INTO link VALUES ('Debourg', 'PartDieu');\n" +
                "INSERT INTO link VALUES ('PartDieu', 'Debourg');\n" +
                "INSERT INTO reachable VALUES ('Charpennes', 'Perrache');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Charpennes');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'PartDieu');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Debourg');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'Perrache');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Charpennes');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Debourg');\n" +
                "INSERT INTO reachable VALUES ('PartDieu', 'PartDieu');\n" +
                "INSERT INTO reachable VALUES ('Debourg', 'Perrache');\n" +
                "CREATE or REPLACE VIEW V_metro AS\n" +
                "SELECT link1.c1 as c1\n" +
                "FROM link link1\n" +
                ";CREATE or REPLACE VIEW V_metro AS\n" +
                "SELECT link1.c2 as c1\n" +
                "FROM link link1\n" +
                ";CREATE or REPLACE VIEW V_unreachable AS\n" +
                "SELECT metro1.c1 as c1, metro2.c1 as c2\n" +
                "FROM metro metro1, metro metro2, reachable reachable3\n" +
                "WHERE reachable3.c2 <> metro2.c1\n" +
                "AND reachable3.c1 = metro1.c1\n" +
                "OR reachable3.c2 = metro2.c1\n" +
                "AND reachable3.c1 <> metro1.c1\n" +
                "\n" +
                ";",sql);

        assertNull(exception);
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
        Exception exception = null;
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
            exception = e;
        }

        String sql = Translating.translate(m.getEDB(), m.getIDB(), m.getTgds());
        assertEquals("DROP TABLE q ;\n" +
                "CREATE TABLE q( \n" +
                "\tc1 VARCHAR(150)\n" +
                ");DROP TABLE s ;\n" +
                "CREATE TABLE s( \n" +
                "\tc1 VARCHAR(150)\n" +
                ");DROP TABLE t ;\n" +
                "CREATE TABLE t( \n" +
                "\tc1 VARCHAR(150)\n" +
                ");INSERT INTO q VALUES ('A');\n" +
                "INSERT INTO s VALUES ('B');\n" +
                "INSERT INTO t VALUES ('A');\n" +
                "CREATE or REPLACE VIEW V_p AS\n" +
                "SELECT q1.c1 as c1\n" +
                "FROM q q1, r r2\n" +
                "WHERE q1.c1 <> r2.c1\n" +
                "\n" +
                ";CREATE or REPLACE VIEW V_p AS\n" +
                "SELECT t1.c1 as c1\n" +
                "FROM t t1, q q2\n" +
                "WHERE t1.c1 <> q2.c1\n" +
                "\n" +
                ";CREATE or REPLACE VIEW V_q AS\n" +
                "SELECT s1.c1 as c1\n" +
                "FROM s s1, t t2\n" +
                "WHERE t2.c1 <> s1.c1\n" +
                "\n" +
                ";CREATE or REPLACE VIEW V_r AS\n" +
                "SELECT t1.c1 as c1\n" +
                "FROM t t1\n" +
                ";",sql);

        assertNull(exception);
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
        Exception exception = null;
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
            exception = e;
        }

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
                "SELECT metro1.c1 as c1, reachable2.c2 as c2\n" +
                "FROM metro metro1, reachable reachable2\n" +
                "\n" +
                ")\n" +
                ") SELECT * FROM rec_reachable ;",sql);

        assertNull(exception);
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
        Exception exception = null;
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
            exception = e;
        }

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
                ";CREATE or REPLACE VIEW V_unreachable AS\n" +
                "SELECT metro1.c1 as c1, metro2.c1 as c2\n" +
                "FROM metro metro1, metro metro2, reachable reachable3\n" +
                "WHERE reachable3.c2 <> metro2.c1\n" +
                "AND reachable3.c1 = metro1.c1\n" +
                "OR reachable3.c2 = metro2.c1\n" +
                "AND reachable3.c1 <> metro1.c1\n" +
                "\n" +
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

        assertNull(exception);
    }
}
