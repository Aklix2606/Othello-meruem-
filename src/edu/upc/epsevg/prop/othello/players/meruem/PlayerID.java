package edu.upc.epsevg.prop.othello.players.meruem;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;


/**
 *
 * @author Aitor Alonso Coloma
 * @author Alexandre Picas Martínez
 */
public class PlayerID implements IPlayer, IAuto{

    private final String name;
    private int deepth;
    private CellType me;
    private boolean TIMEOUT;
    private int max_deepth;
    private boolean timeout_cut;
    private int n_nodes;
    

    
    /**
     * Constructora
     * 
     * @param name Nom del jugador.
     * @param max_deepth Profunditat màxima de l'algorisme.
     * @param timeout_cut Bolea per indicar si volem que el timout faci efecte sobre l'algorisme.
     */
    public PlayerID (String name, int max_deepth, boolean timeout_cut) {
        this.name = name;
        this.TIMEOUT = false;
        if (max_deepth == 0) this.max_deepth = 100;
        else this.max_deepth = max_deepth;
        this.timeout_cut = timeout_cut;
    }

    
    /**
     * Funció que serà executada quan s'acabi el temps de timeout predefinit en el joc.
     * Atura l'algorimse si timeout_cut és cert.
     */
    @Override
    public void timeout() {
        if(timeout_cut) this.TIMEOUT = true;
    }

