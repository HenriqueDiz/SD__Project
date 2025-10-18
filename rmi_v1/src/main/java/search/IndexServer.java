package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class IndexServer extends UnicastRemoteObject implements Index {

    private BlockingQueue<String> urlsToIndex;
    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // Hashset for non repeated URLS
    private RobotInterface robot;

    public IndexServer() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();
        indexedItems = new ConcurrentHashMap<>();
               
    }

    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer(); //server object
            Registry registry = LocateRegistry.createRegistry(8183); //registo de RMI
            registry.rebind("index", server); //usa o regito para registar o objecto do server
            System.out.println("Server ready. Waiting for input...");

            //todo: This approach needs to become interactive. Use a Scanner(System.in) to create a rudimentary user interface to:
            //1. Add urls for indexing
            //2. search indexed urls
            //server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");
            Scanner keyboard = new Scanner(System.in);
            String line;
            while((line = keyboard.nextLine()) != "")
                if(line.startsWith("http:") || line.startsWith("https:"))
                    server.putNew(line);
                else
                    server.searchWord(line).forEach(System.out::println);

            keyboard.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

    public synchronized String takeNext() throws RemoteException {
        //todo: not implemented fully. Prefer structures that return in a push/pop fashion

        String nextUrl = urlsToIndex.poll();
        if (nextUrl == null) {
            nextUrl = "";
        }

        return nextUrl;
    }

    public synchronized void putNew(String url) throws java.rmi.RemoteException {
        //todo: Example code. Must be changed to use structures that have primitives such as .add(...)
        urlsToIndex.add(url);

    }

    public synchronized void addToIndex(String word, String url) throws java.rmi.RemoteException {
        //TODO: not implemented
        //System.out.println("Indexing " + word + " for " + url);
        if(indexedItems.containsKey(word)){
            //System.out.println("Already indexed " + word + ", adding new url");
            HashSet<String> palavrasParaUrls = indexedItems.get(word);
            palavrasParaUrls.add(url);
            indexedItems.put(word, palavrasParaUrls);
        }else {
            //System.out.println("New word, adding to index");
            HashSet<String> palavrasNovas = new HashSet<String>();
            palavrasNovas.add(url);
            indexedItems.put(word, palavrasNovas);
        }
    }

    
    public synchronized List<String> searchWord(String word) throws java.rmi.RemoteException {
        //todo: not implemented
        System.out.println("Searching for " + word);
        robot.printOnWorker("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        if(indexedItems.containsKey(word)){
            System.out.println("Found " + indexedItems.get(word).size() + " results");
            ArrayList<String> resultadoPesquisa = new ArrayList<String>(indexedItems.get(word));
            return resultadoPesquisa;
        }
        return new ArrayList<String>();
    }

    public void subscribeRobot(RobotInterface robot) throws java.rmi.RemoteException {
        this.robot = robot;
    }
}
