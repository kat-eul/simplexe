package src;

import java.util.Arrays;

public class TableauSimplexe {

    private static final double M = 10_000_000d;

    private final double[][] tableau;
    private final int nbContraintes;
    private final int nbVariables;
    private final int[] base;
    private final double[] b;
    private final double[] c;
    private final int nbCollones;
    private final boolean isMaximisation;
    private final int MAX_ITER = 1000;

    public TableauSimplexe(double[][] A_orig, double[] b_orig, double[] c_orig, char[] symboles, boolean isMaximisation) {
        // Validations d'entrée
        if (A_orig == null || b_orig == null || c_orig == null || symboles == null) {
            throw new IllegalArgumentException("Les matrices/vecteurs fournis ne doivent pas être null.");
        }
        this.nbContraintes = A_orig.length;
        this.nbVariables = c_orig.length;
        if (symboles.length != nbContraintes) {
            throw new IllegalArgumentException("La longueur de symboles doit être égale au nombre de contraintes.");
        }
        if (b_orig.length != nbContraintes) {
            throw new IllegalArgumentException("La longueur de b doit être égale au nombre de contraintes.");
        }

        // Cloner les entrées pour éviter effets de bord
        double[][] A = new double[nbContraintes][];
        for (int i = 0; i < nbContraintes; i++) {
            A[i] = Arrays.copyOf(A_orig[i], nbVariables);
        }
        this.b = Arrays.copyOf(b_orig, b_orig.length);
        double[] c_in = Arrays.copyOf(c_orig, c_orig.length);
        this.isMaximisation = isMaximisation;

        // Compter les variables d'écart (slack/surplus) et artificielles
        int nbArtificielles = 0;
        int nbEcart = 0;
        for (char s : symboles) {
            switch (s) {
                case '<': nbEcart++; break;
                case '=': nbArtificielles++; break;
                case '>': nbEcart++; nbArtificielles++; break;
                default: throw new IllegalArgumentException("Symbole inattendu: " + s);
            }
        }

        this.nbCollones = nbVariables + nbEcart + nbArtificielles;
        this.tableau = new double[nbContraintes][nbCollones];
        this.base = new int[nbContraintes];

        // Construction du tableau : placer les coefficients des variables d'origine
        for (int i = 0; i < nbContraintes; i++) {
            for (int j = 0; j < nbVariables; j++) {
                tableau[i][j] = A[i][j];
            }
        }

        // On ajoute les colonnes d'écart et artificielles et on initialise base correctement
        int indiceEcart = 0;
        int indiceArtificielle = 0;
        for (int i = 0; i < nbContraintes; i++) {
            switch (symboles[i]) {
                case '<':
                    // variable d'écart +1 et mise en base
                    int colEcart = nbVariables + indiceEcart;
                    tableau[i][colEcart] = 1.0;
                    base[i] = colEcart;
                    indiceEcart++;
                    break;
                case '=':
                    // pas de slack, on place l'artificielle et la met en base
                    int colArtEq = nbVariables + nbEcart + indiceArtificielle;
                    tableau[i][colArtEq] = 1.0;
                    base[i] = colArtEq;
                    indiceArtificielle++;
                    break;
                case '>':
                    // surplus (-1) + artificielle (+1), artificielle en base
                    int colEcartSurplus = nbVariables + indiceEcart;
                    int colArtSup = nbVariables + nbEcart + indiceArtificielle;
                    tableau[i][colEcartSurplus] = -1.0;
                    tableau[i][colArtSup] = 1.0;
                    base[i] = colArtSup;
                    indiceEcart++;
                    indiceArtificielle++;
                    break;
                default:
                    base[i] = -1;
                    break;
            }
        }

        // Construire c pour nbCollones
        this.c = new double[nbCollones];
        // variables d'origine
        for (int i = 0; i < nbVariables; i++) {
            this.c[i] = c_in[i];
        }
        // variables d'écart (slack/surplus) = 0
        for (int i = nbVariables; i < nbVariables + nbEcart; i++) {
            this.c[i] = 0.0;
        }
        // variables artificielles : Big-M (pénaliser artificielles)
        for (int i = nbVariables + nbEcart; i < nbCollones; i++) {
            this.c[i] = isMaximisation ? -M : M;
        }
    }

