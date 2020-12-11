package com.company;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.variables.integer.IntVar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static choco.cp.solver.CPSolver.eq;
import static choco.cp.solver.CPSolver.sum;

public class Main {

    public static void main(String[] args) {

        //n : nb infirmiers , p: nbr postes , h: horizon planification , jourFerr : liste des jours férié
        int n=7,p=6,h=14;
        ArrayList<Integer> jourFerr = new ArrayList<>();
        jourFerr.add(1);
        jourFerr.add(6);
        jourFerr.add(9);
        jourFerr.add(13);



        CPModel m= new CPModel();

        IntegerVariable [][][] x=new IntegerVariable[n][p][h];

        IntegerVariable [][] c=new IntegerVariable[n][h];

        IntegerVariable [] varTmp,varTmp2;


        for(int i=0;i<n;i++)
        {
            for(int j=0;j<p;j++)
            {
                for(int t=0;t<h;t++)
                {
                    x[i][j][t]=Choco.makeIntVar("X["+i+"]["+j+"]["+t+"]",0,1);
                    m.addVariable(x[i][j][t]);
                }

            }
        }



        for(int i=0;i<n;i++)

        {
            for(int t=0;t<h;t++)


            {
                c[i][t]=Choco.makeIntVar("C["+i+"]["+t+"]",0,1);
                m.addVariable( c[i][t]);
            }

        }




        //les contraintes
        // C1

        for(int t=0;t<h;t++)

        {
            varTmp= new IntegerVariable[n];

            for (int i = 0; i < n; i++)

            {
                varTmp[i] = x[i][0][t];
            }

            varTmp2= new IntegerVariable[n];


            for (int i = 0; i < n; i++)

            {
                varTmp2[i] = x[i][5][t];
            }

            m.addConstraint(Choco.eq(Choco.sum(varTmp), 1));
            m.addConstraint(Choco.eq(Choco.sum(varTmp2), 1));
        }



        //C2
        for(int t=0;t<h;t++)
        {

            varTmp= new IntegerVariable[n];


            for (int i = 0; i < n; i++)

            {
                varTmp[i] = x[i][4][t];
            }


            m.addConstraint(Choco.eq(Choco.sum(varTmp), 2));


        }



        //C3
        for (int t = 0; t < h; t++)
        {
            varTmp= new IntegerVariable[n*3];

            int k=0;
            for (int j = 1; j < 4; j++)
            {
                for (int i = 0; i < n; i++) {

                    varTmp[k] = x[i][j][t];
                    k++;

                }

            }
            m.addConstraint(Choco.leq(Choco.sum(varTmp), 3));
            m.addConstraint(Choco.geq(Choco.sum(varTmp), 2));
        }


        //C4
        for (int t = 0; t < h; t++)
        {
            if(!jourFerr.contains(t)) {

                for (int i = 0; i < n; i++) {
                    varTmp = new IntegerVariable[p];

                    for (int j = 0; j < p; j++)

                    {
                        varTmp[j] = x[i][j][t];

                    }

                    m.addConstraint(Choco.eq(Choco.sum(varTmp), Choco.minus(1,c[i][t])));
                }

            }
        }


        //C5

        for (int t = 0; t < jourFerr.size()-2; t++)
        {

            for (int i = 0; i < n; i++) {

                varTmp = new IntegerVariable[p*3];
                int k=0;
                for (int j = 0; j < p; j++)

                {
                    for (int l = 0; l < 3; l++)

                    {
                        varTmp[k] = x[i][j][jourFerr.get(t+l)];
                        k++;
                    }

                }

                m.addConstraint(Choco.leq(Choco.sum(varTmp), 3));
                m.addConstraint(Choco.geq(Choco.sum(varTmp), 1));
                }

        }



        //resolution

        CPSolver s=new CPSolver();
        s.read(m);
        s.solve();


        System.out.println("-------------- Horizon de planification "+h+" Jours--------------");

        System.out.println("-------------- Planning Semaine 1 --------------");

        AffichageRes(0,h/2,n,p,s,x,c);
        System.out.println(" -------------- Planning Semaine 2 --------------");
        AffichageRes(7,h,n,p,s,x,c);



    }

    public static void AffichageRes(int d,int h,int n,int p,CPSolver s,IntegerVariable [][][] x,IntegerVariable [][] c)
    {
        TableGenerator tableGenerator = new TableGenerator();

        List<String> headersList = new ArrayList<>();
        headersList.add("Jour");
        headersList.add("Infirmier");
        headersList.add("Poste");
        headersList.add("Valeur X (Affectation Infirmier) ");
        headersList.add("Valeur C (Congé Infirmier) ");


        List<List<String>> rowsList = new ArrayList<>();
        int k=0;
        for(int t=d;t<h;t++){

            for(int i=0;i<n;i++)

            {
                for(int j=0;j<p;j++)

                {

                    List<String> row = new ArrayList<>();
                    row.add(DayOfWeek.of(k+1).getDisplayName(TextStyle.FULL_STANDALONE,Locale.FRANCE).toString());
                    row.add(String.valueOf(i+1));
                    row.add(String.valueOf(j+1));
                    row.add(s.getVar(x[i][j][t]).toString());
                    row.add(s.getVar(c[i][t]).toString());



                    rowsList.add(row);
                }}

          k++;
        }

        System.out.println(tableGenerator.generateTable(headersList, rowsList));

    }

}
