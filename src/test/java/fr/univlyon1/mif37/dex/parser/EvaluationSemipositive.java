package fr.univlyon1.mif37.dex.parser;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.utils.Stratified;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationSemipositive {

    @Test
    public void example1_2() throws ParseException {
        Reader input = new InputStreamReader(EvaluationSemipositive.class.getResourceAsStream("/example2.txt"));
        MappingParser parser = new MappingParser(input);
        Mapping m = parser.mapping();
        assertNotNull(m);
        assert(!Stratified.isPositif(m));
        assertEquals(12,m.getEDB().size());
        assertEquals(2,m.getIDB().size());
        assertEquals(3,m.getTgds().size());
    }
}
