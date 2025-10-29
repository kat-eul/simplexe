package src;

public class Simplexe {

    private final TableauSimplexe tableau;

    public Simplexe(double[][] A, double[] b, double[] c, char[] symboles, boolean isMaximisation) {
        this.tableau = new TableauSimplexe(A, b, c, symboles, isMaximisation);

        // Affichage initial (conserve le comportement original)
        double z = tableau.calculateZ();
        tableau.afficherSolution(0, z);
    }

    // Délègue la résolution
    public void resoudre() {
        tableau.resoudre();
    }

    // Exemple d'utilisation
    public static void main(String[] args) {
        double[][] A2 = {
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

        Simplexe simplexe2 = new Simplexe(A2, b2, c2, symboles2, isMaximisation2);
        simplexe2.resoudre();
    }
}