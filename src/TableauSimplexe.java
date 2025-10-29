package src;
public class TableauSimplexe {

    private static final double M = 10000000;

    private final double[][] tableau;
    private final int nbContraintes;
    private final int nbVariables;
    private final int[] base;
    private final double[] b;
    private final double[] c;
    private final int nbCollones;
    private final boolean isMaximisation;

    TableauSimplexe(double[][] A, double[] b, double[] c, char[] symboles, boolean isMaximisation) {
        this.nbContraintes = A.length;
        this.nbVariables = c.length;
        this.b = b;
        this.isMaximisation = isMaximisation;

        // On compte les variables d'écarts et artificielles à ajouter
        int nbArtificielles = 0;
        int nbEcart = 0;
        for (char s : symboles) {
            switch (s) {
                case '<': nbEcart++; break;
                case '=': nbArtificielles++; break;
                case '>': nbArtificielles++; nbEcart++; break;
            }
        }

        this.nbCollones = nbVariables + nbEcart + nbArtificielles;
        this.tableau = new double[nbContraintes][nbCollones];
        this.base = new int[nbContraintes];

        // --- Construction du tableau ---
        int indiceEcart = 0;
        int indiceArtificielle = 0;
        for (int i = 0; i < nbContraintes; i++) {
            // On remplit le tableau avec les variables d'origines
            for (int j = 0; j < nbVariables; j++) {
                tableau[i][j] = A[i][j];
            }
            // Définir les variables en base
            // NOTE: j'ai conservé la logique originale (avec -1) pour ne pas changer le comportement.
            if (symboles[i] == '<') {
                base[i] = nbVariables + indiceEcart - 1;
            } else {
                base[i] = nbVariables + nbEcart + indiceArtificielle - 1;
            }

            // On remplit les variables d'écarts et artificielles
            switch (symboles[i]) {
                case '<':
                    tableau[i][nbVariables + indiceEcart] = 1;
                    indiceEcart++;
                    break;
                case '=':
                    tableau[i][nbVariables + nbEcart + indiceArtificielle] = 1;
                    indiceArtificielle++;
                    break;
                case '>':
                    tableau[i][nbVariables + indiceEcart] = -1;
                    tableau[i][nbVariables + nbEcart + indiceArtificielle] = 1;
                    indiceEcart++;
                    indiceArtificielle++;
                    break;
            }
        }

        // On remplit le tableau des Cj
        this.c = new double[nbCollones];
        for (int i = 0; i < nbVariables; i++) {
            this.c[i] = c[i];
        }
        for (int i = nbVariables; i < (nbVariables + nbEcart); i++) {
            this.c[i] = 0;
        }
        for (int i = (nbVariables + nbEcart); i < nbCollones; i++) {
            this.c[i] = isMaximisation ? -M : M;
        }
    }

    // === Algorithme du simplexe ===
    void resoudre() {
        int colPivot, lignePivot;
        int etape = 1;
        while ((colPivot = colonnePivot()) != -1 && etape <= 10) { //CRITERE 1 et 3
            lignePivot = lignePivot(colPivot); // CRITERE 2
            System.out.println("Var entrante = " + colPivot);
            System.out.println("Var sortante = " + base[lignePivot]);
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
        System.out.println("FINAL");
    }

    int colonnePivot() {
        int pivot = -1;
        double val_pivot = 0;
        for (int j = 0; j < nbCollones; j++) {

            double zj = 0;
            for (int i = 0; i < nbContraintes; i++) {
                zj += c[base[i]] * tableau[i][j];
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

    int lignePivot(int col) {
        int ligne = -1;
        double minRatio = -1; // valeur impossible initiale
        for (int i = 0; i < nbContraintes; i++) {
            System.out.print("R" + i + "  ");

            if (tableau[i][col] > 0) {
                double ratio = b[i] / tableau[i][col];
                System.out.println(" = " + ratio);
                if (ratio < minRatio || minRatio == -1) {
                    minRatio = ratio;
                    ligne = i;
                }
            }
        }
        return ligne;
    }

    void pivoter(int ligne, int col) {
        double pivot = tableau[ligne][col];

        // Modif Lp
        for (int j = 0; j < nbCollones; j++) {
            tableau[ligne][j] /= pivot;
        }
        b[ligne] /= pivot;

        // Modif des autres lignes sauf Lp
        for (int i = 0; i < nbContraintes; i++) {
            if (i != ligne) {
                double facteur = tableau[i][col];
                for (int j = 0; j < nbCollones; j++) {
                    tableau[i][j] -= facteur * tableau[ligne][j];
                }
                b[i] -= facteur * b[ligne];
            }
        }
    }

    double calculateZ() {
        double z = 0;
        for (int i = 0; i < nbContraintes; i++) {
            z += c[base[i]] * b[i];
        }
        return z;
    }

    void afficherSolution(int etape, double z) {
        System.out.println("T" + etape + " --------------------------------------");
        for (int i = 0; i < nbContraintes; i++) {
            for (int j = 0; j < nbCollones; j++) {
                System.out.print(tableau[i][j] + "|");
            }
            // Conserver System.err comme dans l'original
            System.err.println("| B = " + b[i]);
            System.out.println("-------------------------------------------------------");
        }
        for (double constante : this.c) {
            System.out.print(constante + "|");
        }
        System.out.println("Z = " + z);
    }
}