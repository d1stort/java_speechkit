import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import java.nio.file.*;
import java.util.*;
import javax.xml.xpath.*;

public class Speechkit {

    public static String YANDEX_ASR_HOST = "asr.yandex.net";
    public static String YANDEX_ASR_PATH = "/asr_xml";
    public static double CHUNK_SIZE = Math.pow(1024, 2);
    public static String TTS_URL = "https://tts.voicetech.yandex.net/generate";
    public static Path FILE_PATH = Paths.get("output.wav");
    public static int httpPort = 80;
    public static int httpsPort = 443;

    public static void main(String[] args) {
        try
        {
            byte[] inputBytes = Files.readAllBytes(FILE_PATH);
            //text_to_speech("расходы на образование в ярославской области в 2016 году",null, "ru-RU",true,false,null);
            ArrayList<String> outputText = null;
            outputText = speech_to_text(inputBytes);
            for (String a: outputText
                 ) {
                System.out.println(a);
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    static void text_to_speech(String text, String filename, String lang, boolean convert, boolean as_audio, String file_like)
    {
        Settings set = new Settings();
        try {
            String sUrl = TTS_URL+"?text="+URLEncoder.encode(text,"UTF-8")+"&format=wav&lang="+lang+"&speaker=oksana&key="+set.YANDEX_API_KEY+"&emotion=neutral&speed=1.0";
            URL url = new URL(sUrl);
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
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

    public static ArrayList<String> speech_to_text(byte[] bytes)
    {

        ArrayList<String> recognisedText = null;
        try {

            Settings set = new Settings();
            String topic = "notes";
            String lang = "ru-RU";
            UUID uuid = UUID.randomUUID();
            URL sUrl = new URL("https://"+YANDEX_ASR_HOST+YANDEX_ASR_PATH+"?key="+set.YANDEX_API_KEY+"&uuid="+uuid+"&topic="+topic+"&lang="+lang);
            HttpsURLConnection conn = (HttpsURLConnection) sUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "audio/x-pcm;bit=16;rate=16000");
            conn.setRequestProperty("Content-Length",String.valueOf(bytes.length));
            conn.addRequestProperty("Host",YANDEX_ASR_HOST);
            //conn.setRequestProperty("Send-Chunked", "true");
            //conn.setChunkedStreamingMode(512);
            conn.setDoOutput(true);

            try(DataOutputStream out = new DataOutputStream(conn.getOutputStream())){
                out.write(bytes,0,bytes.length);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            if(conn.getResponseCode() != 200)
                System.out.println("Can't send and recognize speech");

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
                String s;
                StringBuilder sb = new StringBuilder();
                while ((s = reader.readLine()) != null){
                    sb.append(s);
                }

                recognisedText = getInternalXMLText(sb);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return recognisedText;
    }

    private static String expression = "//recognitionResults/variant";

    private static ArrayList<String> getInternalXMLText(StringBuilder xml) {

        ArrayList<String> arrayList = new ArrayList<>();

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(xml.toString().getBytes("UTF-8"));

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(in);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression expression = xPath.compile(Speechkit.expression);

            NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);


            for (int i = 0; i < nodeList.getLength(); i++) {
                arrayList.add(nodeList.item(i).getTextContent());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return arrayList;
    }
}
