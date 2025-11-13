package statistics;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import common.Utils;

/**
 * Implementação do callback de estatísticas que imprime atualizações no console.
 * Recebe notificações de mudanças nas estatísticas e exibe-as formatadas.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class StatsCallbackImpl extends UnicastRemoteObject implements StatsCallback {

    /**
     * Construtor que exporta o objeto remoto.
     * 
     * @throws RemoteException  Se ocorrer um erro ao exportar o objeto remoto.
     */
    public StatsCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void onStatsUpdate(Statistics stats) throws RemoteException {
        System.out.println("\n" + Utils.yellow("ATUALIZAÇÃO DE ESTATÍSTICAS (tempo real)"));
        System.out.println("-".repeat(30));

        try {
            Map<String, Integer> top10 = stats.getTop10Searches();
            if (top10.isEmpty()) {
                System.out.println("Ainda não há pesquisas registradas");
            } else {
                int rank = 1;
                for (Map.Entry<String, Integer> entry : top10.entrySet()) {
                    System.out.println(rank + ". " + entry.getKey() + " (" + entry.getValue() + " pesquisas)");
                    rank++;
                }
            }

            System.out.println("\n" + Utils.yellow("TEMPO MÉDIO DE RESPOSTA POR BARREL"));
            System.out.println("-".repeat(30));
            Map<String, Long> responseTimes = stats.getAverageResponseTime();
            if (responseTimes.isEmpty()) {
                System.out.println("Nenhum tempo de resposta registrado.");
            } else {
                for (Map.Entry<String, Long> entry : responseTimes.entrySet()) {
                    System.out.println("Barrel: " + entry.getKey() + " - Tempo médio: " + entry.getValue() + " ns");
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(Utils.red("Falha ao mostrar atualização de estatísticas: " + e.getMessage()));
        }
    }
}