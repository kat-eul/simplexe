
public class Simplexe{

    private static final double M = 10000000;
    private double[][] tableau;
    private int nbContraintes;
    private int nbVariables;
    private int[] base;
    private double[] b;
    private double[] c;
    int nbCollones;
    boolean isMaximisation;

    public Simplexe(double[][] A, double[] b, double[] c, char[] symboles, boolean isMaximisation) {
        nbContraintes = A.length;
        nbVariables = c.length;
        this.b = b;
        this.isMaximisation = isMaximisation;

        // On compte les variables d'écarts et artificielles à ajouter
        int nbArtificielles = 0;
        int nbEcart = 0;
        for(char s : symboles){
            switch (s) {
                case '<' : nbEcart++; break;
                case '=' : nbArtificielles++; break;
                case '>' : nbArtificielles++;nbEcart++;break;
            }
        }

        nbCollones = nbVariables + nbEcart + nbArtificielles;
        tableau = new double[nbContraintes][nbCollones];
        base = new int[nbContraintes];

        // --- Construction du tableau ---
        int indiceEcart = 0;
        int indiceArtificielle = 0;
        for (int i = 0; i < nbContraintes; i++) {
            // On remplit le tableau avec les variables d'origines
            for (int j = 0; j < nbVariables; j++) {
                tableau[i][j] = A[i][j];
            }
            // Définir les variables en base
            if(symboles[i] =='<'){
                base[i] = nbVariables+indiceEcart-1;
            }else{
                base[i] = nbVariables+nbEcart+indiceArtificielle-1; 
            }

            // On remplit les variables d'écarts et artificielles
            switch (symboles[i]) {
                case '<':
                tableau[i][nbVariables+indiceEcart] = 1;
                indiceEcart++;
                    break;
                case '=':
                tableau[i][nbVariables+nbEcart+indiceArtificielle] = 1;
                indiceArtificielle++;
                    break;
                case '>':
                tableau[i][nbVariables+indiceEcart] = -1;
                tableau[i][nbVariables+nbEcart+indiceArtificielle] = 1;
                indiceEcart++;
                indiceArtificielle++;
                    break;
            }
        }

        //On remplit le tableau des Cj
        this.c = new double[nbCollones];
        for(int i=0 ; i<nbVariables ; i++){
            this.c[i] = c[i];
        }
        for (int i = nbVariables; i < (nbVariables+nbEcart); i++) {
            this.c[i] = 0;
        }
        for (int i=(nbVariables+nbEcart) ; i<nbCollones ; i++){
            this.c[i] = isMaximisation? -M : M;
        }

        // Fonction objectif :
        double z = calculateZ();

        afficherSolution(0, z);

        //OK : pour max et <, à avoir pour le reste après
    }

    // === Algorithme du simplexe ===
    public void resoudre() {
        int colPivot, lignePivot;
        int etape = 1;
        while ((colPivot = colonnePivot()) != -1 && etape<=10) { //CRITERE 1 et 3 : On cherche la var entrante : + grand c en max (pos) et + petit c (neg) en min (-1 = FINI)
            lignePivot = lignePivot(colPivot); // CRITERE 2 : on cherche var sortante
            System.out.println("Var entrante = "+colPivot);
            System.out.println("Var sortante = "+base[lignePivot]);
            if (lignePivot == -1) {
                System.out.println("⚠️  Solution non bornée !");
                return;
            }
            //OK
            pivoter(lignePivot, colPivot);
            base[lignePivot]=colPivot;

            double z = calculateZ();
            afficherSolution(etape,z);
            etape++;
        }
        System.out.println("FINAL");
    }

    private int colonnePivot() { //OK
        int pivot = -1;
        double val_pivot = 0;
        for (int j = 0; j < nbCollones; j++) {

            double zj = 0;
                for(int i=0; i<nbContraintes; i++){
                    zj += c[base[i]]*tableau[i][j];
                }
            double actual_val = c[j]-zj;
            System.out.println("i="+j+" | Cp-Zj="+actual_val);
            if((isMaximisation && actual_val > val_pivot) || (!isMaximisation && actual_val<val_pivot)){
                val_pivot = actual_val;
                pivot = j;
            }
        }
        return pivot;
    }

    private int lignePivot(int col) {
        int ligne = -1;
        double minRatio = -1; //pas possible
        for (int i = 0; i < nbContraintes; i++) {
            System.out.print("R"+i+"  ");
            
            if (tableau[i][col] > 0) {
                double ratio = b[i] / tableau[i][col];
                System.out.println(" = "+ratio);
                if (ratio < minRatio || minRatio==-1) {
                    minRatio = ratio;
                    ligne = i;
                }
            }
        }
        return ligne;
    }

    private void pivoter(int ligne, int col) { //OK
        double pivot = tableau[ligne][col];

        //Modif Lp
        for (int j = 0; j < nbCollones; j++) {
            tableau[ligne][j] /= pivot; 
        }
        b[ligne] /= pivot; 

        //Modif des autres lignes sauf Lp
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

    private double  calculateZ(){ //OK
        double z = 0;
        for (int i=0 ; i<nbContraintes ; i++){
            z += c[base[i]]*b[i];
        }
        return z;
    }

    private void afficherSolution(int etape, double z) {
        System.out.println("T"+etape+" --------------------------------------");
        for (int i =0 ; i<nbContraintes ; i++){
            for(int j=0 ; j<nbCollones ; j++){
                System.out.print(tableau[i][j]+ "|");
            }
            System.err.println("| B = "+b[i]);
            System.out.println("-------------------------------------------------------");
        }
        for (double constante : this.c){
            System.out.print(constante+ "|");
        }
        System.out.println("Z = "+z);
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

        /*double[][] A = {
            {2, 1},
            {2, 3},
            {3,1}
        };
        double[] b = {18,42,24};
        double[] c = {3,2};
        char[] symboles = {'<','<','<'};
        boolean isMaximisation = true;

        Simplexe simplexe = new Simplexe(A, b, c, symboles,isMaximisation);
        simplexe.resoudre();*/

        // Exemple :
        // Max Z = 5x1 + 4x2 + 6x3
        // s.c. :
        // x1 + x2 + x3 = 225
        // x1 >= 45
        // x2 <= 55
        // x3 >= 70
        // x3 <= 100
        // x1, x2, x3 >= 0
        
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

        Simplexe simplexe2 = new Simplexe(A2, b2, c2,symboles2,isMaximisation2);
        simplexe2.resoudre();
    }
}
