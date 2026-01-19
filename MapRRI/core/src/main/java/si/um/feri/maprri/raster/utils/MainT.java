package si.um.feri.maprri.raster.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainT {

    static String mapServiceUrl = "https://maps.geoapify.com/v1/tile/";
    static String token = "?&apiKey=" + Keys.GEOAPIFY;
    static String tilesetId = "dark-matter-purple-roads";
    static String format = "@2x.png";

    public static void main(String[] args) throws IOException {
        URL url = new URL(mapServiceUrl + tilesetId + "/" + 15 + "/" + 17806 + "/" + 11583 + format + token);
        ByteArrayOutputStream bis = fetchTile(url);

        writeBytesToFile("test.png", bis.toByteArray());
    }

    private static void writeBytesToFile(String fileOutput, byte[] bytes)
            throws IOException {

        try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
            fos.write(bytes);
        }

    }

    public static ByteArrayOutputStream fetchTile(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
            bis.write(bytebuff, 0, n);
        }
        return bis;
    }
}
