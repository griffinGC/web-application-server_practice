package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // InputStream을 받아서 출력
            BufferedInputStream inputStream = new BufferedInputStream(in);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            int cnt = 0;
            String line = bufferedReader.readLine();
            // line의 방식이 GET, POST 인지에 따라 분류
            String[] datas = line.split(" ");
            String url = "";
            log.debug("datas[0] : " + datas[0]);
            if(datas[0].equals("GET") || datas[0].equals("POST")){
                log.debug("HTTP 요청값은 : " + datas[0]);
                if (datas[1] != null){
                    url = datas[1];
                    log.debug("location is : " + url);
                    // ? 위치 찾아서 파싱
                    int idx = url.indexOf("?");
                    String requestUrl = url.substring(0, idx);
                    log.debug("requestUrl : " + requestUrl);
                    String params = url.substring(idx+1);
                    log.debug("params data : " + params);
                    User user = HttpRequestUtils.saveUserData(params);
                    log.debug("user info : " + user.toString());
                } else {
                    log.debug("location is null");
                }
            }
            log.debug("cnt : " + cnt + " / line : " + line);
            while (!"".equals(line)) {
                line = bufferedReader.readLine();
                if(line == null) return;
                log.debug("cnt : " + cnt + " / line : " + line);
                cnt++;
            }
            DataOutputStream dos = new DataOutputStream(out);
            // file 읽어오기
            // 루트 디렉토리에서 이걸 실행해서 가능
            byte[] body;
            if(url.equals("/index.html")){
                body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                log.debug("body data is : " + Arrays.toString(body));
            } else {
                body = "Hello World".getBytes();
                log.debug("no data so return body : " + body);
            }
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
