package mx.edu.utez.pigestiontutorias.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;

public class SQLConnector {

    private static HikariDataSource dataSource;

    static {
        try {
            ClassLoader classLoader = SQLConnector.class.getClassLoader();
            URL walletUrl = classLoader.getResource("wallet/");

            if (walletUrl == null) {
                throw new RuntimeException("No se encontró la Wallet");
            }

            String walletPath = URLDecoder.decode(walletUrl.getPath(), StandardCharsets.UTF_8);
            if (walletPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                walletPath = walletPath.substring(1);
            }
            walletPath = walletPath.replace("\\", "/");


            System.setProperty("oracle.net.tns_admin", walletPath);

            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");
            String dbName = System.getenv("DB_NAME");

            // Si falta alguno en el entorno, buscamos en el archivo .properties
            if (dbUser == null || dbPass == null || dbName == null) {
                System.err.println("Advertencia: Faltan variables de entorno de la BD. Buscando en credentials.properties...");
                Properties creds = new Properties();

                try (InputStream is = classLoader.getResourceAsStream("credentials.properties")) {
                    if (is == null) {
                        throw new RuntimeException("No se encontró el archivo credentials.properties ni las variables de entorno de la base de datos.");
                    }

                    byte[] fileBytes = is.readAllBytes();

                    java.nio.charset.Charset detectedCharset = detectCharset(fileBytes);
                    System.out.println("Codificación detectada para credentials.properties: " + detectedCharset.name());

                    try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileBytes);
                         java.io.InputStreamReader reader = new java.io.InputStreamReader(bais, detectedCharset)) {
                        creds.load(reader);
                    }

                    // Si ya se habían leído del entorno, conservamos ese valor; si no, del archivo
                    if (dbUser == null) dbUser = creds.getProperty("db.user");
                    if (dbPass == null) dbPass = creds.getProperty("db.pass");
                    if (dbName == null) dbName = creds.getProperty("db.name");
                }
            }

            if (dbName == null) {
                throw new RuntimeException("El nombre de la base de datos (db.name / DB_NAME) no está configurado.");
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("oracle.jdbc.OracleDriver");

            config.setJdbcUrl("jdbc:oracle:thin:@" + dbName);

            config.setUsername(dbUser);
            config.setPassword(dbPass);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(20000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            System.out.println("¡Pool de conexiones a la base de datos inicializado con éxito!");

        } catch (Exception e) {
            System.err.println("Error crítico al inicializar la base de datos");
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection() {
        if(dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static java.nio.charset.Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return StandardCharsets.UTF_8;
        }

        java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);

        try {
            decoder.decode(java.nio.ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (java.nio.charset.CharacterCodingException e) {
            return StandardCharsets.ISO_8859_1;
        }
    }
}