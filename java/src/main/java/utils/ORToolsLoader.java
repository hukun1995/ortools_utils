package utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kun.hu
 * ortools lib库导入工具
 */
@Slf4j
public class ORToolsLoader {

    /**
     * 是否已经load
     */
    private static boolean loaded = false;

    public synchronized static void load(String libPath) {
        if (loaded) {
            log.info("or-tools has been loaded, libPath:{}", libPath);
            return;
        }

        try {
            if (libPath != null && !libPath.isEmpty()) {
                // 按输入地址加载
                System.load(libPath);
                loaded = true;
            } else {
                // 按库名加载
                System.loadLibrary("jniortools");
                loaded = true;
            }
            log.info("or-tools loaded successful, libPath:{}", libPath);
        }catch (Exception e){
            log.error("or-tools lib loaded failed, libPath: {}", libPath);
        }
    }
}
