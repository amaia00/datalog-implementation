package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.utils.Stratified;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationPositive {

    @Test
    public void example1_1() throws ParseException {
        Reader input = new InputStreamReader(EvaluationPositive.class.getResourceAsStream("/example1_1.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(Stratified.isPositif(m));

        assertEquals(4,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(4,m.getTgds().size());

    }
}
