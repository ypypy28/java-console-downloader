/**
 * Created by ypypy
 */
package ru.ypypy28.downloader;


import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Downloader {
    final private ConcurrentLinkedQueue<String[]> URLS_QUEUE;
    final private String SAVE_DIR;
    final private int N_THREADS;
    final private int DOWNLOAD_SPEED;

    public Downloader(String pathToUrls, String saveDir, int nThreads, String dSpeed) {
        this.URLS_QUEUE = this.makeQueue(pathToUrls);
        this.SAVE_DIR = saveDir;
        this.N_THREADS = nThreads;
        this.DOWNLOAD_SPEED = this.getSpeed(dSpeed);
    }
    
    // callable method
    public int download() {
        ExecutorService pool = Executors.newFixedThreadPool(N_THREADS);
        
        // list of futures for callable return
        List <Future<Integer>> downloadedLength = new LinkedList<Future<Integer>>();
        int oneThreadSpeed = (int) Math.ceil(DOWNLOAD_SPEED/N_THREADS);
        
        for (int i = 0; i < N_THREADS; i++) {
            Future fut = pool.submit(new UrlTargetCallable(URLS_QUEUE, SAVE_DIR, N_THREADS, oneThreadSpeed));
            downloadedLength.add(fut);
        }

        int total = 0;
        for (Future fut : downloadedLength) {
            try {
               total += (int) fut.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        return total;
    }


    // convert speed value to bytes if needed
    // allowed suffixes: k (kilobyte), m (megabyte)
    private int getSpeed(String dSpeed) {
        try {
            return Integer.parseInt(dSpeed);
        } catch (NumberFormatException e) {
            int dSpeedLength = dSpeed.length();
            char suf = dSpeed.charAt(dSpeedLength - 1);
            int speed = 0;

            switch (suf) {
                case 'k': speed = Integer.parseInt(dSpeed.substring(0, dSpeedLength-1))*1024;
                          break;
                case 'm': speed = Integer.parseInt(dSpeed.substring(0, dSpeedLength-1))*1024*1024;
                          break;
                default : System.out.println("ОШИБКА: Вы ввели некорректное значение скорости" +
                                              "(скорость в байтах + возможные суффикцы: k, m)");
                          System.exit(1);
            }

            return speed;
        }
    }
    // making queue from string: "url file_name"
    private ConcurrentLinkedQueue<String[]> makeQueue(String pathToUrls) {
        ConcurrentLinkedQueue<String[]> urlsQueue = new ConcurrentLinkedQueue<>();

        try ( BufferedReader fileReader = new BufferedReader(new FileReader(pathToUrls)) ){
            String line = fileReader.readLine();
            while ( line != null ) {
//                System.out.println(line.split(" "));
                urlsQueue.add(line.split(" "));
                line = fileReader.readLine();

            }

        } catch (IOException e) {
            System.out.println("ОШИБКА: проверьте, что указанный файл существует\n" + e.getMessage());
            System.exit(1);
        }

        return urlsQueue;
    }

    public static void main(String[] args) {
        long startTimer = System.currentTimeMillis();

        ConsoleArgs a = new ConsoleArgs(args);

        Downloader dl = new Downloader(a.URLS_FILE, a.SAVE_DIR, a.N_THREADS, a.DOWNLOAD_SPEED);

        int totalDownloaded = dl.download();

        long endTimer = System.currentTimeMillis();
        double deltaTimer = (endTimer - startTimer)/1000.0;
        System.out.println("\nВремя выполнения программы: " + String.format("%.2f", deltaTimer) + " секунд(ы)" +
                           "\nВсего скачано: " + totalDownloaded + " байт");

    }

}
