import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        String pathToClass = HomePageServlet.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String pathToTemplates = pathToClass
                .substring(1)
                .replace("build/classes/java/main/", "templates/");

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(pathToTemplates);
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);

        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String queryZone = req.getParameter("timezone");

        TimeZone tz;
        if (queryZone == null) {
            Cookie[] cookies = req.getCookies();
            if (cookies == null) {
                tz = TimeZone.getTimeZone("UTC");
            } else {
                Cookie timezoneCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("lastTimezone")).findFirst().orElse(null);
                if (timezoneCookie == null) {
                    tz = TimeZone.getTimeZone("UTC");
                } else {
                    tz = TimeZone.getTimeZone(timezoneCookie.getValue());
                }
            }
        } else {
            String fixedTimezone = queryZone.replace("UTC", "GMT").replace(' ', '+');
            tz = TimeZone.getTimeZone(fixedTimezone);
        }

        ZonedDateTime nowZoned = ZonedDateTime.now(tz.toZoneId());
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z");
        String dateTime = nowZoned.format(dtFormatter);

        resp.setContentType("text/html; charset=utf-8");
        resp.addCookie(new Cookie("lastTimezone", tz.toZoneId().toString()));

        Context timeContext = new Context(resp.getLocale(), Map.of("dateTime", dateTime));
        engine.process("time", timeContext, resp.getWriter());
        resp.getWriter().close();
    }
}
