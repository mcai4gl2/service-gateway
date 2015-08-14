package service.gateway.test;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.OutputStream;

@Controller
public class PingController {
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public void ping(OutputStream outputStream) throws IOException {
        IOUtils.write("Pong", outputStream);
    }
}
