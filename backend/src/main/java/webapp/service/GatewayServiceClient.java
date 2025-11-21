package webapp.service;

import common.PageInfo;
import gateway.GatewayInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webapp.dto.SearchResultDTO;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service Layer que abstrai a comunicação com o Gateway RMI.
 * 
 * @Service indica que é um componente de serviço do Spring.
 * É automaticamente detectado pelo component scan e pode ser injetado.
 * 
 * Responsabilidades:
 * - Comunicar com o Gateway via RMI
 * - Converter dados RMI para DTOs
 * - Tratar exceções RMI
 * - Enriquecer resultados com PageInfo
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@Service
public class GatewayServiceClient {
    
    private final GatewayInterface gateway;
    
    /**
     * Construtor com injeção de dependência.
     * @Autowired diz ao Spring para injetar o bean GatewayInterface
     */
    @Autowired
    public GatewayServiceClient(GatewayInterface gateway) {
        this.gateway = gateway;
    }
    
    /**
     * Realiza uma busca por múltiplas palavras.
     * 
     * Fluxo:
     * 1. Chama gateway.searchWords() via RMI
     * 2. Recebe List<String[]> onde cada array é [url, references]
     * 3. Para cada URL, busca título e descrição com PageInfo
     * 4. Converte tudo para SearchResultDTO
     * 5. Retorna lista de DTOs
     * 
     * @param words     Lista de palavras para buscar
     * @param page      Número da página (0-based)
     * @param pageSize  Quantidade de resultados por página
     * @return          Lista de resultados formatados
     */
    public List<SearchResultDTO> search(List<String> words, int page, int pageSize) {
        try {
            System.out.println("Buscando: " + words + " (página " + page + ")");
            
            // Chamada RMI ao Gateway
            List<String[]> rawResults = gateway.searchWords(words, page, pageSize);
            
            System.out.println("Gateway retornou " + rawResults.size() + " resultado(s)");
            
            // Converter resultados RMI para DTOs
            List<SearchResultDTO> dtos = new ArrayList<>();
            
            for (String[] result : rawResults) {
                String url = result[0];
                int references = Integer.parseInt(result[1]);
                
                // PageInfo busca título e descrição da página
                PageInfo pageInfo = new PageInfo(url);
                
                // Criar DTO com todas as informações
                SearchResultDTO dto = new SearchResultDTO(
                    url,
                    pageInfo.getTitle(),
                    pageInfo.getDescription(),
                    references
                );
                
                dtos.add(dto);
            }
            
            System.out.println("Resultados processados com sucesso");
            return dtos;
            
        } catch (RemoteException e) {
            System.err.println("Erro ao comunicar com Gateway: " + e.getMessage());
            throw new RuntimeException("Serviço de busca temporariamente indisponível", e);
        }
    }
    
    /**
     * Obtém lista de Barrels ativos.
     * 
     * @return Lista de strings com informação dos barrels (formato: "nome:porta:host:indexSize")
     */
    public List<String> getActiveBarrels() {
        try {
            return gateway.getActiveBarrels();
        } catch (RemoteException e) {
            System.err.println("Erro ao obter barrels ativos: " + e.getMessage());
            throw new RuntimeException("Erro ao obter barrels ativos", e);
        }
    }
    
    /**
     * Obtém lista de Barrels registrados.
     * 
     * @return Lista de strings com informação dos barrels registrados
     */
    public List<String> getRegisteredBarrels() {
        try {
            return gateway.getRegisteredBarrels();
        } catch (RemoteException e) {
            System.err.println("Erro ao obter barrels registrados: " + e.getMessage());
            throw new RuntimeException("Erro ao obter barrels registrados", e);
        }
    }
    
    /**
     * Adiciona uma URL para indexação.
     * 
     * @param url           URL a ser indexada
     * @param indexAnyway   Se true, reindexar mesmo que já esteja indexada
     * @return              true se já estava indexada, false se foi adicionada
     */
    public boolean addURL(String url, boolean indexAnyway) {
        try {
            return gateway.addURL(url, indexAnyway);
        } catch (RemoteException e) {
            System.err.println("Erro ao adicionar URL: " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar URL", e);
        }
    }
}
