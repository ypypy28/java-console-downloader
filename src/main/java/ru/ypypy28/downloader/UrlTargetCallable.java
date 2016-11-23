package ru.ypypy28.downloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ypypy
 */
public class UrlTargetCallable implements Callable {

    // current implementation is not for large files
    // (if needed use long)
    private int totalDownloaded = 0;

    private ConcurrentLinkedQueue<String[]> urlsFilenames;
    final private String SAVE_DIR;
    final private int N_THREADS;
    final private int DOWNLOAD_SPEED;
    final private int CHUNK_SIZE = 1024;


    public UrlTargetCallable(ConcurrentLinkedQueue<String[]> urls, final String saveDir, final int nThreads, final int dSpeed) {
        this.urlsFilenames = urls;
        this.SAVE_DIR = saveDir;
        this.N_THREADS = nThreads;
        this.DOWNLOAD_SPEED = dSpeed;
    }

    // return total download size
    public Integer call() {
        String[] nextTarget = urlsFilenames.poll();
        while (nextTarget != null) {
            URL url = null;
            BufferedInputStream in = null;
            try {
                url = new URL(nextTarget[0]);
                in = new BufferedInputStream(url.openStream());
                System.out.println("Начинаем скачивать файл по ссылке:\n"+url);

            } catch (IOException e) {
                System.out.println("BAD URL: " + nextTarget[0]);
//                e.printStackTrace();
            }

            if (in != null) {
                String filename = nextTarget[1];
                long timerStart = System.currentTimeMillis();

                try (FileOutputStream f = new FileOutputStream( SAVE_DIR + "\\" + filename )){
                    final byte[] data = new byte[CHUNK_SIZE];
                    int count;
                    int downloadedPerSec = 0;
                    long timerEnd;
                    long timerDelta;
                    while ( (count = in.read(data, 0, CHUNK_SIZE)) != -1 ) {
                        f.write(data, 0, count);
                        totalDownloaded += count;
                        downloadedPerSec += count;
                        f.flush();

                        // when downloaded more bytes than allowed per second
                        // check if 1 second isn't finished yet. If so - thread go to sleep
                        // otherwise current speed less speed limit (it's ok)
                        if (downloadedPerSec > DOWNLOAD_SPEED) {
                            downloadedPerSec = 0;
                            timerEnd = System.currentTimeMillis();
                            if ( (timerDelta = timerEnd - timerStart) < 1000 ) {
                                Thread.sleep(1000-timerDelta);
                            }
                            timerStart = System.currentTimeMillis();
                        }
                    }
//                    System.out.println("Скачано: " + totalDownloaded);
                } catch (IOException e) {
                    System.out.println("Ошибка записи в файл " + filename);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("URL: " + url + "\n\tSaved as: " + filename);
            }
            nextTarget = urlsFilenames.poll();

        }
        return Integer.valueOf(totalDownloaded);

    }


}
