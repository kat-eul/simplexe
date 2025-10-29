package src;

public class Simplexe {

    private final TableauSimplexe tableau;

    public Simplexe(double[][] A, double[] b, double[] c, char[] symboles, boolean isMaximisation) {
        this.tableau = new TableauSimplexe(A, b, c, symboles, isMaximisation);

        // Fonction objectif initiale et affichage initial (comme dans l'original)
        double z = tableau.calculateZ();
        tableau.afficherSolution(0, z);
    }

    // Délègue la résolution au tableau
    public void resoudre() {
        tableau.resoudre();
    }

    // === Exemple d'utilisation ===
    public static void main(String[] args) {
        // Exemple :
        // Max Z = 3x1 + 2x2
        // s.c. :
        // 2x1 + x2 <= 18
        // 2x1 + 3x2 <= 42
        // 3x1, x2 <= 24
        // x1, x2 >=0

        double[][] A = {
            {2, 1},
            {2, 3},
            {3,1}
        };
        double[] b = {18,42,24};
        double[] c = {3,2};
        char[] symboles = {'<','<','<'};
        boolean isMaximisation = true;

        Simplexe simplexe = new Simplexe(A, b, c, symboles,isMaximisation);
        simplexe.resoudre();

        // Exemple :
        // Max Z = 5x1 + 4x2 + 6x3
        // s.c. :
        // x1 + x2 + x3 = 225
        // x1 >= 45
        // x2 <= 55
        // x3 >= 70
        // x3 <= 100
        // x1, x2, x3 >= 0

        /*double[][] A2 = {
            {1,1,1},
            {1,0,0},
            {0,1,0},
            {0,0,1},
            {0,0,1}
        };
        double[] b2 = {225,45,55,70,100};
        double[] c2 = {5,4,6};
        char[] symboles2 = {'=','>','<','>','<'};
        boolean isMaximisation2 = false;

        Simplexe simplexe2 = new Simplexe(A2, b2, c2,symboles2,isMaximisation2);
        simplexe2.resoudre();*/
    }
}