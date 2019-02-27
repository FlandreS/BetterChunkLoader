package net.kaikk.mc.bcl.evernife;

public class ChunksByRank {


    public static int getAmout(String rank){

        if (rank.equalsIgnoreCase("Visitante")) return 9;   // + 0
        if (rank.equalsIgnoreCase("Novato")) return 12;     // + 3
        if (rank.equalsIgnoreCase("Membro")) return 16;     // + 4
        if (rank.equalsIgnoreCase("Aprendiz")) return 20;   // + 4
        if (rank.equalsIgnoreCase("Veterano")) return 25;   // + 5
        if (rank.equalsIgnoreCase("Prodigio")) return 31;   // + 6
        if (rank.equalsIgnoreCase("Genio")) return 37;      // + 6
        if (rank.equalsIgnoreCase("Mito")) return 43;       // + 6
        if (rank.equalsIgnoreCase("Lenda")) return 49;      // + 6

        return 9; // Default
    }


}
