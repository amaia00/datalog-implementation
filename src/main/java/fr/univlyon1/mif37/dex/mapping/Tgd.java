package fr.univlyon1.mif37.dex.mapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ecoquery on 20/05/2016.
 */
public class Tgd {

    private List<Literal> left;
    private Atom right;

    public Tgd(Collection<Literal> left, Atom right) {
        this.left = new ArrayList<>(left);
        this.right = right;
    }

    public Collection<Literal> getLeft() {
        return left;
    }

    public List<Literal> getLeftList() {
        List<Literal> leftRules = new ArrayList<>();
        leftRules.addAll(this.left.stream().filter(l -> l.getFlag()).collect(Collectors.toList()));
        leftRules.addAll(this.left.stream().filter(l -> !l.getFlag()).collect(Collectors.toList()));

        return leftRules;
    }

    public List<Literal> getLeftPositiveList() {
        List<Literal> leftRules = new ArrayList<>();
        leftRules.addAll(this.left.stream().filter(l -> l.getFlag()).collect(Collectors.toList()));
        return leftRules;
    }

    public Atom getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tgd tgd = (Tgd) o;

        if (!left.equals(tgd.left)) return false;
        return right.equals(tgd.right);

    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    public boolean isSafe() {
      for (Variable v:right.getVars()) {
        boolean exists = false;
        for (Literal lit:left) {
          if (lit.containsVariable(v)) {
            exists = true;
          }
        }
        // at least one variable does not occur in any literal on RHS
        if (!exists) {
          return false;
        }
      }
      // checked all variables without failure, must be safe
      return true;
    }
}
