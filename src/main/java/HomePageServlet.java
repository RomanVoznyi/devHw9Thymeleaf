import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/")
public class HomePageServlet extends HttpServlet {
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
        resp.setContentType("text/html; charset=utf-8");
        engine.process("hello", new Context(), resp.getWriter());
        resp.getWriter().close();
    }
}
