package pl.ds.websight.usermanager.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component (service = Servlet.class)
@SlingServletPaths (value = UserManagerShortUrlServlet.SHORT_PATH)
public class UserManagerShortUrlServlet extends SlingSafeMethodsServlet {

    static final String SHORT_PATH = "/apps/users";
    static final String URL = "/apps/websight-user-manager.html";

    @Override
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(URL);
        if (requestDispatcher == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        requestDispatcher.forward(request, response);
    }
}