    /**
     * Decideix el moviment del jugador donat l'estat del joc.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        this.me = s.getCurrentPlayer();
        this.deepth = 1;
        this.n_nodes = 0;
        ArrayList<Point> moves =  s.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L,0,  SearchType.RANDOM);
        } else {
            // hi ha possibles moviments a fer:
            TIMEOUT = false;
            int moviment = 0;
            double valor = Integer.MIN_VALUE;
            while(!TIMEOUT && deepth < max_deepth)
            {
                for (int i = 0; i < moves.size(); i++) {
                   GameStatus a = new GameStatus(s);

                    a.movePiece(moves.get(i));
                    n_nodes += 1;
                    if (a.isGameOver()){
                        return new Move( moves.get(moviment), 0L, 0, SearchType.MINIMAX);
                    } else {
                        double min = minValor(a, deepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
                        if (valor < min){
                            valor = min;
                            moviment = i;
                        }
                    }                
                }
                deepth += 1;
            }
            return new Move( moves.get(moviment), n_nodes, deepth, SearchType.MINIMAX);         
        }
    }
    
    /**
     * Funció maximitzadora del Minimax.
     * 
     * @param s Tauler i estat actual de joc.
     * @param mdp Profunditat maxima a la que ha d'arribar.
     * @param alpha Valor de alfa del minimax amb poda alfa-beta.
     * @param beta Valor de beta del minimax amb poda alfa-beta.
     * @return Retorna el valor maxim dels possibles movimens a fer.
     */
    private double maxValor(GameStatus s, int mdp, double alpha, double beta)
    {
        double valor = Integer.MIN_VALUE;
         if (mdp == 0) {
            return heuristic(s);
        }       
        if (s.currentPlayerCanMove()){
            // El jugador actual pot moure doncs busquem els moviments
            ArrayList<Point> moves = s.getMoves();
            if (TIMEOUT) {
                //System.out.println(mdp);
                return heuristic(s);
            }
            else for (int i = 0; i < moves.size(); i++) {
                
                
                GameStatus a = new GameStatus(s);
                
                a.movePiece(moves.get(i));
                n_nodes += 1;
                if (a.isGameOver()){
                    if (a.GetWinner() == me) {
                        return Integer.MAX_VALUE;
                    } else {
                        return Integer.MIN_VALUE;
                    }
                } else {
                    double min = minValor(a, mdp - 1, alpha, beta);
                    valor = Math.max(valor, min);
                    if (beta <= valor) return valor;
                    alpha = Math.max(valor, alpha);
                }
            }
        }
        return valor;
    }
    
    
    /**
     * Funció minimitzadora del minimax.
     * 
     * @param s Tauler i estat actual de joc.
     * @param mdp Profunditat maxima a la que ha d'arribar.
     * @param alpha Valor de alfa del minimax amb poda alfa-beta.
     * @param beta Valor de beta del minimax amb poda alfa-beta.
     * @return Retorna el valor minim dels possibles movimens a fer.
     */
    private double minValor(GameStatus s, int mdp, double alpha, double beta)
    {
        double valor = Integer.MAX_VALUE;
        if (mdp == 0) {
            return heuristic(s);
        }        
        if (s.currentPlayerCanMove()){
            // El jugador actual pot moure doncs busquem els moviments
            ArrayList<Point> moves = s.getMoves();
            if (TIMEOUT) {
                //System.out.println(mdp);
                return heuristic(s);
            }
            else for (int i = 0; i < moves.size(); i++) {
                
                GameStatus a = new GameStatus(s);
                a.movePiece(moves.get(i));
                n_nodes += 1;
                if (a.isGameOver()){
                    if (a.GetWinner() == me) {
                        return Integer.MAX_VALUE;
                    } else {
                        return Integer.MIN_VALUE;
                    }
                } else {
                    double max = maxValor(a, mdp  - 1, alpha, beta);
                    valor = Math.min(valor, max);
                    if (valor <= alpha) return valor;
                    beta = Math.min(valor, beta);
                }
            }
        }
        return valor;
    }
    
    
    /**
     * Funció que s'encarrega de retornar el valor final de les heurístiques
     * balancejades ja que cadascuna ha de tenir un valor.
     * 
     * @param s
     * @return retorna un valor que representa l'estat del joc en termes númerics
     */
    private double heuristic(GameStatus s)
    {
        return 10*esquines(s) + 4*preEsquines(s) + 
               4*costatsParells(s)+ 7*costats(s) + 7*zonaPerill(s);
    }
    
    
    /**
     * Funció que calcula la puntuació que te un tauler segons 
     * el numero de fitxes que hi ha a la "zona de perill" del tauler
     * depenent del color d'aquestes.
     * 
     * @param s Tauler i estat actual de joc.
     * @return Retorna el valor de la zona de perill (positiu en cas
     *          de guanyar a favor de Meruem, negatiu en cas contrari)
     */
    private double zonaPerill(GameStatus s) {
        double meves = 0;
        double enemy = 0;
        double ValZonaPerill = 0;
        int size = s.getSize()-1;
        for (int i  = 1; i < size-1; i++) {
            if (s.getPos(1,i) == me)
                meves++;
            else if (s.getPos(1,i) == me.opposite(me))
                enemy++;
            
            if (s.getPos(size-1,i) == me)
                meves++;
            else if (s.getPos(size-1,i) == me.opposite(me))
                enemy++;
            
            if (s.getPos(i,1) == me)
                meves++;
            else if (s.getPos(i,1) == me.opposite(me))
                enemy++;
            
            if (s.getPos(i,size-1) == me)
                meves++;
            else if (s.getPos(i,size-1) == me.opposite(me))
                enemy++;
        }
        
        if (meves + enemy != 0)
            ValZonaPerill = (enemy - meves)/(enemy + meves);
        return ValZonaPerill;
    }
    
    
    /**
     * Funció que s'encarrega de calcular la dominància que hi ha
     * sobre els costats
     * 
     * @param s Tauler i estat actual de joc.
     * @return Retorna un valor positiu en cas de que nosaltres dominem els costats
     *          i un valor negatiu en cas contrari
     */
    private double costats(GameStatus s)
    {
        
        double costatsMe = 0;
        double costatsEn = 0;
        double ValCostats = 0;
        int size = s.getSize()-1;
        //bucle que recorre els costats y compara a parells
        for (int i = 2; i < size-2; i++) {
            //costats verticals
                if (s.getPos(i,0) == me){
                    costatsMe+=1;
                }
                else if (s.getPos(i,0) == me.opposite(me)) {
                    costatsEn+=1;
                }
                if (s.getPos(i,size) == me){
                    costatsMe+=1;
                }
                else if (s.getPos(i,size) == me.opposite(me)) {
                    costatsEn+=1;
                }
                
            //Costats horitzontals
                if (s.getPos(0,i) == me){
                    costatsMe+=1;
                }
                else if (s.getPos(0,i) == me.opposite(me)) {
                    costatsEn+=1;
                }
                if (s.getPos(size,i) == me){
                    costatsMe+=1;
                }
                else if (s.getPos(size,i) == me.opposite(me)) {
                    costatsEn+=1;
                }
        }
        if (costatsMe - costatsEn != 0)
            ValCostats = (costatsMe - costatsEn)/(costatsMe + costatsEn);
        return ValCostats;
    }
    
    
    /**
     * Funció que s'encarrega de calcular la dominància dels costats
     * pero en aquesta es mira que els costats siguin simetrics.
     * 
     * @param s Tauler i estat actual de joc.
     * @return Retorna un valor positiu en cas de tenir major dominància.
     *  
     * 
     */
    private double costatsParells(GameStatus s) 
    {
        double costatsMe = 0;
        double costatsEn = 0;
        double ValCostats = 0;
        int size = s.getSize()-1;
        //bucle que recorre els costats y compara a parells
        for (int i = 1; i < size-1; i++) {
            //costats verticals
                if ((s.getPos(i,0) == me) && (s.getPos(i,size) == me)){
                    costatsMe+=2;
                }
                else if ((s.getPos(i,0) == me.opposite(me)) &&
                        (s.getPos(i,size) == me.opposite(me))) {
                    costatsEn+=2;
                }
                
            //Costats horitzontals
                if ((s.getPos(0,i) == me) && (s.getPos(size,i) == me)){
                    costatsMe+=2;
                }
                else if ((s.getPos(0,i) == me.opposite(me)) &&
                        (s.getPos(size,i) == me.opposite(me))) {
                    costatsEn+=2;
                }
                
        }
        if (costatsMe - costatsEn != 0)
            ValCostats = (costatsMe - costatsEn)/(costatsMe + costatsEn);
        return ValCostats;
    }
    
    
    /**
    * Funció que calcula el número de cantonades sobre les que es
    * té control, i sobre les que té control l'enemic.
    * 
    * @param s Tauler i estat actual de joc.
    * @return Retorna un número positiu en cas de tenir major control
    *          de les esquines que l'enemic, negatiu en cas contrari.
    */
    private double esquines(GameStatus s) 
    {
        double meEsquines = 0;
        double enEsquines = 0;
        if (s.getPos(0,0) == me)
            meEsquines+=1;
        else if (s.getPos(0,0) == me.opposite(me))
            enEsquines+=1;
        if (s.getPos(0,s.getSize()-1) == me)
            meEsquines+=1;
        else if (s.getPos(0,s.getSize()-1) == me.opposite(me))
            enEsquines+=1;
        if (s.getPos(s.getSize()-1,0) == me)
            meEsquines+=1;
        else if (s.getPos(s.getSize()-1,0) == me.opposite(me))
            enEsquines+=1;
        if (s.getPos(s.getSize()-1, s.getSize()-1) == me)
            meEsquines+=1;
        else if (s.getPos(s.getSize()-1, s.getSize()-1) == me.opposite(me))
            enEsquines+=1;
        
        double valEsquines = 0;
        if (meEsquines + enEsquines != 0)
            valEsquines = 100 * (meEsquines - enEsquines)/(meEsquines + enEsquines);
        
        return valEsquines;
    }
    
    
    /**
     * Funció que s'encarrega de calcular el valor de les fitxes
     * que es troben a les posicions que embolten les esquines
     * 
     * @param s Tauler i estat actual de joc.
     * @return Retorna un número positiu si no estem en aquestes posicions
     *          negatiu altrament.
     */
    private double preEsquines(GameStatus s)
    {
        int gran = s.getSize()-2;
        int gran2 = gran+1;
        double mePreEsquines = 0;
        double enPreEsquines = 0;
        if (s.getPos(0,1) == me)
            mePreEsquines++;
        else if (s.getPos(0,1) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(1, 0) == me)
            mePreEsquines++;
        else if (s.getPos(1,0) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(1, 1) == me)
            mePreEsquines++;
        else if (s.getPos(1,1) == me.opposite(me))
            enPreEsquines++;
        
        if (s.getPos(0, gran) == me)
            mePreEsquines++;
        else if (s.getPos(0,gran) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(gran2, 1) == me)
            mePreEsquines++;
        else if (s.getPos(gran2,1) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(gran, 1) == me)
            mePreEsquines++;
        else if (s.getPos(gran,1) == me.opposite(me))
            enPreEsquines++;
        
        if (s.getPos(0,gran) == me)
            mePreEsquines++;
        else if (s.getPos(0,gran) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(1, gran2) == me)
            mePreEsquines++;
        else if (s.getPos(1,gran2) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(1, gran) == me)
            mePreEsquines++;
        else if (s.getPos(1,gran) == me.opposite(me))
            enPreEsquines++;
        
        if (s.getPos(gran2,gran) == me)
            mePreEsquines++;
        else if (s.getPos(gran2,gran) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(gran, gran2) == me)
            mePreEsquines++;
        else if (s.getPos(gran,gran2) == me.opposite(me))
            enPreEsquines++;
        if (s.getPos(gran, gran) == me)
            mePreEsquines++;
        else if (s.getPos(gran,gran) == me.opposite(me))
            enPreEsquines++;
        
        double valPreEsquines = 0;
        
        if (mePreEsquines + enPreEsquines != 0)
            valPreEsquines = 100 * (enPreEsquines - mePreEsquines)/(mePreEsquines + enPreEsquines);
        
        return valPreEsquines;
    }
    
    
    
    /**
     * Funció que retorna un string que és el nom del jugador.
     * 
     * @return El nom del Jugador, en el cas del projecte de PROP seria el nom de l'equip.
     */
    @Override
    public String getName() {
        return name;
    }
}