    public void resoudre() {
        int colPivot, lignePivot;
        int etape = 1;
        while ((colPivot = colonnePivot()) != -1 && etape <= MAX_ITER) {
            lignePivot = lignePivot(colPivot);
            System.out.println("Var entrante = " + colPivot);
            if (lignePivot >= 0) {
                System.out.println("Var sortante = " + base[lignePivot]);
            } else {
                System.out.println("Var sortante = null");
            }
            if (lignePivot == -1) {
                System.out.println("⚠️  Solution non bornée !");
                return;
            }
            pivoter(lignePivot, colPivot);
            base[lignePivot] = colPivot;

            double z = calculateZ();
            afficherSolution(etape, z);
            etape++;
        }
        if (etape > MAX_ITER) {
            System.out.println("⚠️  Arrêt : nombre maximal d'itérations atteint (" + MAX_ITER + ").");
        } else {
            System.out.println("FINAL");
        }
    }

    public int colonnePivot() {
        int pivot = -1;
        double val_pivot = 0.0;
        for (int j = 0; j < nbCollones; j++) {
            double zj = 0.0;
            for (int i = 0; i < nbContraintes; i++) {
                int bi = base[i];
                if (bi >= 0 && bi < c.length) {
                    zj += c[bi] * tableau[i][j];
                }
            }
            double actual_val = c[j] - zj;
            System.out.println("i=" + j + " | Cp-Zj=" + actual_val);
            if ((isMaximisation && actual_val > val_pivot) || (!isMaximisation && actual_val < val_pivot)) {
                val_pivot = actual_val;
                pivot = j;
            }
        }
        return pivot;
    }

    public int lignePivot(int col) {
        int ligne = -1;
        double minRatio = -1.0;
        for (int i = 0; i < nbContraintes; i++) {
            System.out.print("R" + i + "  ");
            double coeff = tableau[i][col];
            if (coeff > 0.0) {
                double ratio = b[i] / coeff;
                System.out.println(" = " + ratio);
                if (ratio < minRatio || minRatio == -1.0) {
                    minRatio = ratio;
                    ligne = i;
                }
            } else {
                System.out.println();
            }
        }
        return ligne;
    }

    public void pivoter(int ligne, int col) {
        double pivot = tableau[ligne][col];
        if (pivot == 0.0) {
            throw new ArithmeticException("Pivot nul détecté à ligne " + ligne + " colonne " + col);
        }

        // normaliser la ligne pivot
        for (int j = 0; j < nbCollones; j++) {
            tableau[ligne][j] /= pivot;
        }
        b[ligne] /= pivot;

        // élimination sur les autres lignes
        for (int i = 0; i < nbContraintes; i++) {
            if (i == ligne) continue;
            double facteur = tableau[i][col];
            if (facteur == 0.0) continue;
            for (int j = 0; j < nbCollones; j++) {
                tableau[i][j] -= facteur * tableau[ligne][j];
            }
            b[i] -= facteur * b[ligne];
        }
    }

    public double calculateZ() {
        double z = 0.0;
        for (int i = 0; i < nbContraintes; i++) {
            int bi = base[i];
            if (bi >= 0 && bi < c.length) {
                z += c[bi] * b[i];
            }
        }
        return z;
    }

    public void afficherSolution(int etape, double z) {
        System.out.println("T" + etape + " --------------------------------------");
        for (int i = 0; i < nbContraintes; i++) {
            for (int j = 0; j < nbCollones; j++) {
                System.out.print(tableau[i][j] + "|");
            }
            System.err.println("| B = " + b[i]);
            System.out.println("-------------------------------------------------------");
        }
        for (double constante : this.c) {
            System.out.print(constante + "|");
        }
        System.out.println("Z = " + z);
    }
}