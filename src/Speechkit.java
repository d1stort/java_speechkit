import org.apache.http.HttpRequest;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.UUID;

public class Speechkit {

    public static String YANDEX_ASR_HOST = "asr.yandex.net";
    public static String YANDEX_ASR_PATH = "/asr_xml";
    public static double CHUNK_SIZE = Math.pow(1024, 2);
    public static String TTS_URL = "https://tts.voicetech.yandex.net/generate";
    public static Path FILE_PATH = Paths.get("c41vqhpdek.wav");
    public static int httpPort = 80;
    public static int httpsPort = 443;

    public static void main(String[] args) {
        //byte[] inputBytes = Files.readAllBytes(FILE_PATH);
        //text_to_speech("расходы на образование в ярославской области в 2016 году",null, "ru-RU",true,false,null);
        speech_to_text();
    }

    static void text_to_speech(String text, String filename, String lang, boolean convert, boolean as_audio, String file_like)
    {
        Settings set = new Settings();
        try {
            String sUrl = TTS_URL+"?text="+URLEncoder.encode(text,"UTF-8")+"&format=wav&lang="+lang+"&speaker=oksana&key="+set.YANDEX_API_KEY+"&emotion=neutral&speed=1.0";
            URL url = new URL(sUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            InputStream is = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[15000];

            while ((nRead = is.read(data,0, data.length)) != -1)
            {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] response = buffer.toByteArray();
            FileOutputStream fos = new FileOutputStream("output.wav");
            fos.write(response);
            fos.close();

            connection.disconnect();
        } catch (Exception e)
        {
            System.out.println("///error///"+e);
        }
    }

    static void speech_to_text()
    {
        try {
            String handshake = "GET /asr_partial HTTP/1.1\r\n" +
                    "Host: asr.yandex.net:80\r\n" +
                    "User-Agent: KeepAliveClient\r\n" +
                    "Upgrade: dictation\r\n\r\n";
            Settings set = new Settings();
            String topic = "queries";
            String lang = "ru-RU";
            UUID uuid = UUID.randomUUID();
            URL sUrl = new URL("https://"+YANDEX_ASR_HOST+YANDEX_ASR_PATH+"?uuid="+uuid+"&key="+set.YANDEX_API_KEY+"&topic="+topic+"&lang="+lang);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(YANDEX_ASR_HOST, httpsPort));

            byte[] data = Files.readAllBytes(FILE_PATH);
            HttpURLConnection conn = (HttpURLConnection) sUrl.openConnection(proxy);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "Transfer-Encoding", "chunked" );
            conn.setRequestProperty( "Content-Type", "audio/x-wav" );
            conn.setRequestProperty( "Content-Length", String.valueOf(data.length));
            conn.setRequestProperty("key", "e05f5a12-8e05-4161-ad05-cf435a4e7d5b");
            OutputStream os = conn.getOutputStream();
            os.write(data);
            System.out.println(conn.getResponseCode());

            /*Socket client = new Socket(YANDEX_ASR_HOST, httpsPort);
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();
            byte bytes[] = handshake.getBytes();
            out.write(handshake.getBytes(),0, in.read(bytes));
            out.flush();
            //BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println("????"+in.read());
            in.close();
            out.close();
            client.close();*/

            /*byte[] data = Files.readAllBytes(FILE_PATH);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(sUrl);
            //httpPost.setHeader("Transfer-Encoding", "chunked");
            //httpPost.setHeader("Content-Length", Integer.toString(data.length));
            httpPost.setHeader("Content-Type", "audio/x-wav");
            httpPost.setHeader("key", "e05f5a12-8e05-4161-ad05-cf435a4e7d5b");
            CloseableHttpResponse resp = client.execute(httpPost);
            System.out.println(resp.getStatusLine().getStatusCode());*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
