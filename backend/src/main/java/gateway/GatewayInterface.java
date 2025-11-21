package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;

import statistics.BarrelStateCallback;
import statistics.Statistics;
import statistics.StatsCallback;

/**
 * Interface remota para o Gateway.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface GatewayInterface extends Remote {

    /**
     * Pesquisa uma palavra no Gateway.
     * @param word A palavra a ser pesquisada.
     * @return Uma lista de URLs onde a palavra foi encontrada.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public List<String> searchWordGateway(String word) throws RemoteException;

    /**
     * Adiciona uma URL ao Gateway.
     * @param url A URL a ser adicionada.
     * @param indexAnyway Indica se a URL deve ser indexada mesmo que não contenha palavras relevantes.
     * @return true se a URL foi adicionada com sucesso, false caso contrário.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public boolean addURL(String url, boolean indexAnyway) throws RemoteException;

    /**
     * Pesquisa várias palavras no Gateway.
     * @param words A lista de palavras a serem pesquisadas.
     * @param page O número da página de resultados (começando em 0).
     * @param pageSize O número de resultados por página.
     * @return Uma lista de arrays de strings, onde cada array contém as URLs encontradas para a palavra correspondente.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public List<String[]> searchWords(List<String> words, int page, int pageSize) throws RemoteException;

    /**
     * Obtém o total de resultados para uma pesquisa (sem paginação).
     * @param words A lista de palavras a serem pesquisadas.
     * @return O número total de resultados encontrados.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public int getTotalResults(List<String> words) throws RemoteException;

    /**
     * Obtém as URLs ativas no Gateway.
     * @return Uma lista de URLs ativas.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public List<String> getActiveBarrels() throws RemoteException;

    /**
     * Obtém as URLs registradas no Gateway.
     * @return Uma lista de URLs registradas.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public List<String> getRegisteredBarrels() throws RemoteException;

    /**
     * Obtém URLs associadas a uma URL indexada.
     * @param url A URL indexada.
     * @return O conjunto de URLs associadas.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;
  
    /**
     * Registra um barrel no Gateway.
     * @param host O endereço IP ou hostname do barrel
     * @param port A porta do barrel
     * @param name O nome do barrel
     * @throws RemoteException Se ocorrer um erro de rede.
     */
    public void registerBarrel(String host, int port, String name) throws RemoteException;

    /**
     * Desregistra um barrel ativo do Gateway quando ele vai desligar.
     * Remove apenas da lista de barrels ativos, mantendo o histórico de registrados.
     * @param name O nome do barrel a ser desregistrado
     * @param port A porta do barrel a ser desregistrado
     * @throws RemoteException Se ocorrer um erro de rede.
     */
    public void unActiveBarrel(String name, int port) throws RemoteException;

    /**
     * Obtém as estatísticas dos barrels.
     * 
     * @return                  A instância de BarrelStatistics.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    public Statistics getBarrelStatistics() throws RemoteException;

    /**
     * Registra um callback para receber notificações de atualizações de estatísticas.
     * 
     * @param callback          Callback a ser registrado.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void registerStatsCallback(StatsCallback callback) throws java.rmi.RemoteException;
    
    /**
     * Remove o registro de um callback de estatísticas.
     * 
     * @param callback          Callback a ser removido.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void unregisterStatsCallback(StatsCallback callback) throws java.rmi.RemoteException;

    /**
     * Registra um callback para receber notificações de mudanças no estado dos barrels.
     * 
     * @param callback          Callback a ser registrado.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void registerBarrelStateCallback(BarrelStateCallback callback) throws java.rmi.RemoteException;
    
    /**
     * Remove o registro de um callback de estado dos barrels.
     * 
     * @param callback          Callback a ser removido.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void unregisterBarrelStateCallback(BarrelStateCallback callback) throws java.rmi.RemoteException;
}