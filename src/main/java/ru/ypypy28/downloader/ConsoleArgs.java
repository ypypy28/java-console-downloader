package ru.ypypy28.downloader;

/**
 * Created by ypypy
 */
public class ConsoleArgs {
    public final String URLS_FILE;
    public final String SAVE_DIR;
    public final int N_THREADS;
    public final String DOWNLOAD_SPEED;

    private final int ARGS_SIZE = 8;


    private void showHelpAndQuit(){
        System.out.println("Не правильно заданы параметры,\n" +
                "Проверьте, что задали значение всем обязательным флагам:\n\n"+
                "  -f F        путь к файлу со списком ссылок\n" +
                "  -o O        путь к папке, где сохранять файлы\n" +
                "  -n N        количество одновременно качающих потоков (1,2,3,4...)\n" +
                "  -l L        общее ограничение на скорость скачивания, для всех потоков");
        System.exit(1);
    }

    public ConsoleArgs(String[] args) {
        // args.length should always be ARGS_SIZE (8)
        if (args.length != ARGS_SIZE){
            this.showHelpAndQuit();
        }

        Integer indexOfUrlsFile = null;
        Integer indexOfSaveDir = null;
        Integer indexOfNThreads = null;
        Integer indexOfDownloadSpeed = null;

        for (int i = 0; i < ARGS_SIZE; i += 2){
            switch (args[i]) {
                case "-f": indexOfUrlsFile = i + 1;
                           break;
                case "-o": indexOfSaveDir = i + 1;
                           break;
                case "-n": indexOfNThreads = i + 1;
                           break;
                case "-l": indexOfDownloadSpeed = i + 1;
                           break;
                  default: this.showHelpAndQuit();
            }
        }
        this.URLS_FILE = args[indexOfUrlsFile];
        this.SAVE_DIR = args[indexOfSaveDir];
        this.N_THREADS = Integer.parseInt(args[indexOfNThreads]);
        if (this.N_THREADS == 0) {
            this.showHelpAndQuit();
        }
        this.DOWNLOAD_SPEED = args[indexOfDownloadSpeed];
    }
}
