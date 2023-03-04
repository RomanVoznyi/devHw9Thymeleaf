import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
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
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String queryZone = req.getParameter("timezone");

        if (queryZone == null || !TimeZone
                .getTimeZone(queryZone.replace("UTC", "GMT").replace(' ', '+'))
                .getID().equals("GMT")) {
            chain.doFilter(req, resp);
        } else {
            try {
                ZoneId.of(queryZone);
                chain.doFilter(req, resp);
            } catch (DateTimeException ex) {
                System.out.println("!Error - " + ex.getMessage());

                resp.setStatus(400);
                resp.setContentType("text/html; charset=utf-8");

                Context errorContext = new Context(resp.getLocale(), Map.of("error", ex.getMessage()));
                engine.process("error", errorContext, resp.getWriter());
                resp.getWriter().close();
            }
        }
    }
}
