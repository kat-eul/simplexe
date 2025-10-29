package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimplexeRunner {

    // Format attendu (expliqué ci-dessous) :
    // - lignes vides ou commençant par '#' sont ignorées (commentaires)
    // - 1ère ligne : "MAX" ou "MIN" (insensible à la casse) -> type d'optimisation
    // - 2ème ligne : coefficients de la fonction objectif (c), séparés par espaces
    // - lignes suivantes : chaque ligne = une contrainte :
    //     coef1 coef2 ... coefN  SYMBOL  RHS
    //   où SYMBOL est l'un de: '<'  '>'  '='
    //
    // Exemple complet dans problem-example.txt fourni avec ce repo.
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java SimplexeRunner <fichier_probleme.txt>");
            System.exit(2);
        }
        Path path = Path.of(args[0]);
        try {
            List<String> raw = Files.readAllLines(path);
            List<String> lines = new ArrayList<>();
            for (String l : raw) {
                String t = l.trim();
                if (t.isEmpty()) continue;
                if (t.startsWith("#")) continue;
                lines.add(t);
            }
            if (lines.size() < 2) {
                System.err.println("Fichier invalide : il faut au moins une ligne pour MAX/MIN et une ligne pour c.");
                System.exit(3);
            }

            // 1) optimisation type
            String optToken = lines.get(0).trim().toLowerCase();
            boolean isMaximisation;
            if (optToken.startsWith("max")) {
                isMaximisation = true;
            } else if (optToken.startsWith("min")) {
                isMaximisation = false;
            } else {
                System.err.println("Première ligne doit être MAX ou MIN (insensible à la casse). Trouvé: " + lines.get(0));
                System.exit(4);
                return;
            }

            // 2) coefficients c
            String[] cTokens = lines.get(1).trim().split("\\s+");
            int n = cTokens.length;
            double[] c = new double[n];
            for (int i = 0; i < n; i++) {
                c[i] = Double.parseDouble(cTokens[i]);
            }

            // 3) contraintes (toutes les lignes restantes)
            int m = lines.size() - 2;
            if (m <= 0) {
                System.err.println("Aucune contrainte fournie.");
                System.exit(5);
            }
            double[][] A = new double[m][n];
            double[] b = new double[m];
            char[] symboles = new char[m];

            for (int i = 0; i < m; i++) {
                String line = lines.get(2 + i);
                // on cherche le symbole '<' '>' '=' séparateur
                // on suppose que le symbole est isolé (ou entouré d'espaces)
                String[] parts = line.trim().split("\\s+");
                // On parcourt pour trouver le symbole
                int symIndex = -1;
                for (int k = 0; k < parts.length; k++) {
                    if (parts[k].equals("<") || parts[k].equals(">") || parts[k].equals("=")) {
                        symIndex = k;
                        break;
                    }
                }
                if (symIndex == -1) {
                    System.err.println("Ligne de contrainte invalide (symbole <, > ou = manquant) : " + line);
                    System.exit(6);
                }
                int coefCount = symIndex;
                if (coefCount != n) {
                    System.err.println("Nombre de coefficients dans la contrainte (attendu " + n + ") différent: " + line);
                    System.exit(7);
                }
                for (int j = 0; j < n; j++) {
                    A[i][j] = Double.parseDouble(parts[j]);
                }
                symboles[i] = parts[symIndex].charAt(0);

                if (symIndex + 1 >= parts.length) {
                    System.err.println("RHS manquant pour la contrainte: " + line);
                    System.exit(8);
                }
                b[i] = Double.parseDouble(parts[symIndex + 1]);
            }

            System.out.println("Lecture OK : n=" + n + " variables, m=" + m + " contraintes, optimisation=" + (isMaximisation ? "MAX" : "MIN"));
            // Instancie et lance le simplexe (utilise ta classe Simplexe)
            Simplexe s = new Simplexe(A, b, c, symboles, isMaximisation);
            s.resoudre();

        } catch (IOException e) {
            System.err.println("Erreur lecture fichier : " + e.getMessage());
            System.exit(10);
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique : " + e.getMessage());
            System.exit(11);
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
            System.exit(12);
        }
    }
}