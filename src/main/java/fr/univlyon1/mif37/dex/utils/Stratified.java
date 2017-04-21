package fr.univlyon1.mif37.dex.utils;

import fr.univlyon1.mif37.dex.mapping.Mapping;
import fr.univlyon1.mif37.dex.mapping.Tgd;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Amaia Nazábal
 * @version 1.0
 * @since 1.0 4/21/17.
 */
public class Stratified {

    /**
     *
     * @param mapping
     * @return
     */
    public static boolean safeConfition(Mapping mapping) {

        AtomicBoolean safeConfition = new AtomicBoolean();
        safeConfition.set(true);
        mapping.getTgds().forEach(tgd -> {
            if (!tgd.isSafe()) {
                safeConfition.set(false);
            }
        });

        return safeConfition.get();
    }

    public static boolean isSemiPositif(Tgd tgd) {
        return false;
    }

    /**
     *
     * @return
     */
    public static boolean safeExtendedCondition() {
        return false;
    }

    /**
     *
     * @param mapping
     * @return
     */
    public static boolean isStratified(Mapping mapping) {
        return (safeConfition(mapping) && safeExtendedCondition());
    }

    public static Mapping fromSemipositiveToPositive(Mapping mapping) {
        mapping.getTgds().forEach(tgd -> {
            if (isSemiPositif(tgd)) {

            }
        });

        return mapping;
    }


}
